package com.cipherlab.cipherconnectpro;

import com.cipherlab.cipherconnectpro.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CipherConnectSettingInfo {
	public static final String TAG = "CipherConnectSettingInfo";
	public static final boolean _DEBUG = false;
    

    public static final String _NAME = "CipherConnect";
    private static SharedPreferences _sp = null;

    public static void initSharedPreferences(Context c) {
        if (_sp==null) {
            _sp = c.getSharedPreferences(_NAME, 0);
        }
    }

    public static String getLastDeviceName(Context c) {
        initSharedPreferences(c);
        return _sp.getString("LastDeviceName", "No select");
    }
    
    public static String getBarcodeInterval(Context c) {
        initSharedPreferences(c);
        return _sp.getString("BarcodeInterval", "No select");
    }

    public static void setBarcodeInterval(Context c,String mBarcodeInterval) {
        initSharedPreferences(c);
        Editor editor = _sp.edit();
        editor.putString("BarcodeInterval", mBarcodeInterval);
        editor.commit();
    }

    public static String getLanguage(Context c) {
        initSharedPreferences(c);
        return _sp.getString("Language", "No select");
    }

    public static void setLanguage(Context c,String mLanguage) {
        initSharedPreferences(c);
        Editor editor = _sp.edit();
        editor.putString("Language", mLanguage);
        editor.commit();
    }
    
    public static String getBTMode(Context c) {
        initSharedPreferences(c);
        return _sp.getString("BTMode", c.getResources().getString(R.string.Str_BT_Classic));
    }

    public static void setBTMode(Context c,String strMode) {
        initSharedPreferences(c);
        Editor editor = _sp.edit();
        editor.putString("BTMode", strMode);
        editor.commit();
    }

    public static boolean isAutoConnect(Context c) {
        
		initSharedPreferences(c);
        return _sp.getBoolean("AutoConnect", false);
    }

    public static void setAutoConnect(Context c, boolean enable) {
        initSharedPreferences(c);
        Editor editor = _sp.edit();
        editor.putBoolean("AutoConnect", enable);
        editor.commit();
    }
	
    public static boolean isMinimum(Context c) {
        initSharedPreferences(c);
        return _sp.getBoolean("Minimum", false);
    }

    public static void setMinimum(Context c, boolean enable) {
        initSharedPreferences(c);
        Editor editor = _sp.edit();
        editor.putBoolean("Minimum", enable);
        editor.commit();
    }
    
    public static boolean isSuspendBacklight(Context c) {
        
		initSharedPreferences(c);
        return _sp.getBoolean("WakeLock", false);
    }

    public static void setSuspendBacklight(Context c, boolean enable) {
        initSharedPreferences(c);
        Editor editor = _sp.edit();
        editor.putBoolean("WakeLock", enable);
        editor.commit();
    }
    
    public static void destroy() {
		_sp = null;
	}
}
