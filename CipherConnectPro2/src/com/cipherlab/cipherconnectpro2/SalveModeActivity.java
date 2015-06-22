package com.cipherlab.cipherconnectpro2;

import com.cipherlab.cipherconnectpro2.R;
import com.cipherlab.help.CipherLog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SalveModeActivity extends BTSettingActivity 
{	
	private final String mTAG = "SlaveModeActivity";
	private TextView mTvwDeviceName;
	private ImageView mBCodeSettingConnImage;
	private TextView mTvwSetConnBCode;
	private ImageView mBCodeAddressImage;
	private TextView mTvwBCodeAddress;
	private ImageView mImageBTConn;
	private ProgressDialog mPDialog = null;
	
	protected void mDoThingsOnServiceConnected()
	{
		mUpdateUI();
	}

	/*
     * <!----------------------------------------------------------------->
     * @Name: mActReceiver
     * @Description: Receiver the connecnt service server state. 
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private BroadcastReceiver mServerActReceiver = new BroadcastReceiver()
	{
		@SuppressLint("InlinedApi")
		@Override
        public void onReceive(Context context, Intent intent) 
		{
        	final String action = intent.getAction();

            if(action.equals(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED))
            {
            	ICipherConnectManagerService.CONN_STATE conntate = mCipherConnectService.GetConnState();  	
            	switch (conntate)
            	{
            	case CONN_STATE_CONNECTED:
            	{
            		String strMag = mCipherConnectService.GetConnDevice().getDeviceName() + " connected";
            		Toast.makeText(SalveModeActivity.this, strMag, Toast.LENGTH_LONG).show();
            	}
            	break;
            	case CONN_STATE_CONNECTERR:
            	{
            		Toast.makeText(SalveModeActivity.this, R.string.setting_bluetooth_device_disconnected, Toast.LENGTH_LONG).show();
            		final String info = intent.getStringExtra(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED_KEY);
            		if(info != null && info.length() > 0)
                		Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
            	}
            	default:
            	break;
            	}
            	mUpdateUI(); 
            }
		}
    };
    
    private void ShowProgressDlg(boolean bShow)
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
	
    private void mUpdateUI()
    {
		if(mCipherConnectService == null)
    		return;
		
		float fWidthPxl = 300.0f;
		float fHeightPxl = 100.0f;
		
		Resources rsc = getResources();
		if(rsc != null)
		{
			fWidthPxl = rsc.getDimension(R.dimen.BarcodeWidth);
			fHeightPxl = rsc.getDimension(R.dimen.BarcodeHeight);
		}
		
		Bitmap bmpSetConn = mCipherConnectService.GetSettingConnBarcodeImage((int)fWidthPxl, (int)fHeightPxl);
		mBCodeSettingConnImage.setImageBitmap(bmpSetConn);
		mBCodeSettingConnImage.setVisibility(View.VISIBLE);
		mTvwSetConnBCode.setVisibility( View.VISIBLE);
		
		Bitmap bmpAddress = mCipherConnectService.GetMacAddrBarcodeImage((int)fWidthPxl, (int)fHeightPxl);
		mBCodeAddressImage.setImageBitmap(bmpAddress);
		mBCodeAddressImage.setVisibility(View.VISIBLE);
		mTvwBCodeAddress.setVisibility( View.VISIBLE);
		
		mImageBTConn.setImageResource(R.drawable.btdisconnect);
		mTvwDeviceName.setText(R.string.strWaitConnOn);	
		
    	ICipherConnectManagerService.CONN_STATE connState = mCipherConnectService.GetConnState();  	
    	switch (connState) 
    	{
    		case  CONN_STATE_BEGINCONNECTING:
    		case  CONN_STATE_CONNECTING:
    		{
    			mTvwDeviceName.setText(R.string.strConnecting);
    			ShowProgressDlg(true);
    		}
    		break;
    		case  CONN_STATE_DISCONNECT:
    		case  CONN_STATE_CONNECTERR:
    		{
    			ShowProgressDlg(false);
    		}
    		break;
    		case  CONN_STATE_CONNECTED:
    		{
    			ShowProgressDlg(false);
    			finish();
    		}
    		default:
    		break;
    	}  	
    }
    
    private static IntentFilter makeActionsIntentFilter() {
    	final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED);
        return intentFilter;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.salve_mode_activity);
		getActionBar().setDisplayHomeAsUpEnabled(true);
        
		//Init UI
		
		mBCodeSettingConnImage = (ImageView)findViewById(R.id.imageSetConn);
		mTvwSetConnBCode = (TextView) findViewById(R.id.tvwSetConn);
		
		mBCodeAddressImage = (ImageView)findViewById(R.id.imageMACAdd);
		mTvwBCodeAddress = (TextView) findViewById(R.id.tvwAddress);
		
		mImageBTConn = (ImageView)findViewById(R.id.imageBTConn);
		mTvwDeviceName = (TextView) findViewById(R.id.tvwDeviceName);
	}

	@Override
	protected void onStart() {
		CipherLog.d(mTAG, "onStart()");
		super.onStart();
		registerReceiver(mServerActReceiver, makeActionsIntentFilter());
	}

	@Override
	protected void onResume() {
		CipherLog.d(mTAG, "onResume()");
		super.onResume();
		
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
	        mUpdateUI();
		}
	}

	//This method be called after requesting turning on and allow BT.
	protected void mDoThingsAtrEnableBTActy()
	{
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			mUpdateUI();
		}
	}
	
	@Override
	protected void onPause() 
	{
		CipherLog.d(mTAG, "onPause()");
		super.onPause();
	}

	@Override
	protected void onStop() {
		unregisterReceiver(mServerActReceiver);
		super.onStop();
		CipherLog.d(mTAG, "onStop()");
	}

	@Override
	protected void onDestroy() 
	{	
       	mCipherConnectService = null;
		super.onDestroy();
		CipherLog.d(mTAG, "onDestroy()");
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	@Override
	protected String getTag() {
		return "SalveModeActivity";
	}
}
