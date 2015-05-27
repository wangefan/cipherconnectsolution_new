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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SalveModeActivity extends BTSettingActivity 
{	
	private TextView mTvwDeviceName;
	private ImageView mBCodeResetImage;
	private TextView mTvwResetBCode;
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

            if(action.equals(CipherConnectManagerService.ACTION_SERVER_STATE_CHANGED))
            {
            	ICipherConnectManagerService.SERVER_STATE servertate = mCipherConnectService.GetServerState();  	
            	switch (servertate)
            	{
            	case SERVER_STATE_ONLINE:
            		//Toast.makeText(SalveModeActivity.this, R.string.strWaitConnOn, Toast.LENGTH_LONG).show();
            		break;
            	case SERVER_STATE_OFFLINE:
            		Toast.makeText(SalveModeActivity.this, R.string.strWaitConnOff, Toast.LENGTH_LONG).show();
            		break;
				default:
					break;
            	}
            	mUpdateUI(); 	
            }
            else if(action.equals(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED))
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
            		Toast.makeText(SalveModeActivity.this, R.string.setting_bluetooth_device_disconnected, Toast.LENGTH_LONG).show();
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
		
		ICipherConnectManagerService.SERVER_STATE servertate = mCipherConnectService.GetServerState();  	
    	switch (servertate) 
    	{
    		case  SERVER_STATE_ONLINE:
    		{	   			
    			float fWidthPxl = 300.0f;
    			float fHeightPxl = 100.0f;
    			
    			Resources rsc = getResources();
    			if(rsc != null)
    			{
    				fWidthPxl = rsc.getDimension(R.dimen.BarcodeWidth);
    				fHeightPxl = rsc.getDimension(R.dimen.BarcodeHeight);
    			}
    					
    			Bitmap bmpReset = mCipherConnectService.GetResetConnBarcodeImage((int)fWidthPxl, (int)fHeightPxl);
    			mBCodeResetImage.setImageBitmap(bmpReset);
    			mBCodeResetImage.setVisibility(View.VISIBLE);
    			mTvwResetBCode.setText(R.string.strResetConn);
    			
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
    		}
    		break;
    		case  SERVER_STATE_OFFLINE:
    		default:
    		{	
    			mBCodeResetImage.setVisibility(View.INVISIBLE);
    			mTvwResetBCode.setVisibility(View.INVISIBLE);
    			mBCodeSettingConnImage.setVisibility( View.INVISIBLE);
    			mTvwSetConnBCode.setVisibility( View.INVISIBLE);
    			mBCodeAddressImage.setVisibility( View.INVISIBLE);
    			mTvwBCodeAddress.setVisibility( View.INVISIBLE);
    			mImageBTConn.setImageResource(R.drawable.btdisconnect);
    		}
    		break;
    	}  	
		
    	ICipherConnectManagerService.CONN_STATE connState = mCipherConnectService.GetConnState();  	
    	switch (connState) 
    	{
    		case  CONN_STATE_BEGINCONNECTING:
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
    			float fWidthPxl = 300.0f;
    			float fHeightPxl = 100.0f;
    			Resources rsc = getResources();
    			if(rsc != null)
    			{
    				fWidthPxl = rsc.getDimension(R.dimen.BarcodeWidth);
    				fHeightPxl = rsc.getDimension(R.dimen.BarcodeHeight);
    			}
    			Bitmap bmpReset = mCipherConnectService.GetResetConnBarcodeImage((int)fWidthPxl, (int)fHeightPxl);
    			mBCodeResetImage.setImageBitmap(bmpReset);
    			mTvwResetBCode.setText(R.string.strDisconnect);
    			mBCodeSettingConnImage.setVisibility( View.INVISIBLE);
    			mTvwSetConnBCode.setVisibility( View.INVISIBLE);
    			mBCodeAddressImage.setVisibility( View.INVISIBLE);
    			mTvwBCodeAddress.setVisibility( View.INVISIBLE);
    			mImageBTConn.setImageResource(R.drawable.btconnected);	
    			String strMag = mCipherConnectService.GetConnDevice().getDeviceName() + " connected";
    			mTvwDeviceName.setText(strMag);  			
    			ShowProgressDlg(false);			
    		}
    		default:
    		break;
    	}  	
    }
    
    private static IntentFilter makeActionsIntentFilter() {
    	final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED);
        intentFilter.addAction(CipherConnectManagerService.ACTION_SERVER_STATE_CHANGED);
        return intentFilter;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.salve_mode_activity);
        
		//Init UI
		mBCodeResetImage = (ImageView)findViewById(R.id.imageResetConn);
		mTvwResetBCode = (TextView) findViewById(R.id.tvwReset);
		
		mBCodeSettingConnImage = (ImageView)findViewById(R.id.imageSetConn);
		mTvwSetConnBCode = (TextView) findViewById(R.id.tvwSetConn);
		
		mBCodeAddressImage = (ImageView)findViewById(R.id.imageMACAdd);
		mTvwBCodeAddress = (TextView) findViewById(R.id.tvwAddress);
		
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
		super.onDestroy();
	}

	@Override
	protected String getTag() {
		return "SalveModeActivity";
	}
}
