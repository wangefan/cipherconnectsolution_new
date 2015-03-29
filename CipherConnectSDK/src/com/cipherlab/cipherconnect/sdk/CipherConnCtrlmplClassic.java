package com.cipherlab.cipherconnect.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.cipherlab.help.ArrayHelper;

public class CipherConnCtrlmplClassic extends CipherConnCtrlmplBase {
	//public static final boolean _DEBUG = false;
	
	private UUID mUuid;
    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";
    private ListenAndConnThread mListenAndConnThread = null;
	private static final int STATE_OFFLINE = -1;       // we're doing nothing
	private static final int STATE_ONLINE = 0;       // start to listen connection
	private int mServrState = STATE_OFFLINE; //Only used for server now.
	private Handler mMainThrdHandler = null;
	
	private AutoConnectThread mAutoConnectThread;
	private ConnectedThread mConnectThread;
	private boolean mIsConnected = false;
	
	public CipherConnCtrlmplClassic(Context context){
		super(context);
		
		//well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB.
		this.mUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		mMainThrdHandler = new Handler();
		mResetListenThread();
	}
	
	protected void fireCipherConnectControlError(ICipherConnBTDevice device, Exception e){
		if(mListenerList == null)
			return;
		for (ICipherConnectControlListener l : this.mListenerList) {
			if(l!=null)
			{
				l.onCipherConnectControlError(device, 0, e.getMessage());
			}
		}
	}
	
	//================ Server functions begin=============
	private void mResetListenThread()
	{
		if(mListenAndConnThread != null)
		{
			mListenAndConnThread.Close();
			mListenAndConnThread = null;
		}
	}
	
	private boolean mFireListenAndConnThread()
	{
		if(BluetoothAdapter.getDefaultAdapter().isEnabled() == false)
		{
			StopListenAndConn();
			fireCipherListenServerOffline();
			return false;
		}
		
		mResetListenThread();
		
		try {
			mListenAndConnThread = new ListenAndConnThread(true);
		}
		catch (CipherConnectErrException e) {
            e.printStackTrace();
            mListenAndConnThread =  null;
            return false;
        } catch (IOException e) {
        	e.printStackTrace();
			mListenAndConnThread =  null;
			return false;
		}
		mListenAndConnThread.start();
		mServrState = STATE_ONLINE;
		fireCipherListenServerOnline();
		return true;
	}
	
	public boolean StartListening()
	{
		if(mServrState == STATE_ONLINE)
			return false;
		if(mIsConnected)
		{
			mServrState = STATE_ONLINE;
			return true;
		}
		
		return mFireListenAndConnThread();
	}
	
	private void StopListenAndConn()
	{
		mServrState = STATE_OFFLINE;
		mResetListenThread();
	}
	
	public void StopListening()
	{
		mServrState = STATE_OFFLINE;
		if(mListenAndConnThread != null)
		{
			//Here will fire offline in thread. 
			mListenAndConnThread.StopServer();
		}
		else {
			//fire offline ourself
			fireCipherListenServerOffline();
		}
	}
	//================ Server functions end=============
	
	public void setAutoReconnect(boolean enable, ICipherConnBTDevice device)throws NullPointerException{
		if(enable){
			if(device == null)
				throw new NullPointerException();
			
			if(this.mAutoConnectThread==null){
				//if(_DEBUG)
				//	Log.d("CipherConnectControl","The AutoConnectThread is opening.");

				this.mAutoConnectThread = new AutoConnectThread(device);
				this.mAutoConnectThread.start();
			}
		}
		else{
			//if(_DEBUG)
			//	Log.d("CipherConnectControl","The AutoConnectThread is closeing.");
			
			if(this.mAutoConnectThread!=null)
				this.mAutoConnectThread.cancel();
			
			this.mAutoConnectThread = null;
		}
	}
	
	public void connect(ICipherConnBTDevice device) throws NullPointerException
	{
		if(device == null || device.getDeviceName() == null)
			throw new NullPointerException();
		
    	if(this.isConnected()){
			//if(_DEBUG)
			//	Log.d("CipherConnectControl", "CipherConnectService.bt_connected:Can't connect again, because you have already connected");
    		
			return;
    	}
    	
    	synchronized(this){
        	if(this.mConnectThread==null){
        		this.mConnectThread = null;
        		this.mConnectThread = new ConnectedThread(device);
        		this.mConnectThread.start();
        	}
    	}
	}
	
	public void connect(String deviceName, String deviceAddr)throws NullPointerException
    {
		ICipherConnBTDevice device = new CipherConnBTDevice(deviceName, deviceAddr);
		connect(device);
	}

	public void disconnect() {
		mResetConnThrd();
		/*
		if(mConnectThread != null && isConnected())
		{
			mConnectThread.();
		}
		else if (mListenAndConnThread != null)
		{
			
		}
		*/
	}

	public ICipherConnBTDevice[] getBtDevices() {
		Set<BluetoothDevice> btDericeList = this.getBtDeviceList();
		int nSize = btDericeList != null ? btDericeList.size() : 0;
		ICipherConnBTDevice [] devices = new CipherConnBTDevice[nSize];
		int idxDevice = 0;
		for (BluetoothDevice device : btDericeList) {
			devices[idxDevice++] = new CipherConnBTDevice(device.getName(), device.getAddress());
		}
		return devices;
	}

	public boolean isAutoReconnect() {
		if(this.mAutoConnectThread==null)
			return false;
		
		return true;
	}

	public boolean isConnected() {
		if(this.mConnectThread==null){
			if(mListenAndConnThread == null)
				return false;
			else
			{
				return mIsConnected;
			}
		}
		
		if(this.mConnectThread.isAlive() && this.mIsConnected)
			return true;
		
		return false;
	}
	
	private void SetConnected(boolean connected){
		synchronized (CipherConnectControl.class) {
			this.mIsConnected = connected;
		}
	}
	
	private Set<BluetoothDevice> getBtDeviceList(){
		if(BluetoothAdapter.getDefaultAdapter()==null){
			this.fireCipherConnectControlError(
					null, 
					CipherConnectControlResource.please_turn_on_Bluetooth_id,
					CipherConnectControlResource.please_turn_on_Bluetooth);
			
			return null;
		}
		Set<BluetoothDevice> dericeList = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		return dericeList;
	}
	
	private void mResetConnThrd() {
		if(this.mConnectThread!=null){
    		this.mConnectThread.cancel();
    	}
		this.mConnectThread = null;
		
		if(BluetoothAdapter.getDefaultAdapter()==null)
			return;
		
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
	}
	
	private void mProcessBarcode(byte[] buffer, ICipherConnBTDevice device) throws UnsupportedEncodingException{
    	if(buffer==null || buffer.length==0)
    		return ;
    	
    	buffer = ArrayHelper.clear(buffer);
    	StringBuffer sb = null;
    	
    	if(Cipher1860Helper.IsPackageData(buffer)==true)
    	{
    		//buffer = Cipher1860Helper.fixPackageDataTag(buffer);
    		sb = Cipher1860Helper.RFID_PackageDataMoreTag(buffer);
    		//sb.append('\n');
    	}
    	else
    	{
    		sb = new StringBuffer();
            for (byte b : buffer) {
            	char c = (char)b;
            	if(c<=0)
            		break;
            	if(c==13)
            	{
            		c='\n';
            		sb.append(c);
            		//Log.e("CipherConnectControl", "Add a Enter("+sb.toString()+").");
            		
            		break;
            	}
            	sb.append(c);
			}
    	}
	
        String barcode = sb.toString();
		
		StringTokenizer st = new StringTokenizer(barcode,"\n");
        if(st.countTokens()==1){
        	fireReceivingBarcode(device, barcode);
        }
        else{
        	
        	int count = st.countTokens();
        	for(int i=0;i<count;i++)
        	{
        		String code = (String)st.nextElement();
        		if(i<count-1)
        			fireReceivingBarcode(device,code +"\n");
        		else
        			fireReceivingBarcode(device,code);
        		
        		try {
					Thread.sleep(300);
				} 
				catch (Exception e) {
				}
        	}
        	/*
        	while (st.hasMoreElements()) {
        		String code = (String)st.nextElement();
        		fireReceivingBarcode(mDeviceName+"B",code +"\n");
        		//fireReceivingBarcode(mDeviceName+"B",code);
				try {
					Thread.sleep(300);
				} 
				catch (Exception e) {
				}
			}
			*/
        }
    }
	
	private BluetoothDevice getBtDevice(String deviceAddr)
	{	
    	Set<BluetoothDevice> dericeList = getBtDeviceList();
    	if(dericeList==null || dericeList.size()==0){
    		return null;
    	}
    	
    	for (BluetoothDevice btDevice : dericeList) {
    		if(deviceAddr.equals(btDevice.getAddress()))
    			return btDevice;
		}
    	
    	return null;
    }
	
	private class CipherConnectErrException extends Exception  
	{
		private static final long serialVersionUID = 5548848902595353181L;
		public static final int INFO_NOT_CIPHER_DEVICE = 0;
		public static final int INFO_SERVER_SKT_ERROR = 1;
		public static final int INFO_SKT_ERROR = 2;
		public static final int INFO_BTDEVICE_NONE = 3;
		
		private int mInfoID;
		
		public CipherConnectErrException(int nInfoID)
		{
			super();
			mInfoID = nInfoID;
		}
		
   		public String getMessage()
   		{
   			String strResult = "Unknown Message";
   			switch (mInfoID) {
   			case INFO_NOT_CIPHER_DEVICE:
   				strResult = "Not Cipher Devices";
   				break;
   			case INFO_SERVER_SKT_ERROR:
   				strResult = "Create Server socket error";
   				break;
   			case INFO_SKT_ERROR:
   				strResult = "Create socket error";
   				break;
   			case INFO_BTDEVICE_NONE:
   				strResult = "Get bluetooth device error";
   				break;
   			default:
   				break;
   			}
   			return strResult; 
		}
	}
	
	/**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted and trans data
     * (or until cancelled).
     */
    private class ListenAndConnThread extends Thread 
    {
        // Data members
    	private BluetoothAdapter mBTAdapter = null;
        private BluetoothServerSocket mServerSocket;
        private BluetoothSocket mSocket;
        private InputStream mInStream;
        private String mSocketType;
        private String mTAG = "ListenAndConnThread";
        private boolean mBSecure;

        //constructor
        public ListenAndConnThread(boolean secure) throws IOException, CipherConnectErrException 
        {
        	mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        	mServerSocket = null;
        	mSocket = null;
        	mInStream = null;
        	mBSecure = secure;
            mSocketType = mBSecure ? "Secure" : "Insecure";
            mCloseServerSocket();
            
        	if(mBTAdapter == null)
        		throw new NullPointerException();
            if (mBSecure) {
            	mServerSocket = mBTAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                		mUuid);
            } else {
            	mServerSocket = mBTAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_INSECURE, mUuid);
            }
            
            if(mServerSocket == null)
            	throw new CipherConnectErrException(CipherConnectErrException.INFO_SERVER_SKT_ERROR);
                  
        }
        
        private void mCloseServerSocket() {
            Log.d(mTAG, "Socket Type" + mSocketType + "cancel " + this);
            if(mServerSocket != null)
            {
	            try {
	            	mServerSocket.close();
	            } catch (IOException e) {
	                Log.e(mTAG, "Socket Type" + mSocketType + "close() of server failed", e);
	            }
	            finally {
	            	mServerSocket = null;
	            }
            }
        }
        
        private void mCloseSocket() 
        {
            if(mSocket != null)
            {
	            try {
	            	mSocket.close();
	            } catch (IOException e) {
	                Log.e(mTAG, "Socket Type" + mSocketType + "close() of socket failed", e);
	            }
	            finally {
	            	mSocket = null;
	            }
            }
        }
        
        private void mCloseInStream() 
        {
            if(mInStream != null)
            {
	            try {
	            	mInStream.close();
	            } catch (IOException e) {
	                Log.e(mTAG, "Socket Type" + mSocketType + "close() of InputStream failed", e);
	            }
	            finally {
	            	mInStream = null;
	            }
            }
        }
        
        public void StopServer()
        {
        	mCloseServerSocket();
        }

        public void Close()
        {
        	mCloseServerSocket();
        	mCloseInStream();
        	mCloseSocket();
        }
        public void run() 
        {
        	if(mServrState != STATE_ONLINE)
        		return;
        	
        	SetConnected(false);
        	
            Log.d(mTAG, "Socket Type: " + mSocketType + "BEGIN ListenAndConnThread" + this);
            setName("ListenAndConnThread" + mSocketType);
            mCloseInStream();
        	mCloseSocket();
        	
        	CipherConnBTDevice device = new CipherConnBTDevice();
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception            	
            	mSocket = mServerSocket.accept();
            	if (mSocket == null) 
            		throw new CipherConnectErrException(CipherConnectErrException.INFO_SKT_ERROR);
            	// Always close BluetoothServerSocket immediately after accepting a connected socket.
            	mCloseServerSocket();
            	
            	BluetoothDevice remoteDevice= mSocket.getRemoteDevice();
            	if(remoteDevice == null) 
            		throw new CipherConnectErrException(CipherConnectErrException.INFO_BTDEVICE_NONE);
            	device.getParamFromBTDevice(remoteDevice);
                fireCipherBeginConnectControl(device);
                
                mInStream = mSocket.getInputStream();	
                
                fireConnecting(device);
                CipherConnectVerify cverify = new CipherConnectVerify(mSocket);
                if(cverify.verify() == false)
                	throw new CipherConnectErrException(CipherConnectErrException.INFO_NOT_CIPHER_DEVICE);
                
                byte[] buffer = cverify.getTransmitBuffer();
                mProcessBarcode(buffer, device);
                buffer = null;
                
                SetConnected(true);
                fireConnected(device);
                
                buffer = new byte[1024];
                // Keep listening to the InputStream while connected
                while (true) 
                {        	
                	Arrays.fill(buffer, (byte) '\0');
                    // Read from the InputStream
                	mInStream.read(buffer);
                	mProcessBarcode(buffer, device);
                }
            } 
            //exception handle
            catch (IOException e) {
                Log.d(mTAG, "Socket Type: " + mSocketType + " IOException", e);
                if(mServrState == STATE_OFFLINE)
                {
                	if(mIsConnected == true) 
	                	fireDisconnected(device);
                	mMainThrdHandler.post(
                			new Runnable()
                			{
                				//@Override 
                				public void run() {
                					fireCipherListenServerOffline();
                				}
                			}
                	);
                }
                else
                {
                	String strMsg = "IOException, unknown message";
                	if(e != null) 
                		strMsg = e.getMessage();
                	
                	fireCipherConnectControlError(device, 0, strMsg);
                }
            } 
            catch (CipherConnectErrException e) {
                Log.d(mTAG, "Socket Type: " + mSocketType + " CipherConnectErrException", e);
                fireCipherConnectControlError(device, e);
            } 
            catch (Exception e) {
                Log.d(mTAG, "Socket Type: " + mSocketType + " Exception occurs");
                fireCipherConnectControlError(device, e);
            } 
            finally {
            	Log.d(mTAG, "Close Listen thread");
            	mCloseInStream();
            	mCloseSocket();
            	if(mServrState == STATE_ONLINE)
            	{
            		mMainThrdHandler.post(
            			new Runnable()
            			{
            				//@Override 
            				public void run()
            				{
            					mFireListenAndConnThread();
            				}
            			}
            		);
            	}
            	SetConnected(false);
            }       
         
            Log.d(mTAG, "END ListenAndConnThread, socket Type: " + mSocketType);
        }
    }
	
	private class AutoConnectThread extends Thread{
		private boolean mIsCancel = true;
		private ICipherConnBTDevice mDevice = null;
		
		public AutoConnectThread(ICipherConnBTDevice device){
			mDevice = device;
		}
		
		public void run(){
			while (mIsCancel) {
				if(isConnected()==false){
						//if(_DEBUG)
						//	Log.d("CipherConnectControl","AutoConnectThread_connect(device_name="+this.mDeviceName+")");
	
						try {
							connect(mDevice);
						} catch (Exception e) {
							Log.e("CipherConnectControl","AutoConnectThread_connect(device_name=" + mDevice.getDeviceName() +")",e);
						}
				}
				try {
					//System.gc();
					sleep(15000);
				} catch (Exception e) {
					Log.e("CipherConnectControl","AutoConnectThread_connect.sleep(10000)",e);
				}
			}
		}
		
		public synchronized void cancel() {
			this.mIsCancel = false;
		}
	}
	
	private class ConnectedThread extends Thread {
	    private BluetoothSocket mBluetoothSocket = null;
	    private InputStream mInputStream = null;
	    private CipherConnBTDevice mDevice = null;
	    private volatile boolean isContinue = true;  // Control terminate thread flag

	    public void terminate() { 
	        isContinue = false; 
	    }
	    
	    public void sendDisconnectCmd()
	    {
	    	if(mBluetoothSocket != null)
	    	{
	    		OutputStream stream = null;
				try {
					stream = this.mBluetoothSocket.getOutputStream();
				} catch (IOException e) {
					Log.e("CipherConnect", "sendDisconnectCmd get the outputStream of BluetoothSocket", e);
					return ;
				}
				if(stream==null)
					return;

				// Set scanner to slave mode
				byte[] buffer = {'#','@','1','0','0','0','0','3','#'};
				String strCmd = "#@109919\r";
				try {
					stream.write(strCmd.getBytes());
					
				} catch (IOException e) {
					Log.e("CipherConnect","sendDisconnectCmd:Can't write to the Device",e);
				}	
	    	}
	    }

	    public ConnectedThread(ICipherConnBTDevice srcDevice){
	    	mDevice = (CipherConnBTDevice) srcDevice;
	    }
	    
	    public void run() 
	    {
	    	
	    	if(BluetoothAdapter.getDefaultAdapter()==null)
	    	{
	    		fireCipherConnectControlError(
	    				mDevice,
	    				CipherConnectControlResource.please_turn_on_Bluetooth_id,
	    				CipherConnectControlResource.please_turn_on_Bluetooth);
	    		mResetConnThrd();
	    		mDevice = null;
	    		return;
	    	}
	    	
	    	BluetoothDevice btDevice = getBtDevice(mDevice.getAddress());
	    	if(btDevice==null){
	    		fireCipherConnectControlError(
	    				mDevice,
	    				CipherConnectControlResource.please_turn_on_Bluetooth_id,
	    				CipherConnectControlResource.please_turn_on_Bluetooth);
	    		mResetConnThrd();
	    		mDevice = null;
	    		return;
	    	}
	    	mDevice.getParamFromBTDevice(btDevice);	// update name and address from OS.
	    	fireCipherBeginConnectControl(mDevice);
	    	fireConnecting(mDevice);
	    	
			try {
				this.mBluetoothSocket = btDevice.createRfcommSocketToServiceRecord(mUuid);
			} 
			catch (Exception e) {
				Log.e("CipherConnectControl", "CipherConnectService.bt_connected:Can't connect to the SocketToServiceRecord",e);
	        	fireCipherConnectControlError(mDevice,0,e.getMessage());
	        	mResetConnThrd();
	        	mDevice = null;
	    		return;
			}
			
			BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
	    	try {
	    		this.mBluetoothSocket.connect();
	    		 //if(_DEBUG)
	    		//	 Log.d("CipherConnectControl", "CipherConnectService.bt_connected:The BluetoothSocket connected.");
	        } 
	    	catch (Exception e) {
	        	Log.e("CipherConnectControl", "CipherConnectService.bt_connected:The BluetoothSocket can't connect.",e);
	        	fireCipherConnectControlError(mDevice,0,e.getMessage());
	        	mResetConnThrd();
	        	mDevice = null;
	            return;
	        }
	    	
	    	try {
				this.mInputStream = this.mBluetoothSocket.getInputStream();
			} 
	    	catch (Exception e) {
	        	Log.e("CipherConnectControl", "CipherConnectService.bt_connected:The BluetoothSocket can't get the InputStream.",e);
	        	this.mInputStream = null;
	        	fireCipherConnectControlError(mDevice,0,e.getMessage());
	        	mResetConnThrd();
	        	try {
					this.mBluetoothSocket.close();
				} catch (Exception e1) {
		        	Log.e("CipherConnectControl", "CipherConnectService.bt_connected:The BluetoothSocket can't close.",e);
				}
	        	mDevice = null;
	        	return;
			}
	    	
	    	byte[] buffer=null;
	    
	    	
			CipherConnectVerify verify = new CipherConnectVerify(this.mBluetoothSocket);
	        try {				
				  if(verify.verify()==false){
				 
					fireCipherConnectControlError(
							mDevice,
							CipherConnectControlResource.the_device_is_not_the_cipherlab_product_id,
							CipherConnectControlResource.the_device_is_not_the_cipherlab_product);
					
				    mResetConnThrd();
				    mDevice = null;
				    return;
				}
			} catch (Exception e) {
				Log.d("CipherConnectControl", e.getMessage());
			}

        	fireConnected(mDevice);
        	
        	buffer = verify.getTransmitBuffer();
        	if(buffer!=null && buffer.length>0)
        	{
        		try {
    				this.processionBarcode(buffer);
    			} catch (UnsupportedEncodingException e1) {
    				Log.d("CipherConnectControl", e1.getMessage());
    			}
        	}
        	buffer=null;

	        while (isContinue) {
	        	buffer = new byte[1024];
            	Arrays.fill(buffer, (byte) '\0');
            	
	        	try {
	        		SetConnected(true);
					this.mInputStream.read(buffer);
					
					//if(_DEBUG)
	            	//	Log.d("CipherConnectControl", "CipherConnectService.ConnectedThread.run:Wait to read data from the InputStream of BluetoothSocket");
				} catch (Exception e) {
					SetConnected(false);
	            	if(this.mInputStream!=null){
	            		try {
	            			this.mInputStream.close();
	            			//if(_DEBUG)
	            			//	Log.d("CipherConnectControl", "CipherConnectService.ConnectedThread.run:The InputStream of BluetoothSocket is close");
						} catch (Exception e2) {
							Log.e("CipherConnectControl", "CipherConnectService.ConnectedThread.run:Can't close the InputStream of BluetoothSocket",e);
						}
            			this.mInputStream = null;
	            	}
	            	fireDisconnected(mDevice);
	            	mResetConnThrd();
	            	mDevice = null;
	            	terminate(); // Terminate thread
	            	
	            	return;
				}
	        	
	        	try {
					this.processionBarcode(buffer);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                buffer = null;
	        }
	    }
	    
		private synchronized void processionBarcode(byte[] buffer) throws UnsupportedEncodingException{
	    	mProcessBarcode(buffer, mDevice);
	    }

	    public synchronized void cancel() {
        	if(this.mBluetoothSocket!=null){
        		try {
					this.mBluetoothSocket.close();
				} 
        		catch (Exception e) {
					Log.e("CipherConnectControl", "CipherConnectService.ConnectedThread.cancel:Can't close the BluetoothSocket",e);
				}
        	}
        	this.mBluetoothSocket = null;
	    }
	}
}
