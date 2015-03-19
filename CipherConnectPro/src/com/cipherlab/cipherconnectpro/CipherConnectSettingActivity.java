package com.cipherlab.cipherconnectpro;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


import com.cipherlab.cipherconnectpro.CipherConnectSettingInfo;
import com.cipherlab.cipherconnectpro.R;
import com.cipherlab.util.DialogUtil;
import com.cipherlab.util.KeyboardUtil;
import com.cipherlab.cipherconnectpro.SalveModeActivity;


public class CipherConnectSettingActivity extends PreferenceActivity {
	
	private static final String TAG = "CipherConnectSettingActivity()";
	private static final int REQUEST_ENABLE_BT = 1;
	public static final int CLASSIC_BLUETOOTH_SETTINGS = 2;
	public static final int LE_BLUETOOTH_SETTINGS = 3;
		
	private BluetoothAdapter mBluetoothAdapter;
	private ServiceReceiver mServiceActionReceiver = new ServiceReceiver();
	private String mStrConnectDlgTitle = null;
	private ProgressDialog mPDialog = null;
	private BuildConnMethodPreference mBuildConn = null;
	private Preference   btnBTSetting = null;  // visual,2012/4/18,Open BT-Setting for 3.x, Yifan,2015/01/27,support Low energy mode.
	//private PreferenceScreen btnDisplaySetting = null;  // visual,2012/8/22
	private ListPreference lstSendBarcodeInterval = null;  // william, 2012/09/13
	private ListPreference lstLanguage = null;             // william, 2012/09/18
	private CheckBoxPreference ckbMinimum = null;	//Minimize keyboard 
    
	// miller,2012/8/27 for suspend screen backlight
	private CheckBoxPreference ckbScreenBacklight = null;

    private ICipherConnectManagerService mCipherConnectService;
    
    private ServiceConnection mSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mCipherConnectService = ((CipherConnectManagerService.LocalBinder) service).getService();                                    
            init_UI();         
            mStopListenConnIfSlaveMode();
            mUpdateUI();
        }

        public void onServiceDisconnected(ComponentName className) {
            mCipherConnectService = null;
        }
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.layout.cipherconnect_setting_activity);     
        PreferenceManager.setDefaultValues(this, R.layout.cipherconnect_setting_activity, false);
        mStrConnectDlgTitle = getResources().getString(R.string.setting_bluetooth_device_status);
        remove_ime_conflic();
        
        /* [Begin] Enable CipherConnectManagerService */ 
        try {
            Intent intent = new Intent(this, CipherConnectManagerService.class);
            this.startService(intent);
            this.bindService(intent, mSConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(this.getResources().getString(R.string.ime_name),
                  "CipherConnectSettingActivity.ConnectStatus_bt_startService:",
                  e);
        }
        /* [End] Enable CipherConnectManagerService */               
    }
  
    /*
     * <!----------------------------------------------------------------->
     * @Name: init_UI()
     * @Description: Initial CipherConnect setting menu 
     *   
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    @SuppressWarnings("deprecation")
	private void init_UI()
	{
    	Boolean btStatus = false;
    	Boolean checkStatus = KeyboardUtil.isEnableingKeyboard(CipherConnectSettingActivity.this, R.string.ime_service_name);

    	if (checkStatus == false)
            mCipherConnectService.stopSelf();
    	
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d("xxxx", "mBluetoothAdapter=null");
        } else {
            btStatus = mBluetoothAdapter.isEnabled();
            if (btStatus == false) {
 
            } else {
       
            }
        }
        
        /*Connect to BT device*/
        mBuildConn = (BuildConnMethodPreference)findPreference("btnBT_buildConn");
        if(mBuildConn != null)
        {
        	mBuildConn.setOnPreferenceClickListener(new OnPreferenceClickListener() 
            {
                public boolean onPreferenceClick(Preference preference)
                {
                	BuildConnMethodPreference bcPreference = (BuildConnMethodPreference)preference;
                	if(bcPreference != null)
                	{
                		if(bcPreference.IsSlaveConn())
                		{
                			Intent intent = new Intent(CipherConnectSettingActivity.this, SalveModeActivity.class);
                			startActivity(intent);
                		}
                		else //Master connection
                		{
                			
                		}
                	}
                	return true;
                }
            });
        }
        
        /* BTSetting */
    	this.btnBTSetting = (ListPreference)findPreference("btnBT_Setting");
    	String strCurSettingMode = CipherConnectSettingInfo.getScanMode(this);
    	
    	if(0 == strCurSettingMode.compareTo(this.getResources().getString(R.string.Str_BT_Classic)))
    	{
    		this.btnBTSetting.setSummary(R.string.Bluetooth_Setting_Summary);
    		this.btnBTSetting.setTitle(R.string.Bluetooth_Setting_Title);
    	}
    	else if(0 == strCurSettingMode.compareTo(this.getResources().getString(R.string.Str_BT_LE))) {
    		this.btnBTSetting.setSummary(R.string.Bluetooth_Setting_BLE_Summary);
    		this.btnBTSetting.setTitle(R.string.Bluetooth_Setting_BLE_Title);
    	}
    	
        this.btnBTSetting.setOnPreferenceClickListener(new OnPreferenceClickListener() 
        {
            public boolean onPreferenceClick(Preference preference) {
                return BTSetting_onPreferenceClick(preference);
            }
        });
        
        this.btnBTSetting.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
        {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				return BTMode_onPreferenceClick(preference, newValue);
			}
        });
     
        /* DisplaySetting */
		ckbScreenBacklight = (CheckBoxPreference) findPreference("ckbSuspend_Enable");
		ckbScreenBacklight.setChecked(CipherConnectSettingInfo
				.isSuspendBacklight(this));
		ckbScreenBacklight
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						return ScreenBacklight_onPreferenceChange(preference,
								newValue);
            }
        });
        
        /* Minimum keyboard */
        ckbMinimum = (CheckBoxPreference) findPreference("ckbMinimum");
        if (checkStatus == true && btStatus == true) {
            ckbMinimum.setEnabled(true);
        } else {
            ckbMinimum.setEnabled(false);
        }
        
		ckbMinimum.setChecked(CipherConnectSettingInfo.isMinimum(this));
		ckbMinimum
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
                return Minimum_onPreferenceChange(preference, newValue);
            }
        });
        
		/* lstSendBarcodeInterval */
        lstSendBarcodeInterval = (ListPreference) findPreference("lstSendBarcodeInterval");
        lstSendBarcodeInterval.setEntries(R.array.SendBarcodeInterval_entries);	
        
        if (lstSendBarcodeInterval == null)
        	CipherConnectSettingInfo.setBarcodeInterval(this, lstSendBarcodeInterval.getValue());
        
        lstSendBarcodeInterval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return SendBarcodeInterval_onPreferenceChange(preference, newValue);
            }
        });
        
        /* lstLanguage */
        lstLanguage = (ListPreference) findPreference("lstLanguage");
        lstLanguage.setEntries(R.array.Language_entries);
        
        if (lstLanguage == null)
        	CipherConnectSettingInfo.setLanguage(this, lstLanguage.getValue());

        Log.d(TAG, "Language : " +lstLanguage.getValue());
        
        lstLanguage.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return Language_onPreferenceChange(preference, newValue);
            }
        });
	}
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: updateUI()
     * @Description: Update screen. 
     *   
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void mUpdateUI() {
        if(mBuildConn == null ||
           mCipherConnectService == null)
        	return;
        mBuildConn.setButtonMode(mCipherConnectService.isConnected());       
    }

    /*
     * <!----------------------------------------------------------------->
     * @Name: enableUI()
     * @Description: Enable Cipherconnect setting menu. 
     *   
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void enableUI() {
        if (KeyboardUtil.isEnableingKeyboard(CipherConnectSettingActivity.this, R.string.ime_service_name) == true) {
            ckbMinimum.setEnabled(true);
            boolean AutoConnectFlag = CipherConnectSettingInfo.isAutoConnect(this);
            
        }
    }

    private void mStopListenConnIfSlaveMode()
    {
    	if(mBuildConn != null && mBuildConn.IsSlaveConn())
    	{
            if(mCipherConnectService != null)
            	mCipherConnectService.StopListenConn();		
    	}
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: disableUI()
     * @Description: Disable Connect status / Choose BT device / Auto connect  
     *               and Minmum keyboard on Setting menu.
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void disableUI() {
        ckbMinimum.setEnabled(false);
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: doBTSettingIntent()
     * @Description: Enter into bluetooth setting page.
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void doBTSettingIntent() {
    	Intent intentBluetooth = new Intent();
		intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
		startActivityForResult(intentBluetooth, CLASSIC_BLUETOOTH_SETTINGS); 
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: BTSetting_onPreferenceClick()
     * @Description: Entry the Bluetooth Setting menu
     *  
     * @param: Preference preference
     * @param: N/A
     * return: boolean 
     * <!----------------------------------------------------------------->
     * */
    public boolean BTSetting_onPreferenceClick(Preference preference) {
    	if(false == mCipherConnectService.IsBLEModeSupported()) {
    		((ListPreference)preference).getDialog().hide();
    		mCipherConnectService.SetBLEMode(false);
    		doBTSettingIntent();
    	}
        return true;
    }
    
    public boolean BTMode_onPreferenceClick(Preference preference, Object newValue) {
    	String strMode = (String) newValue;
	    if(0 == strMode.compareTo(getResources().getString(R.string.Str_BT_Classic))) {
	    	mCipherConnectService.SetBLEMode(false);
	    	preference.setSummary(R.string.Bluetooth_Setting_Summary);
	    	preference.setTitle(R.string.Bluetooth_Setting_Title);
	    	CipherConnectSettingInfo.setScanMode(this, strMode);
	    	doBTSettingIntent();
	    }
	    else if (0 == strMode.compareTo(getResources().getString(R.string.Str_BT_LE))) {
	    	mCipherConnectService.SetBLEMode(true);
	    	preference.setSummary(R.string.Bluetooth_Setting_BLE_Summary);
	    	preference.setTitle(R.string.Bluetooth_Setting_BLE_Title);
	    	CipherConnectSettingInfo.setScanMode(this, strMode);
	    	if(null != mBluetoothAdapter && false == mBluetoothAdapter.isEnabled()) {
	    		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return true;
	    	}
	    	
	    	Intent enableLEScanActivity = new Intent(this, LEDeviceScanActivity.class);
	    	startActivityForResult(enableLEScanActivity, LE_BLUETOOTH_SETTINGS);
	    	return true;
	    }
	    else {
	    	return false;
	    }
    	return true;
    }
    
    public boolean DisplaySetting_onPreferenceClick(Preference preference) {
        Intent intentBluetooth = new Intent();
        intentBluetooth.setAction(android.provider.Settings.ACTION_DISPLAY_SETTINGS);
        startActivity(intentBluetooth); 
        return true;
    }


    /*
     * <!----------------------------------------------------------------->
     * @Name: SendBarcodeInterval_onPreferenceChange()
     * @Description: Choose value for send barcode Interval.
     *  
     * @param: Preference preference
     * @param: Object newValue
     * return: boolean 
     * <!----------------------------------------------------------------->
     * */
    public boolean SendBarcodeInterval_onPreferenceChange(Preference preference, Object newValue) {
        CipherConnectSettingInfo.setBarcodeInterval(this, (String) newValue);
        //BarcodeInterval.setSummary((String) newValue);
        Log.d(TAG, "SendBarcodeInterval_onPreferenceChange(): newValue= " + newValue); 

        return true;
    }

    /*
     * <!----------------------------------------------------------------->
     * @Name: Language_onPreferenceChange()
     * @Description: Choose Language value.
     *  
     * @param: Preference preference
     * @param: Object newValue
     * return: boolean 
     * <!----------------------------------------------------------------->
     * */
	public boolean Language_onPreferenceChange(Preference preference, Object newValue) {
        CipherConnectSettingInfo.setLanguage(this, (String) newValue);
        //BarcodeInterval.setSummary((String) newValue);
        //String list = (String) lstLanguage.getEntry();
        Log.d(TAG, "Language_onPreferenceChange(): newValue= " + newValue);
        //Log.d(TAG, "Language_onPreferenceChange(): Entry= " + lst);

        return true;
    }
        
    /*
     * <!----------------------------------------------------------------->
     * @Name: exit_onPreferenceChange()
     * @Description: Exit CipherConnect and stop service 
     *  
     * @param: Preference preference
     * @param: Object newValue
     * return: boolean 
     * <!----------------------------------------------------------------->
     * */
    public boolean exit_onPreferenceChange(Preference preference) {
        if (!KeyboardUtil.isEnableingKeyboard(CipherConnectSettingActivity.this, R.string.ime_service_name)) {
            mCipherConnectService.stopSelf();
        }

        this.finish();
        return true;
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: Minimum_onPreferenceChange()
     * @Description: Set Minimum keyboard. 
     *   
     * @param: Preference preference
     * @param: Object newValue
     * return: boolean 
     * <!----------------------------------------------------------------->
     * */
    public boolean Minimum_onPreferenceChange(Preference preference, Object newValue) {
        Boolean b = (Boolean) newValue;
        
        if (CipherConnectSettingInfo.isMinimum(this) != b)
            CipherConnectSettingInfo.setMinimum(this, b);
        
        return true;
    }

    private void remove_ime_conflic() {
        if (KeyboardUtil.isEnableingKeyboard(CipherConnectSettingActivity.this, R.string.ime_service_name_conflict)) {
            
        	Builder builder = DialogUtil.newAlertDialog(this,
                              							R.string.remove_lite_dialog_title,
                              							R.string.remove_lite_dialog_body);
            
            builder.setNegativeButton(R.string.remove_lite_quit_button, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int which) {
            		CipherConnectSettingActivity.this.finish();

            		if (mCipherConnectService != null) {
            			mCipherConnectService.AuotConnect(false, CipherConnectSettingInfo.getLastDeviceName(getApplicationContext()));
                		mCipherConnectService.stopSelf();
                	}

                }
            });
            builder.create();
            builder.show();
        }
    }
    
    private static IntentFilter makeServiceActionsIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CipherConnectManagerService.ACTION_SERVER_STATE_CHANGED);
        intentFilter.addAction(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mServiceActionReceiver, makeServiceActionsIntentFilter());
        mUpdateUI();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mServiceActionReceiver);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        switch (requestCode) {
        case REQUEST_ENABLE_BT :
        {
        	if(resultCode == Activity.RESULT_OK ) {
        		super.onActivityResult(requestCode, resultCode, data);
        		Intent enableLEScanActivity = new Intent(this, LEDeviceScanActivity.class);
    	    	startActivity(enableLEScanActivity);
        	}
        }
        break;
        case CLASSIC_BLUETOOTH_SETTINGS:
        {
        	String[] strDeviceNames = mCipherConnectService.getBtDeviceNames();
    		String strDevice = "";
        	if(strDeviceNames != null && strDeviceNames.length > 0) {
        		strDevice = strDeviceNames[0];
    		} 
        	CipherConnectSettingInfo.setLastDeviceName(this, strDevice);

        }
        break;
        case LE_BLUETOOTH_SETTINGS:
        {
        	if(data != null) {
        		String strDeviceName = data.getStringExtra(LEDeviceScanActivity.EXTRAS_DEVICE_NAME);
	        	if (strDeviceName != null) {
	        		CipherConnectSettingInfo.setLastDeviceName(this, strDeviceName);

	            }
        	}
        	else {
        		String[] strDeviceNames = mCipherConnectService.getBtDeviceNames();
        		String strDevice = "";
        		if(strDeviceNames != null && strDeviceNames.length > 0) {
        			strDevice = strDeviceNames[0];
        		}
        		CipherConnectSettingInfo.setLastDeviceName(this, strDevice);

        	}
        }
        break;
        default:
        	super.onActivityResult(requestCode, resultCode, data);
        }        
    }
   
    @Override
    protected void onRestart() {
    	Log.d(TAG, "onRestart()"); 
    	
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled() == false) {

                disableUI();
            } else {

                enableUI();
            }
        }
        super.onRestart();
    }
  
    @Override
	protected void onDestroy() 
    {
    	Log.d(TAG, "onDestroy()");    	
    	this.unbindService(this.mSConnection);
        this.mSConnection = null;
        this.mCipherConnectService = null;
        
        super.onDestroy();
    }    
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: ServiceReceiver()
     * @Description: Receiver the Bluetooth Turn on/off event.  
     *   Handles various events fired by the CipherConnectManagerService.
     *   ACTION_BT_BEGIN_CONNECTING: begin connecting to BT device.
     *   ACTION_BT_CONNECTING: connecting to BT device.
     *   ACTION_BT_CONNECTED: connected to BT device.
     *   ACTION_BT_DISCONNECTED: disconnect BT device.
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    public class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                               BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {

                    enableUI();
                } else if (state == BluetoothAdapter.STATE_OFF) {

                    disableUI();
                }
            }
            //Connection state change
            else if (CipherConnectManagerService.ACTION_CONN_STATE_CHANGED.equals(action)) 
            {
            	ICipherConnectManagerService.CONN_STATE connstate = mCipherConnectService.GetConnState();  	
            	switch (connstate) 
            	{
            		case  CONN_STATE_CONNECTING:
            		{
            			String strMsg = getResources().getString(R.string.setting_bluetooth_device_connecting);
                    	if(mPDialog == null) {
                    		mPDialog = ProgressDialog.show(CipherConnectSettingActivity.this, mStrConnectDlgTitle, strMsg);
                    	}
                    	else {
                    		mPDialog.setMessage(strMsg);
                    		mPDialog.show();
                    	}
            		}
            		break;
            		case  CONN_STATE_CONNECTERR:
            		{
            			Toast.makeText(getApplicationContext(), "BT Connect error", Toast.LENGTH_SHORT).show();
            		}
            		break;
            		case CONN_STATE_DISCONNECT:
            		{
            			if(mPDialog != null) {
                    		String strMsg = getResources().getString(R.string.the_bluetooth_device_disconnected);
                    		mPDialog.setMessage(strMsg);
                    		mPDialog.dismiss();
                    		mPDialog = null;
                    	}
                    	Toast.makeText(getApplicationContext(), "BT Disconnected", Toast.LENGTH_SHORT).show();
            		}
            		break;
            		case  CONN_STATE_CONNECTED:
            		{
            			if(mPDialog != null) {
                    		String strMsg = getResources().getString(R.string.setting_bluetooth_device_connected);
                    		mPDialog.setMessage(strMsg);
                    		mPDialog.dismiss();
                    		mPDialog = null;
                    	}
                    	Toast.makeText(getApplicationContext(), "BT Connected", Toast.LENGTH_SHORT).show();
            		}
            		default:
            		{	
            			
            		}
            		break;
            	}  	
            	mUpdateUI();
            }
            // server status change
            else if(CipherConnectManagerService.ACTION_SERVER_STATE_CHANGED.equals(action))
            {
            	ICipherConnectManagerService.SERVER_STATE serverstate = mCipherConnectService.GetServerState();  	
            	switch (serverstate) 
            	{
		            case  SERVER_STATE_OFFLINE:
		    		{
		    			
		    		}
		    		break;
		    		case  SERVER_STATE_ONLINE:
		    		default:
		    		{	
		    			
		    		}
		    		break;
		        }
            }
        }
    }

	/*
	 * <!----------------------------------------------------------------->
	 * 
	 * @Name: ScreenBacklight_onPreferenceChange()
	 * 
	 * @Description: suspend screen backlight
	 * 
	 * @param: Preference preference
	 * 
	 * @param: Object newValue return: boolean
	 * <!----------------------------------------------------------------->
	 */
	public boolean ScreenBacklight_onPreferenceChange(Preference preference,
			Object newValue) {
		if (newValue instanceof Boolean) {
			Boolean boolVal = (Boolean) newValue;

			if (boolVal == true) {
				CipherConnectSettingInfo.setSuspendBacklight(this, true);

				if (mCipherConnectService.isConnected() == true) {
					CipherConnectWakeLock.enable();
				}
			} else {
				CipherConnectSettingInfo.setSuspendBacklight(this, false);

				if (mCipherConnectService.isConnected() == true) {
					CipherConnectWakeLock.disable();
				}
			}
		}

		return true;
	}
}