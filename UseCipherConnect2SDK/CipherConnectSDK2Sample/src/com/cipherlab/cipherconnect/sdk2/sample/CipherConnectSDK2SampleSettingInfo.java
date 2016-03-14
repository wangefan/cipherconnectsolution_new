package com.cipherlab.cipherconnect.sdk2.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CipherConnectSDK2SampleSettingInfo {
	
	public static final String _NAME = "CipherConnectSDKSample";
	private static SharedPreferences _sp = null;
	
	public static void initSharedPreferences(Context c){
		if(_sp==null){
			_sp = c.getSharedPreferences(_NAME, 0);
		}
	}
	
	public static String getLastDeviceName(Context c) {
		initSharedPreferences(c);
		return _sp.getString("LastDeviceName", "No select");
	}
	
	public static void setLastDeviceName(Context c,String mDeviceName) {
		initSharedPreferences(c);
		Editor editor = _sp.edit();
		editor.putString("LastDeviceName", mDeviceName);
		editor.commit();
	}
	
	public static boolean isAutoReconnect(Context c){
		initSharedPreferences(c);
		return _sp.getBoolean("AutoReconnect", false);
		//return true;
	}
	
	public static void setAutoReconnect(Context c, boolean enable){
		initSharedPreferences(c);
		Editor editor = _sp.edit();
		editor.putBoolean("AutoReconnect", enable);
		editor.commit();
	}
	
	public static void destroy(){
		_sp = null;
	}
}
