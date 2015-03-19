package com.cipherlab.cipherconnectpro;

import java.util.ArrayList;

import com.cipherlab.cipherconnect.sdk.ICipherConnBTDevice;
import com.cipherlab.cipherconnectpro.R;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LEDeviceScanActivity extends ListActivity {
	//constant 
	private static final int REQUEST_ENABLE_BT = 1;
	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	// Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    
	//data member
	private boolean mScanning = false;
	private Handler mScanPeriodHandler = new Handler();
	private	ICipherConnectManagerService mCipherConnectService = null;
	private	BluetoothAdapter mBluetoothAdapter = null;
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
    
	//On Events begin
    private ServiceConnection mSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mCipherConnectService = ((CipherConnectManagerService.LocalBinder) service).getService();
            if(mCipherConnectService != null) {
	            // Use this check to determine whether BLE is supported on the device.  Then you can
	            // selectively disable BLE-related features.
	            if (!mCipherConnectService.IsBLEModeSupported()) {
	                Toast.makeText(LEDeviceScanActivity.this, "onServiceConnected, mCipherConnectService.IsBLEModeSupported() = false", Toast.LENGTH_SHORT).show();
	                finish();
	                return;
	            }
	            mCipherConnectService.AddListener(mconnServiceMgrListener);
	
	            // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
	            // BluetoothAdapter through BluetoothManager.
	            final BluetoothManager bluetoothManager =
	                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
	            mBluetoothAdapter = bluetoothManager.getAdapter();
	
	            // Checks if Bluetooth is supported on the device.
	            if (mBluetoothAdapter == null) {
	                Toast.makeText(LEDeviceScanActivity.this, "onServiceConnected, mBluetoothAdapter == null", Toast.LENGTH_SHORT).show();
	                finish();
	                return;
	            }         
	            
	            scanLeDevice(true);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mCipherConnectService = null;
        }
    };
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final ICipherConnBTDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, LEDeviceScanActivity.class);
        intent.putExtra(EXTRAS_DEVICE_NAME, device.getDeviceName());
        LEDeviceScanActivity.this.setResult(CipherConnectSettingActivity.LE_BLUETOOTH_SETTINGS, intent);
        if (mScanning && mCipherConnectService != null) {
        	mCipherConnectService.StopScanLEDevices();
            mScanning = false;
        }
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
	//On Events end
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getActionBar().setTitle(R.string.LE_scan_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        /* [Begin] bind CipherConnectManagerService, will trigger onServiceConnected if bound successful*/ 
        try {
            Intent intent = new Intent(this, CipherConnectManagerService.class);
            this.startService(intent);
            this.bindService(intent, mSConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            finish();
        }
        /* [End] bind CipherConnectManagerService */
    }
	
    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
    }
	
    @Override
    protected void onPause() {
        super.onPause();
        mScanPeriodHandler.removeCallbacksAndMessages(null);
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
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
}
