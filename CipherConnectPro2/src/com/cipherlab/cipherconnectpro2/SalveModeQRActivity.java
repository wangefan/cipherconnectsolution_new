package com.cipherlab.cipherconnectpro2;

import com.cipherlab.cipherconnectpro2.R;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SalveModeQRActivity extends BTSettingActivity 
{
	final static String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
	
	private ImageView mBCodeResetImage;
	private TextView mTvwResetBCode;
	private ImageView mQRCodeConnImage;
	private TextView mTvwQRCodeConn;
	private ImageView mImageBTConn;
	private TextView mTvwDeviceName;
	private ProgressDialog mPDialog = null;
	
	protected void mDoThingsOnServiceConnected()
	{
		mStartListenService();
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
            		Toast.makeText(SalveModeQRActivity.this, R.string.strWaitConnOn, Toast.LENGTH_LONG).show();
            		break;
            	case SERVER_STATE_OFFLINE:
            		Toast.makeText(SalveModeQRActivity.this, R.string.strWaitConnOff, Toast.LENGTH_LONG).show();
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
            		Toast.makeText(SalveModeQRActivity.this, strMag, Toast.LENGTH_LONG).show();
            	}
            	break;
            	case CONN_STATE_CONNECTERR:
            		Toast.makeText(SalveModeQRActivity.this, R.string.strConnectErr, Toast.LENGTH_LONG).show();
            	default:
            	break;
            	}
            	mUpdateUI(); 
            }
            else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
            	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); 
                
                switch (device.getBondState())
                { 
                case BluetoothDevice.BOND_BONDING: 
                	try {
	                    Log.d("BlueToothTestActivity", "Pairing......"); 
                	} catch (Exception e) {
    					// TODO Auto-generated catch block
    					Toast.makeText(context, "auto-pair Exception...", Toast.LENGTH_SHORT).show();
    				}
                    break; 
                case BluetoothDevice.BOND_BONDED: 
                    Log.d("BlueToothTestActivity", "Pair done"); 
                    break; 
                case BluetoothDevice.BOND_NONE: 
                    Log.d("BlueToothTestActivity", "Cancel pairing"); 
                default: 
                    break; 
                } 
            }else if (intent.getAction().equals(ACTION_PAIRING_REQUEST)) 
            {
            	try {
	    			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	    			final String strAutoTryPin = "1";
	    			if (device.getBondState() != BluetoothDevice.BOND_BONDED) 
	    			{
	    				device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
	    				ClsUtils.setPin(device.getClass(), device, strAutoTryPin); 
	    			}
            	} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(context, "auto-pair error Exception", Toast.LENGTH_SHORT).show();
				}
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
    			mTvwResetBCode.setText(R.string.strResetConn);
    			mTvwResetBCode.setVisibility(View.GONE);
    			
    			Bitmap bmpQRCodeConn = mCipherConnectService.GetSettingConnQRcodeImage((int)fQRWidthPxl, (int)fQRHeightPxl);
    			mQRCodeConnImage.setImageBitmap(bmpQRCodeConn);
    			mQRCodeConnImage.setVisibility(View.VISIBLE);
    			mTvwQRCodeConn.setVisibility( View.VISIBLE);
    			
    			mImageBTConn.setImageResource(R.drawable.btdisconnect);
    			mTvwDeviceName.setText(R.string.strWaitConnOn);
    		}
    		break;
    		case  SERVER_STATE_OFFLINE:
    		default:
    		{	
    			mBCodeResetImage.setVisibility(View.INVISIBLE);
    			mTvwResetBCode.setVisibility(View.INVISIBLE);
    			mTvwQRCodeConn.setVisibility( View.INVISIBLE);
    			mQRCodeConnImage.setVisibility(View.INVISIBLE);
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
    			mBCodeResetImage.setVisibility(View.VISIBLE);
    			mTvwResetBCode.setText(R.string.strDisconnect);
    			mTvwResetBCode.setVisibility(View.VISIBLE);
    			mQRCodeConnImage.setVisibility(View.INVISIBLE);
    			mTvwQRCodeConn.setVisibility( View.INVISIBLE);
    			mImageBTConn.setImageResource(R.drawable.btconnected);	
    			String strMag = mCipherConnectService.GetConnDevice().getDeviceName() + " connected";
    			mTvwDeviceName.setText(strMag);  			
    			ShowProgressDlg(false);			
    		}
    		default:
    		break;
    	}  	
    }
    
    private void mStartListenService()
    {
        if(mCipherConnectService != null)
        	mCipherConnectService.StartListenConn();	
    }
    
    private static IntentFilter makeActionsIntentFilter() {
    	final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED);
        intentFilter.addAction(CipherConnectManagerService.ACTION_SERVER_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //intentFilter.addAction(ACTION_PAIRING_REQUEST);
        return intentFilter;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.salve_mode_qr_activity);
        
		//Init UI
		mBCodeResetImage = (ImageView)findViewById(R.id.imageResetConn);
		mTvwResetBCode = (TextView) findViewById(R.id.tvwReset);
		
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
	        mStartListenService();
	        mUpdateUI();
		}
	}

	//This method be called after requesting turning on and allow BT.
	protected void mDoThingsAtrEnableBTActy()
	{
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			mStartListenService();
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
        if(mCipherConnectService != null)
        {
        	mCipherConnectService.StopListenConn();	
        	mCipherConnectService = null;
        }
		super.onDestroy();
	}

	@Override
	protected String getTag() {
		return "SalveModeActivity";
	}
}