package com.cipherlab.help;

import android.util.Log;

public class CipherLog 
{
	public static boolean _DEBUG = false; 
	public static void d(String tag, String msg) {
		if(_DEBUG)
			Log.d("CipherConnectPro2", "[" + tag + "]:" + msg);		
    }
	
	public static void d(String tag, String msg, Throwable tr) {
		if(_DEBUG)
			Log.d("CipherConnectPro2", "[" + tag + "]:" + msg, tr);
    }
	
	public static void e(String tag, String msg) {
		if(_DEBUG)
			Log.e("CipherConnectPro2", "[" + tag + "]:" + msg);
    }
	
	public static void e(String tag, String msg, Throwable tr) {
		if(_DEBUG)
			Log.e("CipherConnectPro2", "[" + tag + "]:" + msg, tr);
    }
}
