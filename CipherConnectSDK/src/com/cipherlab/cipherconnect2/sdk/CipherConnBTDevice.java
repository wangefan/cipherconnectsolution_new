package com.cipherlab.cipherconnect2.sdk;

import android.bluetooth.BluetoothDevice;

public class CipherConnBTDevice implements ICipherConnBTDevice {

	//Data members
	private String mStrDeviceName = null;
	private String mStrDeviceAddress = null;
	
	//constructor 
	CipherConnBTDevice()
	{
		mStrDeviceName = "";
		mStrDeviceAddress = "";
	}
	
	CipherConnBTDevice(String strDeviceName, String strDeviceAddress)
	{
		mStrDeviceName = new String(strDeviceName);
		mStrDeviceAddress = new String(strDeviceAddress);
	}
	
	//constructor 
	CipherConnBTDevice(ICipherConnBTDevice src)
	{
		mStrDeviceName = new String(src.getDeviceName());
		mStrDeviceAddress = new String(src.getAddress());
	}
	
	CipherConnBTDevice(BluetoothDevice src)
	{
		mStrDeviceName = new String(src.getName());
		mStrDeviceAddress = new String(src.getAddress());
	}
	
	public void getParamFromBTDevice(BluetoothDevice src)
	{
		mStrDeviceName = src.getName();
		mStrDeviceAddress = src.getAddress();
	}
	
	@Override
	public String getDeviceName() {
		return mStrDeviceName;
	}

	@Override
	public String getAddress() {
		return mStrDeviceAddress;
	}

}
