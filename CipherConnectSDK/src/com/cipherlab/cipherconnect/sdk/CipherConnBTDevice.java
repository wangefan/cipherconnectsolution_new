package com.cipherlab.cipherconnect.sdk;

public class CipherConnBTDevice implements ICipherConnBTDevice {

	//Data members
	private String mStrDeviceName = null;
	private String mStrDeviceAddress = null;
	
	//constructor 
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
	
	@Override
	public String getDeviceName() {
		return mStrDeviceName;
	}

	@Override
	public String getAddress() {
		return mStrDeviceAddress;
	}

}
