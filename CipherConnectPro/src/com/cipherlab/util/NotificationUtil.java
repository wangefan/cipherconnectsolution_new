package com.cipherlab.util;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * @author william.lu
 *
 */
public class NotificationUtil {
	public static final String TAG = "NotificationUtil";
	private static int NOTIFY_ID = R.drawable.btn_default;

    public static void notifyWithIntent(int icon, Context context, Intent intent, String title, String message) 
    {
        //if (NOTIFY_ID == 0)
        //	NOTIFY_ID = System.identityHashCode(context);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Notification msg = new Notification(icon, title, 0);
        PendingIntent PI = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        msg.setLatestEventInfo(context, title, message, PI);
        mNotificationManager.notify(NOTIFY_ID, msg);

        PI = null;
        msg = null;
        mNotificationManager = null;
    }

    public static void cancel(Context context) 
    {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.cancel(NOTIFY_ID);
		
        mNotificationManager = null;
    }
}
