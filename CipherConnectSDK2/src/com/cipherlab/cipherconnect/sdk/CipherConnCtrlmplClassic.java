package com.cipherlab.cipherconnect.sdk;

import java.io.IOException;
import java.io.InputStream;
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

import com.cipherlab.help.ArrayHelper;
import com.cipherlab.help.CipherLog;

public class CipherConnCtrlmplClassic extends CipherConnCtrlmplBase {
	//public static final boolean _DEBUG = false;
	private String mTAG = "CipherConnCtrlmplClassic";
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
	
	protected void fireCipherConnectControlError(String deviceName, CipherConnectErrException e){
		if(mListenerList == null)
			return;
		for (ICipherConnectControlListener l : this.mListenerList) {
			if(l!=null)
			{
				l.onCipherConnectControlError(deviceName, 0, e.getMessage());
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
		CipherLog.d(mTAG, "mFireListenAndConnThread begin");
		if(BluetoothAdapter.getDefaultAdapter().isEnabled() == false)
		{
			CipherLog.d(mTAG, "BluetoothAdapter.getDefaultAdapter().isEnabled() == false");
			StopListening();
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
		
		return mFireListenAndConnThread();
	}
	
	public void StopListening()
	{
		mServrState = STATE_OFFLINE;
		mResetListenThread();
	}
	//================ Server functions end=============
	
	public void setAuotReconnect(boolean enable, String deviceName)throws NullPointerException{
		if(enable){
			if(deviceName==null)
				throw new NullPointerException();
			
			if(this.mAutoConnectThread==null){
				//if(_DEBUG)
				//	CipherLog.d("CipherConnectControl","The AutoConnectThread is opening.");

				this.mAutoConnectThread = new AutoConnectThread(deviceName);
				this.mAutoConnectThread.start();
			}
		}
		else{
			//if(_DEBUG)
			//	CipherLog.d("CipherConnectControl","The AutoConnectThread is closeing.");
			
			if(this.mAutoConnectThread!=null)
				this.mAutoConnectThread.cancel();
			
			this.mAutoConnectThread = null;
		}
	}
	
	public void connect(String deviceName) throws NullPointerException{
		fireCipherBeginConnectControl(deviceName);
		if(deviceName==null)
			throw new NullPointerException();
		
		BluetoothDevice device = null;
		try {
	    	device = this.getBtDevice(deviceName);
	    	if(device==null){
	    		return;
	    	}
		} 
		catch (NullPointerException e) {
			throw e;
		}
		

    	if(this.isConnected()){
			//if(_DEBUG)
			//	CipherLog.d("CipherConnectControl", "CipherConnectService.bt_connected:Can't connect again, because you have already connected");
    		
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

	public void disconnect() {
		if(this.mConnectThread!=null){
    		this.mConnectThread.cancel();
    	}
		this.mConnectThread = null;
		
		if(BluetoothAdapter.getDefaultAdapter()==null)
			return;
		
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
	}

	public String[] getBluetoothDeviceNames() {
		Set<BluetoothDevice> btDericeList = this.getBtDeviceList();
		if(btDericeList==null)
			return null;
		
		if(btDericeList.size()<=0){
			return null;
		}
		String[] deviceNames = new String[btDericeList.size()];
		int i = 0;
		for (BluetoothDevice device : btDericeList) {
			deviceNames[i++]=device.getName();
		}
		return deviceNames;
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
		
		this.mConnectThread.cancel();
		this.mConnectThread = null;
		
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
		if(dericeList==null || dericeList.size()<=0){
			this.fireCipherConnectControlError(
					null, 
					CipherConnectControlResource.can_not_find_any_bluetooth_device_id,
					CipherConnectControlResource.can_not_find_any_bluetooth_device);
			
			return null;
		}
		return dericeList;
	}
	
	private void mProcessBarcode(byte[] buffer, String strDviceName) throws UnsupportedEncodingException{
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
            		//CipherLog.e("CipherConnectControl", "Add a Enter("+sb.toString()+").");
            		
            		break;
            	}
            	sb.append(c);
			}
    	}
	
        String barcode = sb.toString();
		
		StringTokenizer st = new StringTokenizer(barcode,"\n");
        if(st.countTokens()==1){
        	fireReceivingBarcode(strDviceName+"A", barcode);
        }
        else{
        	
        	int count = st.countTokens();
        	for(int i=0;i<count;i++)
        	{
        		String code = (String)st.nextElement();
        		if(i<count-1)
        			fireReceivingBarcode(strDviceName+"B",code +"\n");
        		else
        			fireReceivingBarcode(strDviceName+"C",code);
        		
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
	
	private BluetoothDevice getBtDevice(String deviceName)throws NullPointerException{
		if(deviceName==null)
			throw new NullPointerException();
		
    	Set<BluetoothDevice> dericeList = this.getBtDeviceList();
    	if(dericeList==null || dericeList.size()==0){
    		return null;
    	}
    	
    	for (BluetoothDevice device : dericeList) {
    		if(deviceName.equals(device.getName()))
    			return device;
		}

    	this.fireCipherConnectControlError(
				null, 
				CipherConnectControlResource.can_not_find_id,
				CipherConnectControlResource.can_not_find + " " + deviceName);
    	
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
   				strResult = "Get no response from device";
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
            CipherLog.d(mTAG, "Socket Type" + mSocketType + "cancel " + this);
            if(mServerSocket != null)
            {
	            try {
	            	mServerSocket.close();
	            } catch (IOException e) {
	                CipherLog.e(mTAG, "Socket Type" + mSocketType + "close() of server failed", e);
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
	                CipherLog.e(mTAG, "Socket Type" + mSocketType + "close() of socket failed", e);
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
	                CipherLog.e(mTAG, "Socket Type" + mSocketType + "close() of InputStream failed", e);
	            }
	            finally {
	            	mInStream = null;
	            }
            }
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
        	
        	mIsConnected = false;
        	
            CipherLog.d(mTAG, "Socket Type: " + mSocketType + "BEGIN ListenAndConnThread" + this);
            setName("ListenAndConnThread" + mSocketType);
            mCloseInStream();
        	mCloseSocket();

        	String strRemoteDevice = "unknown device name";		
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
                
            	strRemoteDevice = remoteDevice.getName();
                fireCipherBeginConnectControl(strRemoteDevice);
                
                mInStream = mSocket.getInputStream();	
                
                fireConnecting(strRemoteDevice);
                CipherConnectVerify cverify = new CipherConnectVerify(mSocket);
                if(cverify.verify() == false)
                	throw new CipherConnectErrException(CipherConnectErrException.INFO_NOT_CIPHER_DEVICE);
                
                byte[] buffer = cverify.getTransmitBuffer();
                mProcessBarcode(buffer, strRemoteDevice);
                buffer = null;
                
                fireConnected(strRemoteDevice);
                mIsConnected = true;
                
                buffer = new byte[1024];
                // Keep listening to the InputStream while connected
                while (true) 
                {        	
                	Arrays.fill(buffer, (byte) '\0');
                    // Read from the InputStream
                	mInStream.read(buffer);
                	mProcessBarcode(buffer, strRemoteDevice);
                }
            } 
            //exception handle
            catch (IOException e) {
                CipherLog.d(mTAG, "Socket Type: " + mSocketType + " IOException", e);
                if(mServrState == STATE_OFFLINE)
                {
                	if(mIsConnected == true) 
	                	fireDisconnected(strRemoteDevice);
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
                	
                	fireCipherConnectControlError(strRemoteDevice, 0, strMsg);
                }
            } 
            catch (CipherConnectErrException e) {
                CipherLog.d(mTAG, "Socket Type: " + mSocketType + " CipherConnectErrException", e);
                fireCipherConnectControlError(strRemoteDevice, e);
            } 
            finally {
            	CipherLog.d(mTAG, "Close Listen thread");
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
            }       
         
            CipherLog.d(mTAG, "END ListenAndConnThread, socket Type: " + mSocketType);
        }
    }
	
	private class AutoConnectThread extends Thread{
		private boolean mIsCancel = true;
		private String mDeviceName = "";
		
		public AutoConnectThread(String device_name){
			this.mDeviceName = device_name;
		}
		
		public void run(){
			while (mIsCancel) {
				if(isConnected()==false){
						//if(_DEBUG)
						//	CipherLog.d("CipherConnectControl","AutoConnectThread_connect(device_name="+this.mDeviceName+")");
	
						try {
							connect(this.mDeviceName);
						} catch (Exception e) {
							CipherLog.e("CipherConnectControl","AutoConnectThread_connect(device_name="+this.mDeviceName+")",e);
						}
				}
				try {
					//System.gc();
					sleep(15000);
				} catch (Exception e) {
					CipherLog.e("CipherConnectControl","AutoConnectThread_connect.sleep(10000)",e);
				}
			}
		}
		
		public synchronized void cancel() {
			this.mIsCancel = false;
		}
	}
	
	private class ConnectedThread extends Thread {
	    private BluetoothSocket mBluetoothSocket = null;
	    private BluetoothDevice mDevice = null;
	    private InputStream mInputStream = null;
	    private String mDeviceName = null;
	    private volatile boolean isContinue = true;  // Control terminate thread flag

	    public void terminate() { 
	        isContinue = false; 
	    } 

	    public ConnectedThread(BluetoothDevice device){
	    	this.mDevice = device;
	    	try {
		    	if(this.mDevice!=null)
		    		this.mDeviceName = this.mDevice.getName();
			} 
	    	catch (Exception e) {
	    		System.out.println("e="+e);
				//this.mDeviceName = "";
			}
	    }
	    
	    public void run() {
	    	
	    	if(BluetoothAdapter.getDefaultAdapter()==null)
	    		return;
	    	
	    	fireConnecting(this.mDeviceName);
	    	
	    	if(BluetoothAdapter.getDefaultAdapter().isEnabled()==false){
	    		fireCipherConnectControlError(
	    				this.mDeviceName,
	    				CipherConnectControlResource.please_turn_on_Bluetooth_id,
	    				CipherConnectControlResource.please_turn_on_Bluetooth);
				return;
			}
	    	
			try {
				this.mBluetoothSocket = this.mDevice.createRfcommSocketToServiceRecord(mUuid);
			} 
			catch (Exception e) {
				CipherLog.e("CipherConnectControl", "CipherConnectService.bt_connected:Can't connect to the SocketToServiceRecord",e);
	        	disconnect();
	        	fireCipherConnectControlError(this.mDeviceName,0,e.getMessage());
	    		return;
			}
			
			BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
	    	try {
	    		this.mBluetoothSocket.connect();
	    		 //if(_DEBUG)
	    		//	 CipherLog.d("CipherConnectControl", "CipherConnectService.bt_connected:The BluetoothSocket connected.");
	        } 
	    	catch (Exception e) {
	        	CipherLog.e("CipherConnectControl", "CipherConnectService.bt_connected:The BluetoothSocket can't connect.",e);
	        	disconnect();
	        	fireCipherConnectControlError(this.mDeviceName,0,e.getMessage());
	            return;
	        }
	    	
	    	try {
				this.mInputStream = this.mBluetoothSocket.getInputStream();
			} 
	    	catch (Exception e) {
	        	CipherLog.e("CipherConnectControl", "CipherConnectService.bt_connected:The BluetoothSocket can't get the InputStream.",e);
	        	this.mInputStream = null;
	        	disconnect();
	        	fireCipherConnectControlError(this.mDeviceName,0,e.getMessage());
	        	try {
					this.mBluetoothSocket.close();
				} catch (Exception e1) {
		        	CipherLog.e("CipherConnectControl", "CipherConnectService.bt_connected:The BluetoothSocket can't close.",e);
				}
				
	        	return;
			}
	    	
	    	byte[] buffer=null;
	    	
	    	/*
	        byte[] buffer = this.getTransmitBuffer();
			
	        try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			*/
	    	
			CipherConnectVerify verify = new CipherConnectVerify(this.mBluetoothSocket);
	        try {				
				  if(verify.verify()==false){
				 
					fireCipherConnectControlError(
							this.mDeviceName,
							CipherConnectControlResource.the_device_is_not_the_cipherlab_product_id,
							CipherConnectControlResource.the_device_is_not_the_cipherlab_product);
					
				    disconnect();
				    return;
				}
			} catch (Exception e) {
				CipherLog.d("CipherConnectControl", e.getMessage());
			}

        	fireConnected(this.mDeviceName);
        	
        	buffer = verify.getTransmitBuffer();
        	if(buffer!=null && buffer.length>0)
        	{
        		try {
    				this.processionBarcode(buffer);
    			} catch (UnsupportedEncodingException e1) {
    				CipherLog.d("CipherConnectControl", e1.getMessage());
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
	            	//	CipherLog.d("CipherConnectControl", "CipherConnectService.ConnectedThread.run:Wait to read data from the InputStream of BluetoothSocket");
				} catch (Exception e) {
					SetConnected(false);
	            	if(this.mInputStream!=null){
	            		try {
	            			this.mInputStream.close();
	            			//if(_DEBUG)
	            			//	CipherLog.d("CipherConnectControl", "CipherConnectService.ConnectedThread.run:The InputStream of BluetoothSocket is close");
						} catch (Exception e2) {
							CipherLog.e("CipherConnectControl", "CipherConnectService.ConnectedThread.run:Can't close the InputStream of BluetoothSocket",e);
						}
            			this.mInputStream = null;
	            	}
	            	fireDisconnected(this.mDeviceName);
	            	mConnectThread = null;
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
	    
//	    private synchronized byte[] getAllTransmitBuffer()
//	    {
//	    	if(this.mInputStream==null)
//	    		return null;
//	    	
//	    	boolean b = true;
//	    	byte[] out_data = null;
//	    	while (b==true) {
//	    		byte[] data = this.getTransmitBuffer();
//	    		if(data==null || data.length<=0)
//	    			b = false;
//	    		else
//	    			ArrayHelper.append(out_data,data);
//			}
//	    	
//			return out_data;
//	    }
	    /*
	    private synchronized byte[] getTransmitBuffer()
	    {
	    	if(this.mInputStream==null)
	    		return null;
	    	
	    	byte[] data = new byte[5120];
			try {
				Thread.sleep(500);
				this.mInputStream.read(data);
				Thread.sleep(1500);
			} 
			catch (Exception e) {
				CipherLog.e("CipherConnect","CipherConnectService.getTransmitBuffer:Can't recv TransmitBuffer from the Device",e);
				data = null;
				//System.gc();
				return null;
			}
			
			return data;
	    }
		*/
		private synchronized void processionBarcode(byte[] buffer) throws UnsupportedEncodingException{
	    	mProcessBarcode(buffer, mDeviceName);
	    }

	    public synchronized void cancel() {
        	if(this.mBluetoothSocket!=null){
        		try {
					this.mBluetoothSocket.close();
				} 
        		catch (Exception e) {
					CipherLog.e("CipherConnectControl", "CipherConnectService.ConnectedThread.cancel:Can't close the BluetoothSocket",e);
				}
        	}
        	this.mBluetoothSocket = null;
        	this.mDevice = null;
	    }
	}
}
