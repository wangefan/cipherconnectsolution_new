package com.cipherlab.cipherconnect.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CipherConnCtrlmplBLE extends CipherConnCtrlmplBase {
	final private static UUID mSUUIDString = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
	final private static UUID mCUUIDString = UUID.fromString("cc330a40-fb09-11e1-a84d-0002a5d5c51b");
	
	private enum ConnStatus { CONN_STATE_IDLE, CONN_STATE_CONNECTING, CONN_STATE_CONNECTED} 
    
	//Data members
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothGatt mBluetoothGatt = null;
	private BluetoothGattCharacteristic mBTCharct = null;
	private ArrayList<ICipherConnBTDevice> mbtDericeList = null;
	private ICipherConnBTDevice mDestBTLEDevice = null;
	private ConnStatus mConnStatus = ConnStatus.CONN_STATE_IDLE;
	
	private Handler mHandlerConnTimeout = new Handler();
	private synchronized void mSetConnectedStatus(ConnStatus connStatus) {
		mConnStatus = connStatus;
	}
	
	private synchronized boolean mIsConnected() {
		return mConnStatus != ConnStatus.CONN_STATE_IDLE;
	}
	private void mResetCharacteristic() {
		if(mBluetoothGatt != null && mBTCharct != null)
			mBluetoothGatt.setCharacteristicNotification(mBTCharct, false);
		mBTCharct = null;
	}
	
	private void mDisconnectFromWorkerThread() {
		mMainThrdHandler.post(new Runnable(){
		    @Override
		    public void run() {
		    	mDisconnect();
		    }            
		});
	}
	
	private void mDisconnect() {
		if(mIsConnected()) {
			if (mBluetoothGatt != null) {
				mResetCharacteristic();
		        mBluetoothGatt.close();
		        mBluetoothGatt = null;
		        mHandlerConnTimeout.removeCallbacksAndMessages(null);
	        }
	        
	        mSetConnectedStatus(ConnStatus.CONN_STATE_IDLE);
		}
	}
	
	//default constructor
	public CipherConnCtrlmplBLE(Context context) {
		super(context);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mbtDericeList = new ArrayList<ICipherConnBTDevice>();
	}
	
	//================ Server functions begin=============
		public boolean StartListening()
		{
			//Todo:Implement
			return false;
		}
		
		public void StopListening()
		{
			//Todo:Implement
		}
		//================ Server functions end=============
	
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) 
        {
        	// check if has device already?
        	boolean bAdd = true; 
        	for(int idxCBTDevice = 0; idxCBTDevice < mbtDericeList.size(); ++idxCBTDevice) {
        		ICipherConnBTDevice cBTDevice = mbtDericeList.get(idxCBTDevice);
				String strSrcDevice = cBTDevice.getAddress();
				if(true == device.getAddress().equals(strSrcDevice)) {
					bAdd = false;
					break;
				}
			}
        	
        	if(bAdd) 
        	{
        		String name = device.getName();
        		String add = device.getAddress();
        		if(name != null && add != null)
        		{
            		CipherConnBTDevice cBTDeivce = new CipherConnBTDevice(name, add);
    				mbtDericeList.add(cBTDeivce);
    				
    				//fire to listener.
    				if(mListenerList != null) {
    					for (ICipherConnectControlListener connListener : mListenerList) 
    	    	    		connListener.onGetLEDevice(cBTDeivce);
    				}	
        		}
        	}
        }
    };
    
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
            	if(mBluetoothGatt!= null && true == mBluetoothGatt.discoverServices())
            		CipherConnCtrlmplBLE.this.fireConnecting(CipherConnCtrlmplBLE.this.mDestBTLEDevice);
            	else {
            		mDisconnectFromWorkerThread();
            		CipherConnCtrlmplBLE.this.fireDisconnected(CipherConnCtrlmplBLE.this.mDestBTLEDevice);
            		if(mBAuoReconnect)
        	        	mSetCheckConnTimer(true);
            	}
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            	mDisconnectFromWorkerThread();
            	CipherConnCtrlmplBLE.this.fireDisconnected(CipherConnCtrlmplBLE.this.mDestBTLEDevice);
    	        if(mBAuoReconnect)
    	        	mSetCheckConnTimer(true);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	//Set Characteristic Notification
            	List<BluetoothGattService> lstServices = gatt.getServices();
            	for(BluetoothGattService btService : lstServices) 
            	{	
            		if(btService.getUuid().equals(mSUUIDString) ) {
            			BluetoothGattCharacteristic btCharct = btService.getCharacteristic(mCUUIDString);
            			if(btCharct != null) 
            			{
                			final int charaProp = btCharct.getProperties();
                			mResetCharacteristic();
                			// Characteristic has read property
                			if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                // If there is an active notification on a characteristic, clear
                                // it first so it doesn't update the data field on the user interface.
                				mBluetoothGatt.readCharacteristic(btCharct);
                            }
                			
                			// Characteristic has notify property
                			if(0 < (btCharct.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY))
                			{            					
                				//To trigger that can receive data from callback "onCharacteristicChanged"
                				mBluetoothGatt.setCharacteristicNotification(btCharct, true);
                			}
                			mHandlerConnTimeout.removeCallbacksAndMessages(null);
                			mSetConnectedStatus(ConnStatus.CONN_STATE_CONNECTED);
                			CipherConnCtrlmplBLE.this.fireConnected(CipherConnCtrlmplBLE.this.mDestBTLEDevice);
                			if(mBAuoReconnect)
                	        	mSetCheckConnTimer(false);
                			return;
            			}
            		}
            	}
            }
            
            //discover no services.
            mDisconnectFromWorkerThread();
        	CipherConnCtrlmplBLE.this.fireCipherConnectControlError(CipherConnCtrlmplBLE.this.mDestBTLEDevice,
        					CipherConnectControlResource.can_not_find_any_services_id,
        					CipherConnectControlResource.can_not_find_any_services);
            if(mBAuoReconnect)
	        	mSetCheckConnTimer(true);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	CipherConnCtrlmplBLE.this.fireConnecting(CipherConnCtrlmplBLE.this.mDestBTLEDevice);
            }
            else {
            	mDisconnectFromWorkerThread();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
        	final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {

                String barcode = new String(data);
    			
    			StringTokenizer st = new StringTokenizer(barcode,"\n");
                if(st.countTokens()==1){
                	fireReceivingBarcode(null, barcode);
                }
                else{
                	
                	int count = st.countTokens();
                	for(int i=0;i<count;i++)
                	{
                		String code = (String)st.nextElement();
                		fireReceivingBarcode(null,code);
                		
                		try {
    						Thread.sleep(300);
    					} 
    					catch (Exception e) {
    					}
                	}
                }
            }
        }
    };
	
	@Override
	public boolean isConnected() {
		return mIsConnected();
	}

	@Override
	public ICipherConnBTDevice[] getBtDevices() 
	{
		ICipherConnBTDevice[] devices = new ICipherConnBTDevice[mbtDericeList.size()];
		int idxDevices = 0;
		for (ICipherConnBTDevice device : mbtDericeList) {
			devices[idxDevices++] = new CipherConnBTDevice(device);
		}
		return devices;
	}
	
	@Override
	public void connect(String deviceName, String deviceAddr) throws NullPointerException {
		ICipherConnBTDevice device = new CipherConnBTDevice(deviceName, deviceAddr);
		connect(device);
	}

	@Override
	public void connect(ICipherConnBTDevice device) throws NullPointerException {
		if(mIsConnected() || device == null || device.getDeviceName() == null)
			return;
		mSetConnectedStatus(ConnStatus.CONN_STATE_CONNECTING);
		String deviceName = device.getDeviceName();
		fireCipherBeginConnectControl(device);
		fireConnecting(device);
		
		try {
			if(mBluetoothAdapter == null || deviceName.length() <= 0)
				throw new NullPointerException("mBluetoothAdapter == null or empty deviceName ");
			
			mDestBTLEDevice = null;
			for (ICipherConnBTDevice itrDevice : mbtDericeList) {
				if(itrDevice.getAddress().equals(device.getAddress())) {
					mDestBTLEDevice = itrDevice;
					break;
				}
			}
			if(mDestBTLEDevice == null)
				throw new NullPointerException("can`t find "+ deviceName);
			
			final BluetoothDevice receivedDevice = mBluetoothAdapter.getRemoteDevice(mDestBTLEDevice.getAddress());
	        if (receivedDevice == null) 
	        	throw new NullPointerException("can`t getRemoteDevice"+ deviceName);
	        
	        // We want to directly connect to the device, so we are setting the autoConnect
	        // parameter to false.
	        mBluetoothGatt = receivedDevice.connectGatt(mContext, false, mGattCallback);
	        
	        //clean checking connection timer first, it should trigger after connecting error or disconnect. 
	        if(mBAuoReconnect)
	        	mSetCheckConnTimer(false);
	        
	        //disconnect if timeout
	        final long SCAN_PERIOD = 10000;
	        mHandlerConnTimeout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(ConnStatus.CONN_STATE_CONNECTED != mConnStatus) {
                    	mDisconnect();
                    	fireCipherConnectControlError(mDestBTLEDevice,
                    			CipherConnectControlResource.bluetooth_connection_error_id,
                    			CipherConnectControlResource.bluetooth_connection_error);
            	        if(mBAuoReconnect)
            	        	mSetCheckConnTimer(true);
                    }
                }
            }, SCAN_PERIOD);
		}
		catch (Exception e) {
			mDisconnect();
        	fireCipherConnectControlError(device, 0, e.getMessage());
		}
	}
	@Override
	public void disconnect() {
		mDisconnect();
		fireDisconnected(mDestBTLEDevice);
	}

	@Override
	public boolean StartScanLEDevices() throws UnsupportedOperationException {
		if(mBluetoothAdapter != null) {
			mbtDericeList.clear();
			return mBluetoothAdapter.startLeScan(mLeScanCallback);
		}
		return false;
	}
	
	public boolean StopScanLEDevices() throws UnsupportedOperationException {
		if(mBluetoothAdapter != null) {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			return true;
		}
		return false;
	}
}
