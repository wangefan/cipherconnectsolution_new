package com.cipherlab.help;

import android.util.Log;

public class CipherLog 
{
	private static boolean _DEBUG = false; 
	public static void d(String tag, String msg) {
		if(_DEBUG)
			Log.d(tag, msg);
    }
	
	public static void d(String tag, String msg, Throwable tr) {
		if(_DEBUG)
			Log.d(tag, msg, tr);
    }
	
	public static void e(String tag, String msg) {
		if(_DEBUG)
			Log.e(tag, msg);
    }
	
	public static void e(String tag, String msg, Throwable tr) {
		if(_DEBUG)
			Log.e(tag, msg, tr);
    }
}
