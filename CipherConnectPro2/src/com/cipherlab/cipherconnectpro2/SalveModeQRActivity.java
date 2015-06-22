package com.cipherlab.cipherconnectpro2;

import com.cipherlab.cipherconnectpro2.R;
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

public class SalveModeQRActivity extends BTSettingActivity 
{	
	private ImageView mBCodeResetImage;
	
	private ImageView mQRCodeConnImage;
	private TextView mTvwQRCodeConn;
	private ImageView mImageBTConn;
	private TextView mTvwDeviceName;
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
            		Toast.makeText(SalveModeQRActivity.this, strMag, Toast.LENGTH_LONG).show();
            	}
            	break;
            	case CONN_STATE_CONNECTERR:
            	{
            		Toast.makeText(SalveModeQRActivity.this, R.string.setting_bluetooth_device_disconnected, Toast.LENGTH_LONG).show();
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
		
		float fQRWidthPxl = 90.0f;
		float fQRHeightPxl = 90.0f;
		Resources rsc = getResources();
		if(rsc != null)
		{
			fWidthPxl = rsc.getDimension(R.dimen.BarcodeWidth);
			fHeightPxl = rsc.getDimension(R.dimen.BarcodeHeight);
			fQRWidthPxl = rsc.getDimension(R.dimen.QRcodeWidth);
			fQRHeightPxl = rsc.getDimension(R.dimen.QRcodeHeight);
		}
				
		Bitmap bmpReset = mCipherConnectService.GetResetConnBarcodeImage((int)fWidthPxl, (int)fHeightPxl);
		mBCodeResetImage.setImageBitmap(bmpReset);
		mBCodeResetImage.setVisibility(View.GONE);
		
		
		Bitmap bmpQRCodeConn = mCipherConnectService.GetSettingConnQRcodeImage((int)fQRWidthPxl, (int)fQRHeightPxl);
		mQRCodeConnImage.setImageBitmap(bmpQRCodeConn);
		mQRCodeConnImage.setVisibility(View.VISIBLE);
		mTvwQRCodeConn.setVisibility( View.VISIBLE);
		
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
		setContentView(R.layout.salve_mode_qr_activity);
		getActionBar().setDisplayHomeAsUpEnabled(true);
        
		//Init UI
		mBCodeResetImage = (ImageView)findViewById(R.id.imageResetConn);
		
		
		mQRCodeConnImage = (ImageView)findViewById(R.id.imageQRCodeConn);
		mTvwQRCodeConn = (TextView) findViewById(R.id.tvwQRConn);
		
		mImageBTConn = (ImageView)findViewById(R.id.imageBTConn);
		mTvwDeviceName = (TextView) findViewById(R.id.tvwDeviceName);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(mServerActReceiver, makeActionsIntentFilter());
	}

	@Override
	protected void onResume() {
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
		super.onPause();
	}

	@Override
	protected void onStop() {
		unregisterReceiver(mServerActReceiver);
		super.onStop();
	}

	@Override
	protected void onDestroy() 
	{	
        mCipherConnectService = null;
		super.onDestroy();
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
