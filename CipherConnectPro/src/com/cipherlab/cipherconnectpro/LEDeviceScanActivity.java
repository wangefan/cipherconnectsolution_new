package com.cipherlab.cipherconnectpro;

import java.util.ArrayList;

import com.cipherlab.cipherconnect2.sdk.ICipherConnBTDevice;
import com.cipherlab.cipherconnectpro.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LEDeviceScanActivity extends BTSettingActivity {
	//constant 
	private static final long SCAN_PERIOD = 10000; // Stops scanning after 10 seconds.
    
	//data member
	private boolean mScanning = false;
	private Handler mScanPeriodHandler = new Handler();
	private LeDeviceListAdapter mLeDeviceListAdapter = null;
	
	//Listener for listen ConnectManagerService
	private ICipherConnectManagerListener mconnServiceMgrListener = new ICipherConnectManagerListener() {
	    public void onBarcode(String barcode) {
	    	
	    }
	    
	    public void onConnecting() {
	    	
	    }
	    
	    public void onConnected() {
	    	
	    }
	    
	    public void onConnectError(String message) {
	    	
	    }
	    
	    public void onDisconnected() {
	    	
	    }
	    
	    public void onGetLEDevice(final ICipherConnBTDevice device) {
	    	runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
	    	
	    }
	};
	
	// Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<ICipherConnBTDevice> mLeDevices;	
        private LayoutInflater mInflator;
        class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
        }
        
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<ICipherConnBTDevice>();
            mInflator = LEDeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(ICipherConnBTDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public void clear() {
            mLeDevices.clear();
        }
        
        public ICipherConnBTDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
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

            ICipherConnBTDevice device = mLeDevices.get(i);
            final String deviceName = device.getDeviceName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }
    
    //member functions
    private void scanLeDevice(final boolean enable) {
    	try {
    		if(mCipherConnectService != null) {
	    		if (enable) {
		            // Stops scanning after a pre-defined scan period.
		        	mScanPeriodHandler.postDelayed(new Runnable() {
		                @Override
		                public void run() {
		                    mScanning = false;
		                    mCipherConnectService.StopScanLEDevices();
		                    invalidateOptionsMenu();
		                }
		            }, SCAN_PERIOD);
		     
		            //will start scan a period times ad receive devices under "onGetLEDevice".
		        	mCipherConnectService.StartScanLEDevices();
		        	mScanning = true;  
		        } else {
		    		mCipherConnectService.StopScanLEDevices();
		    		mScanning = false;
		        }
    		}
        }
        catch (Exception e) {
        	Toast.makeText(this, "scanLeDevice(" + enable +") fail, exception " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
        invalidateOptionsMenu();	//trigger  onCreateOptionsMenu
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final ICipherConnBTDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        Intent resultInt = new Intent();
        resultInt.putExtra(CipherConnectSettingActivity.KEY_GET_LE_BT_DEVICE, device);
        setResult(RESULT_OK, resultInt);
        onBackPressed();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }
    
    @Override
	protected String getTag() {
		return "LEDeviceScanActivity";
	}

	@Override
	protected void mDoThingsOnServiceConnected() 
	{
		// Initializes list view adapter.
		if (!mCipherConnectService.IsBLEModeSupported()) {
            Toast.makeText(LEDeviceScanActivity.this, "onServiceConnected, mCipherConnectService.IsBLEModeSupported() = false", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
		mCipherConnectService.AddListener(mconnServiceMgrListener);
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
		scanLeDevice(true);	
	}

	@Override
	protected void mDoThingsAtrEnableBTActy() 
	{
		scanLeDevice(true);	
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getActionBar().setTitle(R.string.LE_scan_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
	
    @Override
    protected void onPause() {
        super.onPause();
        mScanPeriodHandler.removeCallbacksAndMessages(null);
        scanLeDevice(false);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.le_menu, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }
    
    @Override
	protected void onDestroy() 
    {
        super.onDestroy();
    }
}
