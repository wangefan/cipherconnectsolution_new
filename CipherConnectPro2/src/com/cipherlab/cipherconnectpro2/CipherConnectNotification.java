package com.cipherlab.cipherconnectpro2;

import android.content.Context;
import android.content.Intent;

import com.cipherlab.cipherconnectpro2.R;
import com.cipherlab.util.NotificationUtil;

public class CipherConnectNotification {
	public static final String TAG = "CipherConnectNotification";
    private static int mIcon = 0;
    private static int mIcon2 = 0;
    private static String mMessage;

    public static void notify(Context context, Intent intent, String title, String message, boolean bEnableDisconn) {
        //mIcon = R.drawable.icon;
        mIcon = R.drawable.notification_connected;
        mIcon2 = R.drawable.list_connected;
        //mIcon2 = R.drawable.icon;
        mMessage = message;
        NotificationUtil.notifyWithIntent(mIcon,mIcon2, context, intent, title, mMessage, bEnableDisconn);
    }

    public static void error_notify(Context context, Intent intent, String title, String message) {
        //mIcon = R.drawable.noconnect;
        mIcon = R.drawable.notification_noconnect;
        mIcon2 = R.drawable.list_noconnect;
        //mIcon2 = R.drawable.icon;
        mMessage = message;
        NotificationUtil.notifyWithIntent(mIcon,mIcon2, context, intent, title, mMessage, false);
    }

    public static void connecting_notify(Context context, Intent intent, String title, String message) {
        //mIcon = R.drawable.connect;
        mIcon = R.drawable.notification_connect;
        mIcon2 = R.drawable.list_connect;
        //mIcon2 = R.drawable.icon;
        mMessage = message;
        NotificationUtil.notifyWithIntent(mIcon, mIcon2, context, intent, title, mMessage, false);
    }

    public static void cancel_notify(Context context) {
        NotificationUtil.cancel(context);
    }

    public static void pause_notify(Context context) {
        NotificationUtil.cancel(context);
    }

    public static Intent intent_cipherconnectproSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.cipherlab.cipherconnectpro2",
                            "com.cipherlab.cipherconnectpro2.CipherConnectSettingActivity");
        return intent;
    }
}
