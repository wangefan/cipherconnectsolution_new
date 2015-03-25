package com.cipherlab.cipherconnectpro;

import java.util.ArrayList;

import com.cipherlab.cipherconnect.sdk.ICipherConnBTDevice;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ClassicBTDeviceScanActivity extends BTSettingActivity 
{
	//Data members
	private ClassicDeviceListAdapter mClassicDeviceListAdapter = null;
	
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
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        
        getActionBar().setTitle(R.string.Classic_scan_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
	
	@Override
 	protected void onDestroy() 
    {
		mClassicDeviceListAdapter = null;
		super.onDestroy();
    }

	@Override
	protected String getTag() 
	{
		return "ClassicBTDeviceScanActivity";
	}
}
