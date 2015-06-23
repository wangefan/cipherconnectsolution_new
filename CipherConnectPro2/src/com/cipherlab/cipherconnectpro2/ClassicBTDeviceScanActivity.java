package com.cipherlab.cipherconnectpro2;

import java.util.ArrayList;

import com.cipherlab.cipherconnect2.sdk.ICipherConnBTDevice;
import com.cipherlab.cipherconnectpro2.R;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ClassicBTDeviceScanActivity extends BTSettingActivity 
{
	//constant
	public static final int CLASSIC_BLUETOOTH_SETTINGS = 2;
	//Data members
	private ClassicDeviceListAdapter mClassicDeviceListAdapter = null;
	private ProgressDialog mPDialog = null;
	
	//Member functions
    private void mFillBTAdaperList()
    {
    	if(mClassicDeviceListAdapter != null && mBluetoothAdapter != null)
    	{
        	if(mBluetoothAdapter.isEnabled())
            {
        		mClassicDeviceListAdapter.clear();
              	ICipherConnBTDevice[] devices = mCipherConnectService.getBtDevices();
            	for(ICipherConnBTDevice device: devices)
            		mClassicDeviceListAdapter.addDevice(device);
            	mClassicDeviceListAdapter.notifyDataSetChanged();
            }	
    	}	
    }
    
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
    
    private void updateUIByConnService()
    {
    	if(mCipherConnectService != null)
    	{
    		ICipherConnectManagerService.CONN_STATE conntate = mCipherConnectService.GetConnState();  	
        	switch (conntate)
        	{
        	case  CONN_STATE_BEGINCONNECTING:
    		case  CONN_STATE_CONNECTING:
    		{
    			ShowProgressDlg(true);
    		}
    		break;
    		case CONN_STATE_CONNECTED:
    			onBackPressed();
    		break;
    		case  CONN_STATE_DISCONNECT:
    		case  CONN_STATE_CONNECTERR:
    		default:
    		{
    			ShowProgressDlg(false);
    		}
        	}	
    	} 	
    }
    
    private BroadcastReceiver mConnServiceActReceiver = new BroadcastReceiver()
	{
		@Override
        public void onReceive(Context context, Intent intent) 
		{
        	final String action = intent.getAction();
        	if(action.equals(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED))
            {
        		updateUIByConnService();
            }
		}
    };
    
    // 	Adapter for holding devices found 
    private class ClassicDeviceListAdapter extends BaseAdapter {
        private ArrayList<ICipherConnBTDevice> mClassicDevices;	
        private LayoutInflater mInflator;
        class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
        }
        
        public ClassicDeviceListAdapter() {
            super();
            mClassicDevices = new ArrayList<ICipherConnBTDevice>();
            mInflator = ClassicBTDeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(ICipherConnBTDevice device) {
            if(!mClassicDevices.contains(device)) {
            	mClassicDevices.add(device);
            }
        }

        public void clear() {
        	mClassicDevices.clear();
        }
        
        public ICipherConnBTDevice getDevice(int position) {
            return mClassicDevices.get(position);
        }

        @Override
        public int getCount() {
            return mClassicDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mClassicDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_ledevice, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            ICipherConnBTDevice device = mClassicDevices.get(i);
            final String deviceName = device.getDeviceName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }
    
    @Override
	protected void mDoThingsOnServiceConnected()
	{
    	mClassicDeviceListAdapter = new ClassicDeviceListAdapter();
        setListAdapter(mClassicDeviceListAdapter);
		mFillBTAdaperList();
	}

	@Override
	protected void mDoThingsAtrEnableBTActy() 
	{
    	mClassicDeviceListAdapter = new ClassicDeviceListAdapter();
        setListAdapter(mClassicDeviceListAdapter);
		mFillBTAdaperList();
	}
    
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final ICipherConnBTDevice device = mClassicDeviceListAdapter.getDevice(position);
        if (device == null) return;
        Intent resultInt = new Intent();
        resultInt.putExtra(CipherConnectSettingActivity.KEY_GET_CLSC_BT_DEVICE, device);
        setResult(RESULT_OK, resultInt);
        onBackPressed();
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.classic_menu, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_scan:
	        {
	        	Intent intentBluetooth = new Intent();
	    		intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
	    		startActivityForResult(intentBluetooth, CLASSIC_BLUETOOTH_SETTINGS); 
	        }
	            
	        break;
	        case android.R.id.home:
	            onBackPressed();
	            return true;
	    }
	    return true;
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        switch (requestCode) {
        case CLASSIC_BLUETOOTH_SETTINGS :
        {
        	mFillBTAdaperList();      
        }
        default:
        	super.onActivityResult(requestCode, resultCode, data);
        }        
    }
	 
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        
        getActionBar().setTitle(R.string.Classic_scan_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
	
	@Override
 	protected void onDestroy() 
    {
		if(mClassicDeviceListAdapter != null)
		{
			mClassicDeviceListAdapter.clear();
			mClassicDeviceListAdapter = null;
		}
		super.onDestroy();
    }

	@Override
	protected String getTag() 
	{
		return "ClassicBTDeviceScanActivity";
	}

	@Override
	protected void onResume() {
		updateUIByConnService();
		
		final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CipherConnectManagerService.ACTION_CONN_STATE_CHANGED);
		registerReceiver(mConnServiceActReceiver, intentFilter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mConnServiceActReceiver);
		super.onPause();
	}
}
