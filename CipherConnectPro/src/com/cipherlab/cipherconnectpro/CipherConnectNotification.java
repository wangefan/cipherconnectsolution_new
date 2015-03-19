package com.cipherlab.cipherconnectpro;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cipherlab.cipherconnectpro.R;
import com.cipherlab.util.NotificationUtil;

public class CipherConnectNotification {
	public static final String TAG = "CipherConnectNotification";
    private static int mIcon = 0;
    private static String mMessage;
    private static boolean isResume = false;

    public static void notify(Context context, Intent intent, String title, String message) {
        mIcon = R.drawable.icon;
        mMessage = message;
        NotificationUtil.notifyWithIntent(mIcon, context, intent, title, mMessage);
    }

    public static void error_notify(Context context, Intent intent, String title, String message) {
        mIcon = R.drawable.noconnect;
        mMessage = message;
        NotificationUtil.notifyWithIntent(mIcon, context, intent, title, mMessage);
    }

    public static void connecting_notify(Context context, Intent intent, String title, String message) {
        mIcon = R.drawable.connect;
        mMessage = message;
        NotificationUtil.notifyWithIntent(mIcon, context, intent, title, mMessage);
    }

    public static void cancel_notify(Context context) {
        NotificationUtil.cancel(context);
    }

    public static void pause_notify(Context context) {
        NotificationUtil.cancel(context);
        isResume = true;
    }
    
    public static void online_notify(Context context, Intent intent, String title, String message) {
        mIcon = R.drawable.online;
        mMessage = message;
        NotificationUtil.notifyWithIntent(mIcon, context, intent, title, mMessage);
    }
    
    public static void offline_notify(Context context, Intent intent, String title, String message) {
        mIcon = R.drawable.offline;
        mMessage = message;
        NotificationUtil.notifyWithIntent(mIcon, context, intent, title, mMessage);
    }

    public static void resume_notify(Context context) {
        Log.d(TAG, "resume_notify isResume="+isResume);
        if (isResume == true) 
        {
            isResume = false;
            NotificationUtil.notifyWithIntent(mIcon, 
            									context,
            									intent_cipherconnectproSettings(), 
            									context.getResources().getString(R.string.ime_name), 
            									mMessage);
            
            Log.d(TAG, "isResume turn off");
        }
    }

    public static Intent intent_BluetoothSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.settings", 
                            "com.android.settings.bluetooth.BluetoothSettings");
        return intent;
    }

    public static Intent intent_LanguageSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.settings", 
                            "com.android.settings.LanguageSettings");
        return intent;
    }

    public static Intent intent_cipherconnectproSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.cipherlab.cipherconnectpro",
                            "com.cipherlab.cipherconnectpro.CipherConnectSettingActivity");
        return intent;
    }
    
    public static Intent intent_cipherconnectproServerOnlive() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.cipherlab.cipherconnectpro",
                            "com.cipherlab.cipherconnectpro.SalveModeActivity");
        return intent;
    }
    
    public static Intent intent_cipherconnectproServerOfflive() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.cipherlab.cipherconnectpro",
                            "com.cipherlab.cipherconnectpro.CipherConnectSettingActivity");
        return intent;
    }
}
