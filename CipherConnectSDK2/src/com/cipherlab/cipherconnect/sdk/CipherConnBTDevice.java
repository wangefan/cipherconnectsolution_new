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
	
	public String getDeviceName() {
		return mStrDeviceName;
	}

	public String getAddress() {
		return mStrDeviceAddress;
	}

}
