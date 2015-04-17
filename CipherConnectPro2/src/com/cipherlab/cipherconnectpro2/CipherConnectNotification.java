package com.cipherlab.cipherconnectpro2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cipherlab.cipherconnectpro2.R;
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
        mIcon = R.drawable.idle;
        mMessage = message;
        NotificationUtil.notifyWithIntent(mIcon, context, intent, title, mMessage);
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
        intent.setClassName("com.cipherlab.cipherconnectpro2",
                            "com.cipherlab.cipherconnectpro2.CipherConnectSettingActivity");
        return intent;
    }
    
    public static Intent intent_cipherconnectproServerOnlive() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.cipherlab.cipherconnectpro2",
                            "com.cipherlab.cipherconnectpro2.SalveModeActivity");
        return intent;
    }
    
    public static Intent intent_cipherconnectproServerOfflive() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.cipherlab.cipherconnectpro2",
                            "com.cipherlab.cipherconnectpro2.CipherConnectSettingActivity");
        return intent;
    }
}
