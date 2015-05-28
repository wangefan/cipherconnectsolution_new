package com.cipherlab.util;

import java.util.List;

import android.content.Context;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.cipherlab.cipherconnectpro2.R;
import com.cipherlab.help.CipherLog;


public class KeyboardUtil {
	
	public static boolean isEnableingKeyboard(Context c, int r_ime_service_name) {
        InputMethodManager imm = (InputMethodManager)c.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> list = imm.getEnabledInputMethodList();
        if (list==null || list.size()==0)
            return false;

        String ime_service_name = c.getResources().getString(r_ime_service_name);
        String ime_name = c.getResources().getString(R.string.ime_name);
        for (InputMethodInfo imo : list) {
            String name = imo.getServiceName();

            CipherLog.d("KeyboardUtil", "InputMethodInfo.name="+name);
            if (name==null || name.length()==0)
                continue;

            if (name.equals(ime_service_name)) {
                CipherLog.d(ime_name+".KeyboardUtil", "The "+ime_service_name+" was enable.");
                return true;
            }
        }

        CipherLog.d("KeyboardUtil", "The CipherConnectKeyboard was not enable.");
        return false;
    }

    public static boolean checkKeyboard(Context c) {
        String temp = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);

        int value = temp.indexOf("CipherConnectKeyboardService");
        if (value > 0) {           
            return true;
        } else {            
            return false;
        }
    }
}
