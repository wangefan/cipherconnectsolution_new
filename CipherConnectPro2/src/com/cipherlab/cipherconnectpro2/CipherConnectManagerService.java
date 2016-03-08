package com.cipherlab.cipherconnectpro2;

import java.util.ArrayList;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.cipherlab.cipherconnect.sdk2.CipherConnCtrl2EZMet;
import com.cipherlab.cipherconnect.sdk2.ICipherConnBTDevice;
import com.cipherlab.cipherconnect.sdk2.ICipherConnCtrl2EZMet;
import com.cipherlab.cipherconnect.sdk2.ICipherConnectControl2Listener;
import com.cipherlab.cipherconnectpro2.ICipherConnectManagerService.CONN_STATE;
import com.cipherlab.cipherconnectpro2.R;
import com.cipherlab.help.CipherLog;
import com.cipherlab.util.NotificationUtil;

public class CipherConnectManagerService extends Service 
{
	final String mTAG = "CipherConnectManagerService";

    private CONN_STATE mConnState = CONN_STATE.CONN_STATE_DISCONNECT;
    private ICipherConnBTDevice mDevice = null;
        
	//broadcast actions, broadcast to ConnectStatus_onPreferenceChange now.
    public static final String ACTION_CONN_STATE_CHANGED =
            "com.cipherEZMet.cipherconnectpro2.CipherConnectManagerService.ConnectionState_CHANGED";
    public static final String ACTION_COMMAND =
            "com.cipherEZMet.cipherconnectpro2.CipherConnectManagerService.Connection_Command";
    
	private static final String TAG = "CipherConnectManagerService()";
	public static ICipherConnCtrl2EZMet mCipherConnectControl;
	private ArrayList<ICipherConnectManagerListener> mListenerList = 
            new ArrayList<ICipherConnectManagerListener>();
    private LocalBinder mBinder;
    
    public static class connDisconnectBDRec extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if (mCipherConnectControl != null && mCipherConnectControl.isConnected())
	        	mCipherConnectControl.disconnect();
        }
    } 
    
    private BroadcastReceiver mBTActReceiver = new BroadcastReceiver()
	{
		@Override
        public void onReceive(Context context, Intent intent) 
		{
        	final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                               BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) 
                {
                	CipherLog.d(mTAG, "BroadcastReceiver, receive bluetooth on");
                	btSetUpForBluetooth();
                } 
                else if (state == BluetoothAdapter.STATE_OFF) 
                {
                	btResetForBluetooth();
                	CipherLog.d(mTAG, "BroadcastReceiver, receive bluetooth off");
                }
            }
		}
	};
    
    @Override
    public void onCreate() {
    	CipherLog.d(TAG, "onCreate(): begin");
    	mBinder = new LocalBinder();
    	CipherConnectWakeLock.initial(this);
        CipherConnectControl_init();
        startForeground(NotificationUtil.NOTIFY_ID, NotificationUtil.GetNotificaion(R.drawable.noconnect, this, 
        										getResources().getString(R.string.ime_name), 
        										getResources().getString(R.string.setting_bluetooth_device_disconnected),
        		                                      CipherConnectNotification.intent_cipherconnectproSettings(),
        		                                      getPackageName(),
        		                                      false));
        
        final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(mBTActReceiver, intentFilter);
        super.onCreate();
        CipherLog.d(TAG, "onCreate(): end");
    }
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		CipherLog.d(TAG, "onStartCommand called");
		return START_NOT_STICKY;	//means not re-create service if no call startService explicitly
	}

    @Override
    public void onDestroy() {
    	CipherLog.d(TAG, "onDestroy(): begin");
        super.onDestroy();
        
        if (mCipherConnectControl.isConnected())
        	mCipherConnectControl.disconnect();
        
        this.mBinder = null;
        if(mCipherConnectControl != null)
        {
        	mCipherConnectControl.close();
            mCipherConnectControl = null;	
        }

        CipherConnectSettingInfo.destroy();
        stopForeground(true);
        unregisterReceiver(mBTActReceiver);
        CipherLog.d(TAG, "onDestroy(): end");
    }
    
    @Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
    
    public class LocalBinder extends Binder implements ICipherConnectManagerService 
    {
		public ICipherConnectManagerService getService() {
			return (ICipherConnectManagerService) LocalBinder.this;
		}
		
		public void setUpForBluetooth() {
			btSetUpForBluetooth();
		}
		
		//Server service begin
    	public boolean StartListenConn() {
    		return bt_StartListenConn();
    	}
    	
    	public void StopListenConn() {
    		bt_StopListenConn();
    	}
    	
    	public ICipherConnBTDevice GetConnDevice() {
    		return bt_GetConnDevice();
    	}
    	      	    
    	public CONN_STATE GetConnState() {
    		return bt_GetConnState();
    	}

    	public Bitmap GetMacAddrBarcodeImage(int nWidth, int nHeight) {
    		return bt_GetMacAddrBarcodeImage(nWidth, nHeight);
    	}
    		
    	public Bitmap GetResetConnBarcodeImage(int nWidth, int nHeight){
    		return bt_GetResetConnBarcodeImage(nWidth, nHeight);
    	}
    		
    	public Bitmap GetSettingConnBarcodeImage(int nWidth, int nHeight) {
    		return bt_GetSettingConnBarcodeImage(nWidth, nHeight);
    	}
    	
    	public Bitmap GetSettingConnQRcodeImage(int nWidth, int nHeight)
    	{
    		return bt_GetSettingConnQRCodeImage(nWidth, nHeight);
    	}
    	//Server service end
    	
    	public Bitmap GetEnableAuthBarcodeImage(int nWidth, int nHeight)
    	{
    		return bt_GetEnableAuthBarcodeImage(nWidth, nHeight);
    	}
    	
    	public Bitmap GetDisableAuthBarcodeImage(int nWidth, int nHeight)
    	{
    		return bt_GetDisableAuthBarcodeImage(nWidth, nHeight);
    	}
    	
    	public Bitmap GetEnableSppBarcodeImage(int nWidth, int nHeight)
    	{
    		return bt_GetEnableSppBarcodeImage(nWidth, nHeight);
    	}
    	
		public boolean connect(ICipherConnBTDevice device) throws Exception {
			return bt_connect(device);
		}
		
		public boolean connect(String deviceName, String deviceAddr)throws Exception {
			return bt_connect(deviceName, deviceAddr);
		}
		
		public void disConnect() {
			if (mCipherConnectControl.isConnected())
	        	mCipherConnectControl.disconnect();
		}
		
		public ICipherConnBTDevice[] getBtDevices() {
			return mCipherConnectControl.getBtDevices();
		}
		
		public boolean isConnected() {
			return mCipherConnectControl.isConnected();
		}
		
        public void AddListener(ICipherConnectManagerListener l) {
            bt_AddListener(l);
        }
        
        public void RemoveListener(ICipherConnectManagerListener l) {
            bt_RemoveListener(l);
        }

        public void setAutoConnect(boolean enable) {
			bt_setAutoConnect(enable);
		}
		
		public boolean isAuotConnect() {
			return mCipherConnectControl.isAutoReconnect();
		}
		
		public void stopSelf() {
			bt_stopSelf();
		}
		
	    public boolean IsBLEModeSupported() {
	    	return mCipherConnectControl.IsBLEModeSupported();
	    }
	    
	    public void SetBLEMode(boolean bEnable) {
	    	mCipherConnectControl.SetBLEMode(bEnable);
	    }
	    
	    public boolean StartScanLEDevices() throws UnsupportedOperationException {
	    	return mCipherConnectControl.StartScanLEDevices();
	    }
	    
	    public boolean StopScanLEDevices() throws UnsupportedOperationException {
	    	return mCipherConnectControl.StopScanLEDevices();
	    }

		@Override
		public String getFWVersion() {
			return mCipherConnectControl.getFWVersion();
		}
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: mBroadcastConnChange()
     * @Description: Broadcast the changing action to client.
     * Client should use Servic.GetConnState() to get status
     * @param: nConnState, defined as connection staus.
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void mBroadcastConnChange(CONN_STATE connState)
	{
		// Client should use Servic.GetConnState() to get status
		mConnState = connState;
		final Intent brdConnState = new Intent(ACTION_CONN_STATE_CHANGED);
        sendBroadcast(brdConnState);
	}
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: mBroadcastConnChangeWithInfo()
     * @Description: Broadcast the changing action to client with info.
     * Client should use getExtra to get info.
     * @param: nConnState, defined as connection staus.
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    public final static String ACTION_CONN_STATE_CHANGED_KEY = "ACTION_CONN_STATE_CHANGED_KEY";
    private void mBroadcastConnChangeWithInfo(CONN_STATE connState, String message)
	{
		// Client should use Servic.GetConnState() to get status
		mConnState = connState;
		final Intent brdConnState = new Intent(ACTION_CONN_STATE_CHANGED);
		brdConnState.putExtra(ACTION_CONN_STATE_CHANGED_KEY, message);
        sendBroadcast(brdConnState);
	}
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: mBroadcastCommand()
     * @Description: Broadcast the special command to client.
     * Client should use Servic.GetConnState() to get status
     * @param: nConnState, defined as connection staus.
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void mBroadcastCommand()
    {
    	final Intent brdConnState = new Intent(ACTION_COMMAND);
        sendBroadcast(brdConnState);
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: btSetUpForBluetooth()
     * @Description: set up for bluetooth relative functions.
     *  
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    public void btSetUpForBluetooth()
    {
    	CipherLog.d(TAG, "btSetUpForBluetooth begin");
    	String strCurBTMode = CipherConnectSettingInfo.getBTMode(this);
    	if(0 == strCurBTMode.compareTo(this.getResources().getString(R.string.Str_BT_Classic)))
    	{
    		mCipherConnectControl.SetBLEMode(false);
    	}
    	else if(0 == strCurBTMode.compareTo(this.getResources().getString(R.string.Str_BT_LE))) {
    		mCipherConnectControl.SetBLEMode(true);
    	}
    	if(false == CipherConnectSettingInfo.getBCMode(this).equals(CipherConnectSettingInfo.MASTER))
    	{
    		bt_StartListenConn();
    	}
    	CipherLog.d(TAG, "btSetUpForBluetooth end");
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: btResetForBluetooth()
     * @Description: reset for bluetooth relative functions.
     *  
     * <!----------------------------------------------------------------->
     * */
    public void btResetForBluetooth()
    {
    	CipherLog.d(TAG, "btResetForBluetooth begin");
    	if(mCipherConnectControl != null)
    	{
    		mCipherConnectControl.reset();
    	}
    	CipherLog.d(TAG, "btResetForBluetooth end");
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: CipherConnectControl_init()
     * @Description: Initial CipherConnectControl callback SDK.
     *  
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void CipherConnectControl_init()
    {
    	mCipherConnectControl = new CipherConnCtrl2EZMet(this);
		mCipherConnectControl.addCipherConnect2Listener(new ICipherConnectControl2Listener() 
		{
    		public void onBeginConnecting(ICipherConnBTDevice device) {
    			CipherConnectControl_onBeginConnecting(device);
    		}
    		public void onConnecting(ICipherConnBTDevice device) {
    			CipherConnectControl_onConnecting(device);
    		}
    		public void onConnected(ICipherConnBTDevice device) {				
				CipherConnectControl_onConnected(device);
			}
    		public void onDisconnected(ICipherConnBTDevice device) {
				CipherConnectControl_onDisconnected(device);
			}
    		public void onCipherConnectControlError(ICipherConnBTDevice device, int id, String message) {				
				CipherConnectControl_onCipherConnectControlError(device, id, message);
			}
			public void onReceivingBarcode(ICipherConnBTDevice device, String barcode) {
				CipherConnectControl_onReceivingBarcode(device,barcode);
			}
			public void onMinimizeCmd() {
				for (ICipherConnectManagerListener listener :mListenerList)
					listener.onMinimizeCmd();
				mBroadcastCommand();
			}
			public void onGetLEDevice(final ICipherConnBTDevice device) {
				for (ICipherConnectManagerListener listener :mListenerList)
					listener.onGetLEDevice(device);
			}
    	});
		btSetUpForBluetooth();
    }
    
    public void CipherConnectControl_onDisconnected(ICipherConnBTDevice device) 
    {
    	String message = "";
    	if(device != null)
		{
    		message = device.getDeviceName()
    				+ " "
					+ this.getResources().getString(R.string.the_bluetooth_device_disconnected);
		}
		
		CipherConnectWakeLock.disable();
		CipherConnectNotification.error_notify(CipherConnectManagerService.this, 
												CipherConnectNotification.intent_cipherconnectproSettings(),
												this.getResources().getString(R.string.ime_name), message);
		
		mBroadcastConnChange(CONN_STATE.CONN_STATE_DISCONNECT);
	}
    
    public void CipherConnectControl_onBeginConnecting(ICipherConnBTDevice device) 
    {
		String message = this.getResources().getString(R.string.the_bluetooth_device_beginConnecting);
		CipherConnectNotification.connecting_notify(CipherConnectManagerService.this, 
													CipherConnectNotification.intent_cipherconnectproSettings(),
													this.getResources().getString(R.string.ime_name), message);
		mBroadcastConnChange(CONN_STATE.CONN_STATE_BEGINCONNECTING);
	}
	
    public void CipherConnectControl_onConnecting(ICipherConnBTDevice device) 
    {
		//CipherLog.d(TAG, "CipherConnectControl_onConnecting("+deviceName);
    	mDevice = device;
		String message = this.getResources().getString(R.string.the_bluetooth_device_connecting);
		CipherConnectNotification.connecting_notify(CipherConnectManagerService.this, 
													CipherConnectNotification.intent_cipherconnectproSettings(),
													this.getResources().getString(R.string.ime_name), message);
		mBroadcastConnChange(CONN_STATE.CONN_STATE_CONNECTING);
	}

    public void CipherConnectControl_onConnected(ICipherConnBTDevice device) 
    {
    	//CipherLog.d(TAG, "CipherConnectControl_onConnected("+deviceName);
    	mDevice = device;
        String message = device.getDeviceName()
			            + " "
			            + this.getResources().getString(
			                R.string.the_bluetooth_device_connected);
		
        if(true == CipherConnectSettingInfo.isSuspendBacklight(this))
        {
        	CipherConnectWakeLock.enable();
        }
        
        CipherConnectNotification.notify(CipherConnectManagerService.this,
										CipherConnectNotification.intent_cipherconnectproSettings(),
			                            this.getResources().getString(R.string.ime_name), message, true);
        mBroadcastConnChange(CONN_STATE.CONN_STATE_CONNECTED);
	}
	
    public void CipherConnectControl_onCipherConnectControlError(ICipherConnBTDevice device, int id,	String message)
    {
    	String error_message = this.getResources().getString(R.string.the_bluetooth_device_connected_error);
		CipherConnectNotification.error_notify(CipherConnectManagerService.this,
												CipherConnectNotification.intent_cipherconnectproSettings(),
												this.getResources().getString(R.string.ime_name), error_message);
		mBroadcastConnChangeWithInfo(CONN_STATE.CONN_STATE_CONNECTERR, message);
	}
	
	public void CipherConnectControl_onReceivingBarcode(ICipherConnBTDevice device, String barcode) 
	{
		for (ICipherConnectManagerListener listener :mListenerList)
			listener.onBarcode(barcode);
	}
	

	/*
     * <!----------------------------------------------------------------->
     * @Name: bt_stopSelf()
     * @Description: Set stop self for auto connect and disconnect command.
     *  
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void bt_stopSelf() {

		if (mCipherConnectControl.isConnected())
				mCipherConnectControl.disconnect();
		else
            this.sys_stopSelf();
    }

    /*
     * <!----------------------------------------------------------------->
     * @Name: sys_stopSelf()
     * @Description: Send stop self message to notify.
     *  
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void sys_stopSelf() {
        if (!mCipherConnectControl.isAutoReconnect())
            super.stopSelf();

        CipherConnectNotification.cancel_notify(this);
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: bt_AddListener()
     * @Description: add listener.
     *  
     * @param: ICipherConnectManagerListener l
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void bt_AddListener(ICipherConnectManagerListener l) {
        this.mListenerList.add(l);
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: bt_RemoveListener()
     * @Description: Remove listener.
     *  
     * @param: ICipherConnectManagerListener l
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void bt_RemoveListener(ICipherConnectManagerListener l) {
        this.mListenerList.remove(l);
    }

    /*
     * <!----------------------------------------------------------------->
     * @Name: bt_connect()
     * @Description: Set bluetooth connect command.
     *  
     * @param: ICipherConnBTDevice device
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    public boolean bt_connect(ICipherConnBTDevice device) {
    	try {
    		//Toast.makeText(getApplicationContext(), "bt_connect(deviceName="+deviceName+")", Toast.LENGTH_SHORT).show();
    		CipherLog.d(TAG, "bt_connect(): deviceName= "+ device.getDeviceName());
    		mCipherConnectControl.connect(device);
    		
    		return true;
    	}
    	catch (Exception e) {
    		Toast.makeText(getApplicationContext(), "Can't be set Connect.["+e.getMessage()+"]", Toast.LENGTH_SHORT).show();
    		
    		return false;
    	}
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: bt_connect()
     * @Description: Set bluetooth connect command.
     *  
     * @param: device MAC address. 
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    public boolean bt_connect(String deviceName, String deviceAddr) {
    	try {
    		
    		CipherLog.d(TAG, "bt_connect(): deviceName= " + deviceName);
    		mCipherConnectControl.connect(deviceName, deviceAddr);
    		
    		return true;
    	}
    	catch (Exception e) {
    		Toast.makeText(getApplicationContext(), "Can't be set Connect.["+e.getMessage()+"]", Toast.LENGTH_SHORT).show();
    		
    		return false;
    	}
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: bt_setAutoConnect()
     * @Description: Set auto connect.
     *  
     * @param: boolean enable
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    public synchronized void bt_setAutoConnect(boolean enable) {
    	CipherLog.d(this.getResources().getString(R.string.ime_name), "The AutoConnectis: "+ enable);
    	
    	mCipherConnectControl.setAutoReconnect(enable);
    }
    
    private boolean bt_StartListenConn()
	{
		if(mCipherConnectControl != null)
			return mCipherConnectControl.StartListening();
		return false;
	}
    
    private void bt_StopListenConn()
	{
		if(mCipherConnectControl != null)
			mCipherConnectControl.StopListening();
	}
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: bt_GetConnDevice()
     * @Description: Get connected device.
     * 
     * return: device 
     * <!----------------------------------------------------------------->
     * */
    private ICipherConnBTDevice bt_GetConnDevice()
    {
    	return mDevice;
    }
    
    //Functions
    private CONN_STATE bt_GetConnState()
  	{
  		return mConnState;
  	}
    
    private Bitmap bt_GetMacAddrBarcodeImage(int nWidth, int nHeight)
	{
		if(mCipherConnectControl != null)
			return mCipherConnectControl.GetMacAddrBarcodeImage(nWidth, nHeight);
		return null;
	}
	
    private Bitmap bt_GetResetConnBarcodeImage(int nWidth, int nHeight)
	{
		if(mCipherConnectControl != null)
			return mCipherConnectControl.GetResetConnBarcodeImage(nWidth, nHeight);
		return null;
	}
	
    private Bitmap bt_GetSettingConnBarcodeImage(int nWidth, int nHeight)
	{
		if(mCipherConnectControl != null)
			return mCipherConnectControl.GetSettingConnBarcodeImage(nWidth, nHeight);
		return null;
	}
    
    private Bitmap bt_GetSettingConnQRCodeImage(int nWidth, int nHeight)
	{
		if(mCipherConnectControl != null)
			return mCipherConnectControl.GetSettingConnQRCodeImage(nWidth, nHeight);
		return null;
	}
    
    private Bitmap bt_GetEnableAuthBarcodeImage(int nWidth, int nHeight)
	{
		if(mCipherConnectControl != null)
			return mCipherConnectControl.GetEnableAuthBarcodeImage(nWidth, nHeight);
		return null;
	}
    
    private Bitmap bt_GetDisableAuthBarcodeImage(int nWidth, int nHeight)
	{
		if(mCipherConnectControl != null)
			return mCipherConnectControl.GetDisableAuthBarcodeImage(nWidth, nHeight);
		return null;
	}
    
    private Bitmap bt_GetEnableSppBarcodeImage(int nWidth, int nHeight)
	{
		if(mCipherConnectControl != null)
			return mCipherConnectControl.GetEnableSppBarcodeImage(nWidth, nHeight);
		return null;
	}
}
