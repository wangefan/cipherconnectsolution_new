package com.cipherlab.cipherconnectpro;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SalveModeActivity extends Activity 
{
	private String mTag = "SalveModeActivity";
	private BluetoothAdapter mBluetoothAdapter;
	private TextView mTvwDeviceName;
	private ImageView mBCodeResetImage;
	private TextView mTvwResetBCode;
	private ImageView mBCodeSettingConnImage;
	private TextView mTvwSetConnBCode;
	private ImageView mBCodeAddressImage;
	private TextView mTvwBCodeAddress;
	private ImageView mImageBTConn;
	private ProgressDialog mPDialog = null;
	private ICipherConnectManagerService mListenConnService = null;
	private ServiceConnection mListenConnServiceConn = new ServiceConnection() 
	{
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			Log.d(mTag, "onServiceConnected, get mListenConnService and set SetKeepService true");
			mListenConnService = ((CipherConnectManagerService.LocalBinder) service).getService();
			if(mListenConnService == null)
			{
				Toast.makeText(SalveModeActivity.this, R.string.strServiceFail, Toast.LENGTH_LONG).show();
			}
			else
			{
				mStartListenService();
				mUpdateUI();
			}
        }

        public void onServiceDisconnected(ComponentName className) 
        {
        	Log.d(mTag, "onServiceDisconnected, set SetKeepService false");
        	mListenConnService.StopListenConn();
        	mListenConnService = null;
        }
	};
	
	//constant 
	private static final int REQUEST_ENABLE_BT = 1;

	/*
     * <!----------------------------------------------------------------->
     * @Name: mBTActReceiver
     * @Description: Receiver the Bluetooth Turn on/off event. 
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
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
                    
                } 
                else if (state == BluetoothAdapter.STATE_OFF) 
                {
                	if (!mBluetoothAdapter.isEnabled()) {
                		mDoBTIntentForResult();
                        return;
                    }
                }
            }
            else if(action.equals(CipherConnectManagerService.ACTION_SERVER_STATE_CHANGED))
            {
            	ICipherConnectManagerService.SERVER_STATE servertate = mListenConnService.GetServerState();  	
            	switch (servertate)
            	{
            	case SERVER_STATE_ONLINE:
            		Toast.makeText(SalveModeActivity.this, R.string.strServiceOn, Toast.LENGTH_LONG).show();
            		break;
            	case SERVER_STATE_OFFLINE:
            		Toast.makeText(SalveModeActivity.this, R.string.strServiceOff, Toast.LENGTH_LONG).show();
            		break;
            	}
            	mUpdateUI(); 	
            }
            else if(action.equals(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED))
            {
            	ICipherConnectManagerService.CONN_STATE conntate = mListenConnService.GetConnState();  	
            	switch (conntate)
            	{
            	case CONN_STATE_CONNECTED:
            	{
            		String strMag = mListenConnService.GetConnDeviceName() + " connected";
            		Toast.makeText(SalveModeActivity.this, strMag, Toast.LENGTH_LONG).show();
            	}
            	break;
            	case CONN_STATE_CONNECTERR:
            		Toast.makeText(SalveModeActivity.this, R.string.strConnectErr, Toast.LENGTH_LONG).show();
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
		if(mListenConnService == null)
    		return;
		
		ICipherConnectManagerService.SERVER_STATE servertate = mListenConnService.GetServerState();  	
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
    					
    			Bitmap bmpReset = mListenConnService.GetResetConnBarcodeImage((int)fWidthPxl, (int)fHeightPxl);
    			mBCodeResetImage.setImageBitmap(bmpReset);
    			mBCodeResetImage.setVisibility(View.VISIBLE);
    			mTvwResetBCode.setText(R.string.strResetConn);
    			
    			Bitmap bmpSetConn = mListenConnService.GetSettingConnBarcodeImage((int)fWidthPxl, (int)fHeightPxl);
    			mBCodeSettingConnImage.setImageBitmap(bmpSetConn);
    			mBCodeSettingConnImage.setVisibility(View.VISIBLE);
    			mTvwSetConnBCode.setVisibility( View.VISIBLE);
    			
    			Bitmap bmpAddress = mListenConnService.GetMacAddrBarcodeImage((int)fWidthPxl, (int)fHeightPxl);
    			mBCodeAddressImage.setImageBitmap(bmpAddress);
    			mBCodeAddressImage.setVisibility(View.VISIBLE);
    			mTvwBCodeAddress.setVisibility( View.VISIBLE);
    			
    			mImageBTConn.setImageResource(R.drawable.btdisconnect);
    			mTvwDeviceName.setText(R.string.strServiceOn);
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
		
    	ICipherConnectManagerService.CONN_STATE connState = mListenConnService.GetConnState();  	
    	switch (connState) 
    	{
    		case  CONN_STATE_BEGINCONNECTING:
    		{
    			mTvwDeviceName.setText(R.string.strConnecting);
    			ShowProgressDlg(true);
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
    			Bitmap bmpReset = mListenConnService.GetResetConnBarcodeImage((int)fWidthPxl, (int)fHeightPxl);
    			mBCodeResetImage.setImageBitmap(bmpReset);
    			mTvwResetBCode.setText(R.string.strDisconnect);
    			mBCodeSettingConnImage.setVisibility( View.INVISIBLE);
    			mTvwSetConnBCode.setVisibility( View.INVISIBLE);
    			mBCodeAddressImage.setVisibility( View.INVISIBLE);
    			mTvwBCodeAddress.setVisibility( View.INVISIBLE);
    			mImageBTConn.setImageResource(R.drawable.btconnected);	
    			String strMag = mListenConnService.GetConnDeviceName() + " connected";
    			mTvwDeviceName.setText(strMag);  			
    			ShowProgressDlg(false);			
    		}
    		default:
    		break;
    	}  	
    }
    
    private void mStartListenService()
    {
        if(mListenConnService != null)
        	mListenConnService.StartListenConn();	
    }
	
    private void mDoBTIntentForResult()
    {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
    
    private static IntentFilter makeBTActionsIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
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
		
		//Bind Listen service
		try {
			Intent intent = new Intent(this, CipherConnectManagerService.class);
	        startService(intent);
	        bindService(intent, mListenConnServiceConn, Context.BIND_AUTO_CREATE);		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mBTActReceiver, makeBTActionsIntentFilter());
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null)
		{
			Toast.makeText(this, R.string.error_bluetooth_not_turnon, Toast.LENGTH_SHORT).show();
            finish();
            return;
		}
		
		// Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
        	mDoBTIntentForResult();
            return;
        }
        
        mStartListenService();
        mUpdateUI();
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        mStartListenService();
        mUpdateUI();
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	@Override
	protected void onPause() 
	{
		unregisterReceiver(mBTActReceiver);
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() 
	{	
		if(mListenConnServiceConn != null)
		{
			unbindService(mListenConnServiceConn);
			mListenConnServiceConn = null;
		}
		
		if(mListenConnService != null)
		{
			mListenConnService.StopListenConn();
			mListenConnService = null;
		}
		super.onDestroy();
	}
}
