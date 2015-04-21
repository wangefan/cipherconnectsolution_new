package com.cipherlab.cipherconnectpro2;

import com.cipherlab.cipherconnectpro2.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public abstract class BTSettingActivity extends ListActivity 
{
	final static String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
	
	abstract protected String getTag();
	
	//This method be called after service connected and ensure that BT is turn on, connect service is bound.
	abstract protected void mDoThingsOnServiceConnected();
	
	//This method be called after requesting turning on and allow BT.
	abstract protected void mDoThingsAtrEnableBTActy();
	
	//constant 
	private static final int REQUEST_ENABLE_BT = 1;
	
	//Members
	protected BluetoothAdapter mBluetoothAdapter;
	protected ICipherConnectManagerService mCipherConnectService = null;
	
	//member funcitons
    private void mDoBTIntentForResult()
    {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
    
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private static IntentFilter makeBTActionsIntentFilter() 
	{
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);  
		intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
		return intentFilter;
	}
	 
	private ServiceConnection mSConnection = new ServiceConnection() 
	{
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			Log.d(getTag(), "onServiceConnected, get mListenConnService and set SetKeepService true");
			
	      	// Initializes a Bluetooth adapter.  For API level 18 and above, get a reference t
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mCipherConnectService = ((CipherConnectManagerService.LocalBinder) service).getService();
            // Checks if Bluetooth is supported on the device.
            if (mBluetoothAdapter == null) {
                Toast.makeText(BTSettingActivity.this, "onServiceConnected, mBluetoothAdapter == null", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }      
            
            if (mCipherConnectService == null) {
            	Toast.makeText(BTSettingActivity.this, "onServiceConnected, mCipherConnectService == null", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }    

			if(mBluetoothAdapter.isEnabled())
			{
				mDoThingsOnServiceConnected();
			}
        }

        public void onServiceDisconnected(ComponentName className) 
        {
        	Log.d(getTag(), "onServiceDisconnected, set SetKeepService false");
        	mCipherConnectService = null;
        }
	};
	
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
	    			final String strAutoTryPin = "0000";
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
    
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_CANCELED)
            {
            	finish();
                return;
            }
            else	//allow
            {
            	mDoThingsAtrEnableBTActy();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState); 
        
        /* [Begin] bind CipherConnectManagerService, will trigger onServiceConnected if bound successful*/
        try {
            Intent intent = new Intent(this, CipherConnectManagerService.class);
            bindService(intent, mSConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
        	e.printStackTrace();
            finish();
        }
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
	}
	
	@Override
	protected void onPause() 
	{
		unregisterReceiver(mBTActReceiver);
		super.onPause();
	}
	
	@Override
	protected void onDestroy() 
	{	
		if(mSConnection != null)
		{
			unbindService(mSConnection);
			mSConnection = null;
		}
		
		mCipherConnectService = null;
		super.onDestroy();
	}
}
