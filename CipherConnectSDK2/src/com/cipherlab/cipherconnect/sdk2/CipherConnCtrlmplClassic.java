package com.cipherlab.cipherconnect.sdk2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import com.cipherlab.help.ArrayHelper;
import com.cipherlab.help.CipherLog;

public class CipherConnCtrlmplClassic extends CipherConnCtrlmplBase {
	final String mTAG = "CipherConnCtrlmplClassic";
	
	private UUID mUuid;
    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";
    private ListenAndConnThread mListenAndConnThread = null;
    private static Object mLockStreamAndSocket = new Object();
	private static final int STATE_OFFLINE = -1;       // we're doing nothing
	private static final int STATE_ONLINE = 0;       // start to listen connection
	private int mServrState = STATE_OFFLINE; //Only used for server now.
	private Handler mMainThrdHandler = null;
	private boolean mBDisconnect = false;
	private ConnectedThread mConnectThread;
	private boolean mIsConnected = false;
	private String mCurltPageInfo = "";
	
	public CipherConnCtrlmplClassic(Context context){
		super(context);
		
		//well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB.
		this.mUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		mMainThrdHandler = new Handler();
		mResetListenThread();
		if(mLockStreamAndSocket == null)
			mLockStreamAndSocket = new Object();
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
			mStopListenAndConn();
			CipherLog.d(mTAG, "mFireListenAndConnThread, BluetoothAdapter.getDefaultAdapter().isEnabled() == false, return false");
			return false;
		}
		
		mResetListenThread();
		
		try {
			mListenAndConnThread = new ListenAndConnThread(false);
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
		CipherLog.d(mTAG, "mListenAndConnThread.start();");
		CipherLog.d(mTAG, "mServrState = STATE_ONLINE;");
		CipherLog.d(mTAG, "mFireListenAndConnThread end, return true");
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
		mSetCheckConnTimer(false);
		mResetConnThrd();	
		
		return mFireListenAndConnThread();
	}
	
	private void mStopListenAndConn()
	{
		mServrState = STATE_OFFLINE;
		mResetListenThread();
	}
	
	public void reset() {
		SetCipherConnectControlListener(null);
		mAutoConnDevice = null;
		mSetCheckConnTimer(false);
		mResetConnThrd();	//trigger exception in thread.  
		mStopListenAndConn();
		mCurltPageInfo = "";
	}
	
	public void StopListening()
	{
		mServrState = STATE_OFFLINE;
		if(mListenAndConnThread != null)
		{
			//Here will fire offline in thread. 
			mListenAndConnThread.StopServer();
			mListenAndConnThread = null;
		}
	}
	//================ Server functions end=============
	
	public void connect(ICipherConnBTDevice device) throws NullPointerException
	{
		if(mBHasConnection)
    		return;
		
		mBHasConnection = true;
		
		if(device == null || device.getDeviceName() == null)
			throw new NullPointerException();
    	
		setHasConnectionInMainThrd(true);
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
		mBDisconnect = true;
		mAutoConnDevice = null;
		mSetCheckConnTimer(false);
		mResetConnThrd();	//trigger exception in thread.  
		
		if(mListenAndConnThread != null)
			mListenAndConnThread.sendCmdToResetScanner();	
		
		mResetListenThread();
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
		synchronized (CipherConnCtrlmplClassic.class) {
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
	
	//this function is called in worker thread.
	private void mResetConnThrd() {
		if(this.mConnectThread!=null){
    		this.mConnectThread.cancel();
    	}
		this.mConnectThread = null;
		
		if(BluetoothAdapter.getDefaultAdapter()==null)
			return;
		
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		setHasConnectionInMainThrd(false);
	}
	
	private boolean mIsMinimizeCmd(String strReceive)
	{
        final String strMinimizeCmd = "#@KBD_SWITCH"; 
        if(strReceive.contains(strMinimizeCmd))
    	{
    		return true;
    	}
        return false;
	}
	
	private void mProcessBarcode(byte[] buffer, ICipherConnBTDevice device) throws UnsupportedEncodingException{
		CipherLog.d(mTAG, "mProcessBarcode begin, buffer = " + buffer);
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
            	}
            	sb.append(c);
			}
    	}
	
        String barcode = sb.toString();
        
        if(mIsMinimizeCmd(barcode))
    	{
    		fireMinimizeCmd();
    	}
        else 
        {
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
            }	
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
		public static final int INFO_BTDEVICE_VERIFY_NORESP = 4;
		
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
   			case INFO_BTDEVICE_VERIFY_NORESP:
   				strResult = "Bluetooth device has no response";
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
        private OutputStream mOutStream;
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
        	mOutStream = null;
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
        	CipherLog.d(mTAG,"ListenAndConnThread::mCloseServerSocket begin, thread = " + Thread.currentThread().getId());
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
        
        private void mCloseStreamAndSocket() 
        {
        	CipherLog.d(mTAG, "mCloseStreamAndSocket begin, may wait out mLockStreamAndSocket, thread = "+ Thread.currentThread().getId());
        	synchronized(mLockStreamAndSocket)
        	{
        		CipherLog.d(mTAG, "mCloseStreamAndSocket, in lock mLockStreamAndSocket");
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
	
	            if(mOutStream != null)
	            {
		            try {
		            	mOutStream.close();
		            } catch (IOException e) {
		                CipherLog.e(mTAG, "Socket Type" + mSocketType + "close() of OutputStream failed", e);
		            }
		            finally {
		            	mOutStream = null;
		            }
	            }
	            
	        	CipherLog.d(mTAG,"mCloseSocketAndStream begin, thread = " + Thread.currentThread().getId());
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
	            CipherLog.d(mTAG, "mCloseStreamAndSocket, in lock mLockStreamAndSocket end");
        	}
        }
		
        public void sendCmdToResetScanner()
	    {
        	CipherLog.d(mTAG,"sendCmdToResetScanner begin, thread = " + Thread.currentThread().getId());
        	synchronized(mLockStreamAndSocket)
        	{
        		CipherLog.d(mTAG,"sendCmdToResetScanner in mLockStreamAndSocket, thread = " + Thread.currentThread().getId());
        		if(mOutStream == null && mSocket != null)
            	{    	
    				try {
    					mOutStream = mSocket.getOutputStream();
    				} catch (IOException e) {
    					CipherLog.d(mTAG, "IOException occurs in calling mSocket.getOutputStream, will return.");
    					return ;
    				}
         		}
            	
    			if(mOutStream == null)
    			{
    				CipherLog.d(mTAG,"sendCmdToResetScanner, mOutStream == null, will return, thread = " + Thread.currentThread().getId());
    				return;
    			}
            	
            	try {
    	        	String strCmd = "#@109919\r";	//reset connection
    	        	mOutStream.write(strCmd.getBytes());
    	     		
    	     		try {
    	     			Thread.sleep(1000);
    	     		} catch (InterruptedException e) {
    	     			e.printStackTrace();
    	     		}
    	     		
    	     		strCmd = "#@109999\r";	//Re-open scanner
    	     		mOutStream.write(strCmd.getBytes());
    	     		
    	     		try {
    	     			Thread.sleep(1000);
    	     		} catch (InterruptedException e) {
    	     			e.printStackTrace();
    	     		}

    			} catch (IOException e) {
    				CipherLog.d(mTAG, "sendCmdToResetScanner, IOException occurs in calling mOutStream.write, will return");
    			}		
            	CipherLog.d(mTAG,"sendCmdToResetScanner in mLockStreamAndSocket, thread = " + Thread.currentThread().getId() + " end");
        	}        	
	    }
        
        public void StopServer()
        {
        	mCloseServerSocket();
        }

        public void Close()
        {
        	mCloseServerSocket();
        	mCloseStreamAndSocket();
        }
        public void run() 
        {
        	if(mServrState != STATE_ONLINE)
        		return;
        	
        	SetConnected(false);
        	
            CipherLog.d(mTAG, "Socket Type: " + mSocketType + "BEGIN ListenAndConnThread" + this);
            setName("ListenAndConnThread" + mSocketType);
            mCloseStreamAndSocket();
        	
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
                boolean bVer = false;
                try {
					bVer = cverify.verify();
				} catch (TimeoutException e) {
					e.printStackTrace();
					throw new CipherConnectErrException(CipherConnectErrException.INFO_BTDEVICE_VERIFY_NORESP);
				}
                if(bVer == false)
                	throw new CipherConnectErrException(CipherConnectErrException.INFO_NOT_CIPHER_DEVICE);
                mCurltPageInfo = cverify.getListPageInfo();
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
                CipherLog.d(mTAG, "IOException in ListenAndConnThread, Thread id = " + Thread.currentThread().getId() +", Socket Type: " + mSocketType, e);
                if(mIsConnected == true) 
                	fireDisconnected(device);
            } 
            catch (CipherConnectErrException e) {
                CipherLog.d(mTAG, "CipherConnectErrException in ListenAndConnThread, Thread id = " + Thread.currentThread().getId() +", , Socket Type: " + mSocketType , e);
                fireCipherConnectControlError(device, CipherConnectControlResource.exception, e.getMessage());
            } 
            catch (Exception e) {
                CipherLog.d(mTAG, "Exception in ListenAndConnThread, Thread id = " + Thread.currentThread().getId() +", , Socket Type: " + mSocketType);
                fireCipherConnectControlError(device, CipherConnectControlResource.exception, e.getMessage());
            } 
            finally {
        		mCloseStreamAndSocket();
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
            	CipherLog.d(mTAG, "END ListenAndConnThread, socket Type: " + mSocketType);
            }       
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

	    public ConnectedThread(ICipherConnBTDevice srcDevice){
	    	mDevice = (CipherConnBTDevice) srcDevice;
	    }
	    
	    public void run() 
	    {
	    	if(mBAuoReconnect)
    		{
    			mAutoConnDevice = mDevice;
    		}
	    	
	    	if(BluetoothAdapter.getDefaultAdapter()==null)
	    	{
	    		fireCipherConnectControlError(
	    				mDevice,
	    				CipherConnectControlResource.please_turn_on_Bluetooth_id,
	    				CipherConnectControlResource.please_turn_on_Bluetooth);
	    		mResetConnThrd();
	    		mDevice = null;
	    		if(mBAuoReconnect)
	    		{
	    			mSetCheckConnTimer(true);
	    		}
	    		return;
	    	}
	    	
	    	BluetoothDevice btDevice = getBtDevice(mDevice.getAddress());
	    	if(btDevice == null)
	    		btDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mDevice.getAddress());

	    	if(btDevice==null){
	    		fireCipherConnectControlError(
	    				mDevice,
	    				CipherConnectControlResource.bluetooth_connection_error_id,
	    				CipherConnectControlResource.bluetooth_connection_error);
	    		mResetConnThrd();
	    		mDevice = null;
	    		if(mBAuoReconnect)
	    		{
	    			mSetCheckConnTimer(true);
	    		}
	    		return;
	    	}
	    	mDevice.getParamFromBTDevice(btDevice);	// update name and address from OS.
	    	fireCipherBeginConnectControl(mDevice);
	    	fireConnecting(mDevice);
	    	
			try {
				this.mBluetoothSocket = btDevice.createRfcommSocketToServiceRecord(mUuid);
			} 
			catch (Exception e) {
				CipherLog.e("CipherConnectControl", "CipherConnectService.bt_connected:Can't connect to the SocketToServiceRecord",e);
	        	fireCipherConnectControlError(mDevice,0,e.getMessage());
	        	mResetConnThrd();
	        	mDevice = null;
	        	if(mBAuoReconnect)
	    		{
	    			mSetCheckConnTimer(true);
	    		}
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
	        	fireCipherConnectControlError(mDevice,0,e.getMessage());
	        	mResetConnThrd();
	        	mDevice = null;
	        	if(mBAuoReconnect)
	    		{
	    			mSetCheckConnTimer(true);
	    		}
	            return;
	        }
	    	
	    	try {
				this.mInputStream = this.mBluetoothSocket.getInputStream();
			} 
	    	catch (Exception e) {
	        	CipherLog.e("CipherConnectControl", "CipherConnectService.bt_connected:The BluetoothSocket can't get the InputStream.",e);
	        	this.mInputStream = null;
	        	fireCipherConnectControlError(mDevice,0,e.getMessage());
	        	mResetConnThrd();
	        	try {
					this.mBluetoothSocket.close();
				} catch (Exception e1) {
		        	CipherLog.e("CipherConnectControl", "CipherConnectService.bt_connected:The BluetoothSocket can't close.",e);
				}
	        	mDevice = null;
	        	if(mBAuoReconnect)
	    		{
	    			mSetCheckConnTimer(true);
	    		}
	        	return;
			}
	    	
	    	byte[] buffer=null;
	    
	    	
			CipherConnectVerify verify = new CipherConnectVerify(this.mBluetoothSocket);
			boolean bVer = false;
	        try {				
	        	bVer = verify.verify();
			} catch (Exception e) {
				CipherLog.d("CipherConnectControl", e.getMessage());
			}
	        finally {
	        	if(bVer == false)
	        	{
	        		fireCipherConnectControlError(
							mDevice,
							CipherConnectControlResource.the_device_is_not_the_cipherlab_product_id,
							CipherConnectControlResource.the_device_is_not_the_cipherlab_product);
					
				    mResetConnThrd();
				    mDevice = null;
				    if(mBAuoReconnect)
		    		{
		    			mSetCheckConnTimer(true);
		    		}
				    return;
	        	}
	        }
	        
	        mCurltPageInfo = verify.getListPageInfo();
        	fireConnected(mDevice);
        	mSetCheckConnTimer(false);
        	
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
	            	fireDisconnected(mDevice);
	            	mResetConnThrd();
	            	mDevice = null;
	            	if(mBDisconnect == false)
	            	{
	            		if(mBAuoReconnect)
	            			mSetCheckConnTimer(true);
	            	}else
	            	{
	            		mMainThrdHandler.post(new Runnable() {
	            			public void run()
	            			{
	            				mBDisconnect = false;
	            			}
	            		});
	            	}
	            	
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
					CipherLog.e("CipherConnectControl", "CipherConnectService.ConnectedThread.cancel:Can't close the BluetoothSocket",e);
				}
        	}
        	this.mBluetoothSocket = null;
	    }
	}

	@Override
	public String getFWVersion() {
		if(mCurltPageInfo.length() > 0)
		{
			final String strFindPrefix = "Ver:";
			String ver = mCurltPageInfo.substring(mCurltPageInfo.indexOf(strFindPrefix) + strFindPrefix.length());
			ver = ver.substring(0, ver.indexOf("\r"));
			return ver;
		}
			
		return "";
	}
}
