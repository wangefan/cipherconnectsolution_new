package com.cipherlab.cipherconnectpro2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CipherConnectSettingInfo {
	public static final String TAG = "CipherConnectSettingInfo";

    public static final String _NAME = "CipherConnect";
    private static SharedPreferences _sp = null;

    public static void initSharedPreferences(Context c) {
        if (_sp==null) {
            _sp = c.getSharedPreferences(_NAME, 0);
        }
    }

    public static String getLastDeviceName() {
        return _sp.getString("LastDeviceName", "No select");
    }
    
    public static String getBarcodeInterval() {
        return _sp.getString("BarcodeInterval", "No select");
    }

    public static void setBarcodeInterval(String mBarcodeInterval) {
        Editor editor = _sp.edit();
        editor.putString("BarcodeInterval", mBarcodeInterval);
        editor.commit();
    }

    public static String getLanguage() {
        return _sp.getString("Language", "No select");
    }

    public static void setLanguage(String mLanguage) {
        Editor editor = _sp.edit();
        editor.putString("Language", mLanguage);
        editor.commit();
    }
    
    public static String getBTMode() {
        return _sp.getString("BTMode", "Classic");
    }

    public static void setBTMode(String strMode) {
        Editor editor = _sp.edit();
        editor.putString("BTMode", strMode);
        editor.commit();
    }

    public static boolean isAutoConnect() {
        return _sp.getBoolean("AutoConnect", false);
    }

    public static void setAutoConnect(boolean enable) {
        Editor editor = _sp.edit();
        editor.putBoolean("AutoConnect", enable);
        editor.commit();
    }
	
    public static boolean isMinimum() {
    	if(_sp != null)
    		return _sp.getBoolean("Minimum", false);
    	else return false;
    }

    public static void setMinimum(boolean enable) {
        Editor editor = _sp.edit();
        editor.putBoolean("Minimum", enable);
        editor.commit();
    }
    
    public static boolean isSuspendBacklight() {
        return _sp.getBoolean("WakeLock", false);
    }

    public static void setSuspendBacklight(boolean enable) {
        Editor editor = _sp.edit();
        editor.putBoolean("WakeLock", enable);
        editor.commit();
    }
    
	final public static String SLAVE = "slave";
	final public static String SLAVEQR = "slave_QR";
	final public static String MASTER = "master";
	
    public static String getBCMode() {
        return _sp.getString("BCMode", SLAVE);
    }

    public static void setBCMode(String strMode) {
        Editor editor = _sp.edit();
        editor.putString("BCMode", strMode);
        editor.commit();
    }
    
    public static void destroy() {
		_sp = null;
	}
}
