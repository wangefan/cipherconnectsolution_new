package com.cipherlab.cipherconnect.sdk2.sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.cipherlab.cipherconnect.sdk2.ICipherConnBTDevice;
import com.cipherlab.cipherconnect.sdk2.ICipherConnCtrl2EZMet;
import com.cipherlab.cipherconnect.sdk2.sample.R;

public class CipherConnectSDK2SampleSettingActivity extends Activity {
	private BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
        public void onReceive(Context context, Intent intent) 
		{
        	final String action = intent.getAction();
        	if(action.equals(CipherConnectSDK2SampleMainActivity.ACTION_CLOSE_SELF))
	            finish();
		}
    };
    
	private ICipherConnCtrl2EZMet mCipherConnectControl;
	private ICipherConnBTDevice[] mDevices = null;
	
	//master mode 
	private RelativeLayout mMasterModeGroup = null;
	RadioButton mRdMaster = null;
	private Spinner mLstDeviceName;
	private Button  mbtConnect;
	private CheckBox mchbAutoReconnect;

	//slave mode 	
	private LinearLayout mSlaveModeGroup = null;
	RadioButton mRdSlave = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        
        mCipherConnectControl = CipherConnectSDK2SampleMainActivity._CipherConnectControl;
       
        this.initUI();
    }
	
	@Override
	protected void onResume() {
		final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CipherConnectSDK2SampleMainActivity.ACTION_CLOSE_SELF);
		registerReceiver(mReceiver, intentFilter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mReceiver);
		super.onPause();
	}
	
	private void setMasterMode(boolean bMasterMode) {
		
		if(bMasterMode) {
			mMasterModeGroup.setVisibility(View.VISIBLE);
			mSlaveModeGroup.setVisibility(View.INVISIBLE);
			mRdMaster.setChecked(true);
			mRdSlave.setChecked(false);
		}
		else {
			mMasterModeGroup.setVisibility(View.INVISIBLE);
			mSlaveModeGroup.setVisibility(View.VISIBLE);
			mRdMaster.setChecked(false);
			mRdSlave.setChecked(true);
		}
	}
	
	private void initUI(){
		mMasterModeGroup = (RelativeLayout) findViewById(R.id.masterModeGroup);
        mSlaveModeGroup = (LinearLayout) findViewById(R.id.slaveModeGroup);
        mRdMaster = (RadioButton) findViewById(R.id.masterMode);
        mRdMaster.setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View v) {
				if(CipherConnectSDK2SampleMainActivity.mBIsListening == true) {
					setMasterMode(true);
					mCipherConnectControl.stopListening();
					CipherConnectSDK2SampleMainActivity.mBIsListening = false;	
				}
			}
		});
      
		mLstDeviceName=(Spinner)this.findViewById(R.id.cobSettingDeviceName);
		mbtConnect = (Button)findViewById(R.id.chbSettingBluetoothConnected);
		mchbAutoReconnect = (CheckBox)findViewById(R.id.chbSettingAutoReConnect);
		
		mRdSlave = (RadioButton) findViewById(R.id.slaveMode);
		mRdSlave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(CipherConnectSDK2SampleMainActivity.mBIsListening == false) {
					setMasterMode(false);
					mCipherConnectControl.startListening();
					CipherConnectSDK2SampleMainActivity.mBIsListening = true;
				}
			}
		});
		float fWidthPxl = 300.0f;
		float fHeightPxl = 50.0f;
		ImageView setConnImage = (ImageView)findViewById(R.id.imageSetConn);
		Bitmap bmpSetConn = mCipherConnectControl.getSettingConnBarcodeImage((int)fWidthPxl, (int)fHeightPxl);
		setConnImage.setImageBitmap(bmpSetConn);
		Bitmap bmpAddress = mCipherConnectControl.getMacAddrBarcodeImage((int)fWidthPxl, (int)fHeightPxl);
		ImageView macAddressImage = (ImageView)findViewById(R.id.imageMACAdd);
		macAddressImage.setImageBitmap(bmpAddress);
		
		if(CipherConnectSDK2SampleMainActivity.mBIsListening) {
			mRdMaster.setChecked(false);
		    setMasterMode(false);
		} else {
			mRdMaster.setChecked(true);
		    setMasterMode(true);
		}
	    
		mDevices = mCipherConnectControl.getBtDevices();
    	if(mDevices == null)
    		return;
    	
		String [] devicesName = new String[mDevices.length];
		for(int idxDevice = 0; idxDevice < mDevices.length; ++idxDevice) {
			devicesName[idxDevice] = mDevices[idxDevice].getDeviceName();
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, devicesName);
		mLstDeviceName.setAdapter(adapter);

		boolean bAutoReconnect = CipherConnectSDK2SampleSettingInfo.isAutoReconnect(this);
    	mchbAutoReconnect.setChecked(bAutoReconnect);
    	
    	mbtConnect.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try {
					ICipherConnBTDevice device = mDevices[mLstDeviceName.getSelectedItemPosition()];
					mCipherConnectControl.connect(device);
					finish();
				} 
				catch (Exception e) {
					Toast.makeText(CipherConnectSDK2SampleSettingActivity.this.getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		mchbAutoReconnect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
				mCipherConnectControl.setAutoReconnect(isChecked);
				CipherConnectSDK2SampleSettingInfo.setAutoReconnect(CipherConnectSDK2SampleSettingActivity.this.getBaseContext(), isChecked);
			}
		});

	}
}