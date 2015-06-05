package com.cipherlab.cipherconnectpro2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CipherConnectSettingInfo {
	public static final String TAG = "CipherConnectSettingInfo";

    public static final String _NAME = "CipherConnect";
    private static SharedPreferences _sp = null;

    public static SharedPreferences getSharedPreferences(Context c) {
        if (_sp==null) {
            _sp = c.getSharedPreferences(_NAME, 0);
        }
        return _sp;
    }

    public static String getLastDeviceName(Context c) {
        return getSharedPreferences(c).getString("LastDeviceName", "No select");
    }
    
    public static String getBarcodeInterval(Context c) {
        return getSharedPreferences(c).getString("BarcodeInterval", "No select");
    }

    public static void setBarcodeInterval(String mBarcodeInterval, Context c) {
        Editor editor = getSharedPreferences(c).edit();
        editor.putString("BarcodeInterval", mBarcodeInterval);
        editor.commit();
    }

    public static String getLanguage(Context c) {
        return getSharedPreferences(c).getString("Language", "No select");
    }

    public static void setLanguage(String mLanguage, Context c) {
        Editor editor = getSharedPreferences(c).edit();
        editor.putString("Language", mLanguage);
        editor.commit();
    }
    
    public static String getBTMode(Context c) {
        return getSharedPreferences(c).getString("BTMode", "Classic");
    }

    public static void setBTMode(String strMode, Context c) {
        Editor editor = getSharedPreferences(c).edit();
        editor.putString("BTMode", strMode);
        editor.commit();
    }

    public static boolean isAutoConnect(Context c) {
        return getSharedPreferences(c).getBoolean("AutoConnect", false);
    }

    public static void setAutoConnect(boolean enable, Context c) {
        Editor editor = getSharedPreferences(c).edit();
        editor.putBoolean("AutoConnect", enable);
        editor.commit();
    }
	
    public static boolean isMinimum(Context c) {
    	return getSharedPreferences(c).getBoolean("Minimum", false);
    }

    public static void setMinimum(boolean enable, Context c) {
        Editor editor = getSharedPreferences(c).edit();
        editor.putBoolean("Minimum", enable);
        editor.commit();
    }
    
    public static boolean isSuspendBacklight(Context c) {
        return getSharedPreferences(c).getBoolean("WakeLock", false);
    }

    public static void setSuspendBacklight(boolean enable, Context c) {
        Editor editor = getSharedPreferences(c).edit();
        editor.putBoolean("WakeLock", enable);
        editor.commit();
    }
    
	final public static String SLAVE = "slave";
	final public static String SLAVEQR = "slave_QR";
	final public static String MASTER = "master";
	
    public static String getBCMode(Context c) {
        return getSharedPreferences(c).getString("BCMode", SLAVE);
    }

    public static void setBCMode(String strMode, Context c) {
        Editor editor = getSharedPreferences(c).edit();
        editor.putString("BCMode", strMode);
        editor.commit();
    }
    
    public static void destroy() {
		_sp = null;
	}
}
