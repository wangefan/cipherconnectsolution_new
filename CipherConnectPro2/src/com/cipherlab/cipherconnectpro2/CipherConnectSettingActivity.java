package com.cipherlab.cipherconnectpro2;

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
import android.os.Handler;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;


import com.cipherlab.cipherconnect.sdk2.ICipherConnBTDevice;
import com.cipherlab.cipherconnectpro2.R;
import com.cipherlab.util.DialogUtil;
import com.cipherlab.util.KeyboardUtil;
import com.cipherlab.cipherconnectpro2.CipherConnectSettingInfo;
import com.cipherlab.cipherconnectpro2.SalveModeActivity;
import com.cipherlab.help.CipherLog;


public class CipherConnectSettingActivity extends PreferenceActivity 
{
	//constant 
	private static final int REQUEST_GET_CLASSIC_BT = 1;
	private static final int REQUEST_GET_CLE_BT = 2;
	private static final int REQUEST_ENABLE_IM = 3;
		
	public static final String KEY_GET_CLSC_BT_DEVICE = "KEY_GET_CLSC_BT_DEVICE";
	public static final String KEY_GET_LE_BT_DEVICE = "KEY_GET_LE_BT_DEVICE";
	private static final String TAG = "CipherConnectSettingActivity()";
		
	private BluetoothAdapter mBluetoothAdapter;
	private ICipherConnectManagerService mCipherConnectService;
	private ServiceReceiver mServiceActionReceiver = new ServiceReceiver();
	private ProgressDialog mPDialog = null;
	
	boolean mBAddBTModeButton = false;
	boolean mBAlreadyEnableIMPage = false;
	boolean mDoubleBackToExitPressedOnce = false;
	
	//controls
	private BuildConnMethodPreference mBuildConn = null;
	private ListPreference   mBtnBTMode = null; 
	private CheckBoxPreference ckbScreenBacklight = null;
	private CheckBoxPreference mCKEnableMinimum = null;
	private CheckBoxPreference mCKDisconnSwch = null;
	private ListPreference lstSendBarcodeInterval = null; 
	private ListPreference lstDefaultLanguage = null;   
	private ListPreference lstLanguage = null;    
	private Preference mAbout = null;    
    
    private ServiceConnection mSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
        	// Initializes a Bluetooth adapter.  For API level 18 and above, get a reference t
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            
            // Checks if Bluetooth is supported on the device.
            if (mBluetoothAdapter == null) {
                Toast.makeText(CipherConnectSettingActivity.this, "onServiceConnected, mBluetoothAdapter == null", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }      
            
            mCipherConnectService = ((CipherConnectManagerService.LocalBinder) service).getService(); 
            if (mCipherConnectService == null) {
            	Toast.makeText(CipherConnectSettingActivity.this, "onServiceConnected, mCipherConnectService == null", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }   
                                               
            init_UI();         
            mUpdateUI(false);
        }

        public void onServiceDisconnected(ComponentName className) {
            mCipherConnectService = null;
        }
    };
    
    private void mConnectBT(ICipherConnBTDevice device)
    {
    	if(device != null && mCipherConnectService != null)
    	{
    		try {
				mCipherConnectService.connect(device);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    private void mShowProgressDlg(boolean bShow)
    {
    	if(bShow)
    	{
    		if(mPDialog != null)
    			mPDialog.show();
    		else
    		{
    			String strTitle = getResources().getString(R.string.strConnecting), 
    				   strMsg = getResources().getString(R.string.strConnectingMsg);
    			mPDialog = ProgressDialog.show(this, strTitle, strMsg);
    		}
    	}
    	else
    	{
    		if(mPDialog != null)
    			mPDialog.dismiss();
    	}
    }
    
    private boolean mBIMNotReady() 
    {
    	boolean bEnabled = KeyboardUtil.isEnableingKeyboard(this, R.string.ime_service_name);
    	String id = Settings.Secure.getString(
    			   getContentResolver(), 
    			   Settings.Secure.DEFAULT_INPUT_METHOD
    			);
    	ComponentName defaultInputMethod = ComponentName.unflattenFromString(id);
    	ComponentName myInputMethod = new ComponentName(this, CipherConnectKeyboardService.class);

    	boolean bIsCurrent = myInputMethod.equals(defaultInputMethod);
    	return bEnabled && bIsCurrent;
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	CipherLog.d(TAG, "onCreate begin");
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.layout.cipherconnect_setting_activity);     
        PreferenceManager.setDefaultValues(this, R.layout.cipherconnect_setting_activity, false);
        remove_ime_conflic();
        
        /* [Begin] Enable CipherConnectManagerService */ 
        try {
            Intent intent = new Intent(this, CipherConnectManagerService.class);
            bindService(intent, mSConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            CipherLog.e(this.getResources().getString(R.string.ime_name),
                  "CipherConnectSettingActivity.ConnectStatus_bt_startService:",
                  e);
        }
        /* [End] Enable CipherConnectManagerService */ 
        mBAlreadyEnableIMPage = false;
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
    	CipherLog.d(TAG, "init_UI begin");
    	Boolean btStatus = false;
    	Boolean checkStatus = KeyboardUtil.isEnableingKeyboard(CipherConnectSettingActivity.this, R.string.ime_service_name);

    	if (checkStatus == false)
            mCipherConnectService.stopSelf();
    	
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            CipherLog.d("xxxx", "mBluetoothAdapter=null");
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
            			String strCurMode = getResources().getString(R.string.Str_BT_Classic); 
            					
        				if(mBtnBTMode != null)
        					strCurMode = (String)mBtnBTMode.getEntry();
            			
            			//Classic
            			if(strCurMode.equals(getResources().getString(R.string.Str_BT_Classic)))
            			{
            				if(mCipherConnectService.isConnected())
            				{
                        		mCipherConnectService.disConnect();
            				}
            				else
            				{
            					if(bcPreference.GetBConnState().equals(BuildConnMethodPreference.BCEnum.SLAVE))
                        		{
                        			Intent intent = new Intent(CipherConnectSettingActivity.this, SalveModeActivity.class);
                        			startActivity(intent);
                        		}
                        		else if(bcPreference.GetBConnState().equals(BuildConnMethodPreference.BCEnum.SLAVE_QR))
                        		{
                        			Intent intent = new Intent(CipherConnectSettingActivity.this, SalveModeQRActivity.class);
                        			startActivity(intent);
                        		}
                        		else //Master connection
                        		{
	            					String devName = mBuildConn.getLastDevName(), 
	            						   devAddr = mBuildConn.getLastDevAddr();	
	            					boolean bNeedAutoReConn = mBuildConn.getAutoReConn();
	            					mCipherConnectService.setAutoConnect(bNeedAutoReConn);
	            					try {
										mCipherConnectService.connect(devName, devAddr);
									} catch (Exception e) {										
										e.printStackTrace();
									}
                					CipherLog.d(TAG, "connect to : " + devName + ", MAC addr = " + devAddr);
                				}
                			}
            			}
            			//Low Energy
            			else if(strCurMode.equals(getResources().getString(R.string.Str_BT_LE)))
            			{
            				if(mCipherConnectService.isConnected())
            				{
            					mCipherConnectService.disConnect();
            				}
            				else
            				{
            					String devName = mBuildConn.getLastDevName(), 
             						   devAddr = mBuildConn.getLastDevAddr();	
            					boolean bNeedAutoReConn = mBuildConn.getAutoReConn();
            					mCipherConnectService.setAutoConnect(bNeedAutoReConn);
             					try {
									mCipherConnectService.connect(devName, devAddr);
								} catch (Exception e) {										
									e.printStackTrace();
								}
             					CipherLog.d(TAG, "connect to : " + devName + ", MAC addr = " + devAddr);		
            				}
            			}
                	}
                	return true;
                }
            });
        	
        	mBuildConn.setOnPreferenceClickScanListener(new Button.OnClickListener()
        	{
        		public  void onClick(View v)
        		{
        			String strCurBTMode = CipherConnectSettingInfo.getBTMode(CipherConnectSettingActivity.this);
        			if(0 == strCurBTMode.compareTo(getResources().getString(R.string.Str_BT_Classic)))
        			{
        				Intent getBtDeviceIntent = new Intent(CipherConnectSettingActivity.this, ClassicBTDeviceScanActivity.class);
        		        startActivityForResult(getBtDeviceIntent, REQUEST_GET_CLASSIC_BT);
        			}
        			else if(0 == strCurBTMode.compareTo(getResources().getString(R.string.Str_BT_LE)))
        			{
        				Intent getBtDeviceIntent = new Intent(CipherConnectSettingActivity.this, LEDeviceScanActivity.class);
        		        startActivityForResult(getBtDeviceIntent, REQUEST_GET_CLE_BT);
        			}
        		}
        	});
        	
        	mBuildConn.setPreferenceAutoChangeListener(new CheckBox.OnCheckedChangeListener()
        	{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
				{
					if(mCipherConnectService != null)
						mCipherConnectService.setAutoConnect(isChecked);
				}
        	});
        	
        	mBuildConn.setService(mCipherConnectService);
        }
        
        /* BT mode select */
        if(mBAddBTModeButton)
        {
        	PreferenceCategory pc = (PreferenceCategory) getPreferenceScreen().getPreference(0);
        	if(pc != null)
        	{
        		mBtnBTMode = new ListPreference(this);
        		mBtnBTMode.setKey("btnBT_Mode");
        		mBtnBTMode.setTitle(R.string.Bluetooth_Mode_Title);
        		mBtnBTMode.setSummary(R.string.Bluetooth_Mode_Summary);
        		mBtnBTMode.setEntries(R.array.ScanMode_entries);
        		mBtnBTMode.setEntryValues(R.array.ScanMode_entries_value);
        		mBtnBTMode.setDefaultValue(R.string.Str_BT_Classic);
        		pc.addPreference(mBtnBTMode);
        	}
        	 
        	if(mBtnBTMode != null)
        	{
        		// not support LE mode
        		if(false == mCipherConnectService.IsBLEModeSupported())
        		{
        			mBtnBTMode.setSummary(R.string.Bluetooth_Mode_Summary);
            		mBtnBTMode.setTitle(R.string.Bluetooth_Mode_Title);
            		mBtnBTMode.setEnabled(false);
        		}
        		else
        		{
        			//Get from persist setting
        			String strCurBTMode = CipherConnectSettingInfo.getBTMode(this);
                	if(0 == strCurBTMode.compareTo(this.getResources().getString(R.string.Str_BT_Classic)))
                	{
                		mBtnBTMode.setSummary(R.string.Bluetooth_Mode_Summary);
                	}
                	else if(0 == strCurBTMode.compareTo(this.getResources().getString(R.string.Str_BT_LE))) {
                		mBtnBTMode.setSummary(R.string.Bluetooth_Mode_BLE_Summary);
                	}
                	mBtnBTMode.setEnabled(true);
                	mBtnBTMode.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
                    {
            			@Override
            			public boolean onPreferenceChange(Preference preference, Object newValue) {
            				return BTMode_onPreferenceClick(preference, newValue);
            			}
                    });
        		}
        	}  
        }
     
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
		mCKEnableMinimum = (CheckBoxPreference) findPreference("ckbMinimum");
		mCKEnableMinimum.setChecked(CipherConnectSettingInfo.isMinimum(this));
		mCKEnableMinimum.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
                return EnableMinimum_onPreferenceChange(preference, newValue);
            }
        });
		
		/* Minimum keyboard */
		mCKDisconnSwch = (CheckBoxPreference) findPreference("ckbDisconnSwch");
		mCKDisconnSwch.setChecked(CipherConnectSettingInfo.isDisconnSwch(this));
		mCKDisconnSwch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
                return DisconnSwch_onPreferenceChange(preference, newValue);
            }
        });
		
		/* lstSendBarcodeInterval */
        lstSendBarcodeInterval = (ListPreference) findPreference("lstSendBarcodeInterval");
        lstSendBarcodeInterval.setEntries(R.array.SendBarcodeInterval_entries);	
        
        //if (lstSendBarcodeInterval == null)
        //	CipherConnectSettingInfo.setBarcodeInterval(lstSendBarcodeInterval.getValue(), this);
        this.lstSendBarcodeInterval.setSummary(lstSendBarcodeInterval.getEntry());
        CipherLog.d(TAG, "SendBarcodeInterval : " +lstSendBarcodeInterval.getValue());
        
        lstSendBarcodeInterval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return SendBarcodeInterval_onPreferenceChange(preference, newValue);
            }
        });
        
        /* lstDefaultLanguage */
        lstDefaultLanguage = (ListPreference) findPreference("lstDefaultLanguage");
        lstDefaultLanguage.setEntries(R.array.Language_default_entries);
        
        //if (lstDefaultLanguage == null)
        	//CipherConnectSettingInfo.setDefaultLanguage(lstDefaultLanguage.getValue(), this);
        this.lstDefaultLanguage.setSummary(lstDefaultLanguage.getEntry());
        CipherLog.d(TAG, "DefaultLanguage : " +lstDefaultLanguage.getValue());
        
        lstDefaultLanguage.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return DefaultLanguage_onPreferenceChange(preference, newValue);
            }
        });
        
        /* lstLanguage */
        lstLanguage = (ListPreference) findPreference("lstLanguage");
        lstLanguage.setEntries(R.array.Language_entries);
        
      //if (lstLanguage == null)
        //	CipherConnectSettingInfo.setLanguage(lstLanguage.getValue(), this);
    	this.lstLanguage.setSummary(lstLanguage.getEntry());
        CipherLog.d(TAG, "Language : " +lstLanguage.getValue());
        
        lstLanguage.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return Language_onPreferenceChange(preference, newValue);
            }
        });
        
        /* About */
        mAbout = (Preference) findPreference("about");
        
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
    private void mUpdateUI(boolean bShowToast) 
    {	
    	mCKEnableMinimum = (CheckBoxPreference) findPreference("ckbMinimum");
    	if(mCKEnableMinimum != null)
			mCKEnableMinimum.setChecked(CipherConnectSettingInfo.isMinimum(this));
		
        if(mBuildConn != null)
        	mBuildConn.updateButtons();
        
        if(mCipherConnectService == null)
    		return;
        
        ICipherConnectManagerService.CONN_STATE connState = mCipherConnectService.GetConnState();  	
    	switch (connState) 
    	{
    		case  CONN_STATE_BEGINCONNECTING:
    		case  CONN_STATE_CONNECTING:
    		{
    			mShowProgressDlg(true);
    		}
    		break;
    		case  CONN_STATE_DISCONNECT:
    		{
    			mShowProgressDlg(false);
    			if(bShowToast)
    				Toast.makeText(getApplicationContext(), "Disconnect", Toast.LENGTH_SHORT).show();
    			if(mAbout != null)
    				mAbout.setSummary(getResources().getString(R.string.setting_about_sum_fwname) + "none");
    			break;
    		}
    		case  CONN_STATE_CONNECTERR:
    		{
       			mShowProgressDlg(false);
    			if(bShowToast)
    				Toast.makeText(getApplicationContext(), "Connect error", Toast.LENGTH_SHORT).show();
    		}
    		break;
    		case  CONN_STATE_CONNECTED:
    		{
    			if(mBtnBTMode != null)
    				mBtnBTMode.setEnabled(false);
    			ICipherConnBTDevice device = mCipherConnectService.GetConnDevice();
    			if(mBuildConn != null && device != null)
    				mBuildConn.setLastDev(device.getDeviceName(), device.getAddress());
    			if(mAbout != null)
    				mAbout.setSummary(getResources().getString(R.string.setting_about_sum_fwname) + mCipherConnectService.getFWVersion());
    			mShowProgressDlg(false);
    			if(bShowToast)
    				Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
    		}
    		break;
    	}  
    }
    
    public boolean BTMode_onPreferenceClick(Preference preference, Object newValue) {
    	String strMode = (String) newValue;
	    if(0 == strMode.compareTo(getResources().getString(R.string.Str_BT_Classic))) {
	    	mCipherConnectService.SetBLEMode(false);
	    	preference.setSummary(R.string.Bluetooth_Mode_Summary);
	    	CipherConnectSettingInfo.setBTMode(strMode, this);
	    }
	    else if (0 == strMode.compareTo(getResources().getString(R.string.Str_BT_LE))) {
	    	mCipherConnectService.SetBLEMode(true);
	    	preference.setSummary(R.string.Bluetooth_Mode_BLE_Summary);
	    	CipherConnectSettingInfo.setBTMode(strMode, this);   	
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
        CipherConnectSettingInfo.setBarcodeInterval((String) newValue, CipherConnectSettingActivity.this);
        //BarcodeInterval.setSummary((String) newValue);
        CipherLog.d(TAG, "SendBarcodeInterval_onPreferenceChange(): newValue= " + newValue); 

        if(this.lstSendBarcodeInterval!=null)
        {
        	CharSequence entry = this.GetEntry(R.array.SendBarcodeInterval_entries,R.array.SendBarcodeInterval_entries_value,(String)newValue);
        	this.lstSendBarcodeInterval.setSummary(entry);
        }
        
        return true;
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: DefaultLanguage_onPreferenceChange()
     * @Description: Choose Language value.
     *  
     * @param: Preference preference
     * @param: Object newValue
     * return: boolean 
     * <!----------------------------------------------------------------->
     * */
	public boolean DefaultLanguage_onPreferenceChange(Preference preference, Object newValue) {
        CipherConnectSettingInfo.setDefaultLanguage((String) newValue, this);
        CipherLog.d(TAG, "DefaultLanguage_onPreferenceChange(): newValue= " + newValue);

        if(this.lstDefaultLanguage!=null)
        {
        	CharSequence entry = this.GetEntry(R.array.Language_default_entries,R.array.Language_default_entries_value,(String)newValue);
        	this.lstDefaultLanguage.setSummary(entry);
        }
        
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
        CipherConnectSettingInfo.setLanguage((String) newValue, this);
        //BarcodeInterval.setSummary((String) newValue);
        //String list = (String) lstLanguage.getEntry();
        CipherLog.d(TAG, "Language_onPreferenceChange(): newValue= " + newValue);
        //CipherLog.d(TAG, "Language_onPreferenceChange(): Entry= " + lst);
        
        if(this.lstLanguage!=null)
        {
        	CharSequence entry = this.GetEntry(R.array.Language_entries,R.array.Language_entries_value,(String)newValue);
        	this.lstLanguage.setSummary(entry);
        }
        
        return true;
    }
	
	private String GetEntry(int entriesID, int valueID, String newValue)
	{
		if(entriesID<0)
			return "";
		if(valueID<0)
			return "";
		if(newValue==null)
			return "";
		
		try {
	    	String[] entryList = getResources().getStringArray(entriesID);
	    	if(entryList==null)
	    		return "";

	    	String[] valueList = getResources().getStringArray(valueID);
	    	if(valueList==null)
	    		return "";
	    	
	    	for(int i=0;i<valueList.length;i++)
	    	{
	    		String value = valueList[i];
	    		if(value.equals(newValue))
	    			return entryList[i];
	    	}
		} catch (Exception e) {
			CipherLog.d(TAG, "GetEntry().ex=" + e.getMessage());
		}

		
    	return "";
	}
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: EnableMinimum_onPreferenceChange()
     * @Description: Set Minimum keyboard. 
     *   
     * @param: Preference preference
     * @param: Object newValue
     * return: boolean 
     * <!----------------------------------------------------------------->
     * */
    public boolean EnableMinimum_onPreferenceChange(Preference preference, Object newValue) {
        Boolean b = (Boolean) newValue;
        
        if (CipherConnectSettingInfo.isMinimum(this) != b)
            CipherConnectSettingInfo.setMinimum(b, this);
        
        return true;
    }
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: DisconnSwch_onPreferenceChange()
     * @Description: Set Disconnect BT connection after switching to User Keybaord. 
     *   
     * @param: Preference preference
     * @param: Object newValue
     * return: boolean 
     * <!----------------------------------------------------------------->
     * */
    public boolean DisconnSwch_onPreferenceChange(Preference preference, Object newValue) {
        Boolean b = (Boolean) newValue;
        
        if (CipherConnectSettingInfo.isDisconnSwch(this) != b)
            CipherConnectSettingInfo.setDisconnSwch(b, this);
        
        return true;
    }    

    private void remove_ime_conflic() {
    	CipherLog.d(TAG, "remove_ime_conflic begin");
        if (KeyboardUtil.isEnableingKeyboard(CipherConnectSettingActivity.this, R.string.ime_service_name_conflict)) {
            
        	Builder builder = DialogUtil.newAlertDialog(this,
                              							R.string.remove_lite_dialog_title,
                              							R.string.remove_lite_dialog_body);
            
            builder.setNegativeButton(R.string.remove_lite_quit_button, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int which) {
            		CipherConnectSettingActivity.this.finish();

            		if (mCipherConnectService != null) {
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
        intentFilter.addAction(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED);
        intentFilter.addAction(CipherConnectManagerService.ACTION_COMMAND);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }
    
    @Override
    protected void onResume() {
    	CipherLog.d(TAG, "onResume()");
        super.onResume();
        
        registerReceiver(mServiceActionReceiver, makeServiceActionsIntentFilter());
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null)
		{
			Toast.makeText(this, R.string.error_bluetooth_not_turnon, Toast.LENGTH_SHORT).show();
            finish();
            return;
		}
		
		if(mBIMNotReady() == false && mBAlreadyEnableIMPage == false)
        {
        	Intent setInputMethod = new Intent(this, SetInputMethod.class);
        	startActivityForResult(setInputMethod, REQUEST_ENABLE_IM);
        	return;
        }
		mBAlreadyEnableIMPage = false;
        mUpdateUI(false);
    }
    
    @Override
    protected void onPause() {
    	CipherLog.d(TAG, "onPause()");
        super.onPause();
        unregisterReceiver(mServiceActionReceiver);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CipherLog.d(TAG, "onActivityResult()"); 
        switch (requestCode) {
        case REQUEST_GET_CLASSIC_BT :
        {
        	if(resultCode == Activity.RESULT_OK ) 
        	{
        		registerReceiver(mServiceActionReceiver, makeServiceActionsIntentFilter()); //ensure that can receive callback from connect
        		ICipherConnBTDevice device = (ICipherConnBTDevice) data.getSerializableExtra(KEY_GET_CLSC_BT_DEVICE);
        		
        		//Reset auto re-connect first.
        		mCipherConnectService.setAutoConnect(false);
        		
        		boolean bNeedAutoReConn = mBuildConn.getAutoReConn();
				mCipherConnectService.setAutoConnect(bNeedAutoReConn);
        		mConnectBT(device);
        		super.onActivityResult(requestCode, resultCode, data);        		
        	}
        }
        break;
        case REQUEST_GET_CLE_BT:
        {
        	if(resultCode == Activity.RESULT_OK ) 
        	{
        		registerReceiver(mServiceActionReceiver, makeServiceActionsIntentFilter()); //ensure that can receive callback from connect
        		ICipherConnBTDevice device = (ICipherConnBTDevice) data.getSerializableExtra(KEY_GET_LE_BT_DEVICE);
        		boolean bNeedAutoReConn = mBuildConn.getAutoReConn();
				mCipherConnectService.setAutoConnect(bNeedAutoReConn);
        		mConnectBT(device);
        		super.onActivityResult(requestCode, resultCode, data);        		
        	}
        }
        break;
        case REQUEST_ENABLE_IM:
        {
        	if(resultCode == Activity.RESULT_OK ) 
        	{
        		mBAlreadyEnableIMPage = true ;     		
        	}
        }
        break;
        default:
        	super.onActivityResult(requestCode, resultCode, data);
        }        
    }
  
    @Override
	protected void onDestroy() 
    {
    	CipherLog.d(TAG, "onDestroy()");    	
    	this.unbindService(this.mSConnection);
        this.mSConnection = null;
        this.mCipherConnectService = null;
        
        super.onDestroy();
    }    
    
    @Override
    public void onBackPressed() {
        if (mDoubleBackToExitPressedOnce) {
        	if(mCipherConnectService != null)
        		mCipherConnectService.disConnect();
            super.onBackPressed();
            return;
        }

        mDoubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.msg_back, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mDoubleBackToExitPressedOnce = false;                       
            }
        }, 2000);
    } 
    
    /*
     * <!----------------------------------------------------------------->
     * @Name: ServiceReceiver()
     * @Description: Handles various events fired by the CipherConnectManagerService.
     *   ACTION_CONN_STATE_CHANGED.
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    public class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	final String action = intent.getAction();
        	
            //Connection state change
            if (CipherConnectManagerService.ACTION_CONN_STATE_CHANGED.equals(action)) 
            {
            	final String info = intent.getStringExtra(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED_KEY);
            	mUpdateUI(true);
            	if(info != null && info.length() > 0)
            		Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
            }
            else if(CipherConnectManagerService.ACTION_COMMAND.equals(action))
            {
            	mUpdateUI(false);
            }
            else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) 
            {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				if (state == BluetoothAdapter.STATE_ON) 
				{
				
				} 
				else if (state == BluetoothAdapter.STATE_OFF) 
				{
					
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
				CipherConnectSettingInfo.setSuspendBacklight(true, this);

				if (mCipherConnectService.isConnected() == true) {
					CipherConnectWakeLock.enable();
				}
			} else {
				CipherConnectSettingInfo.setSuspendBacklight(false, this);

				if (mCipherConnectService.isConnected() == true) {
					CipherConnectWakeLock.disable();
				}
			}
		}

		return true;
	}
}