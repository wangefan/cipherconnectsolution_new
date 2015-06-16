package com.cipherlab.util;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * @author william.lu
 *
 */
public class NotificationUtil {
	public static final String TAG = "NotificationUtil";
	public final static int NOTIFY_ID = R.drawable.btn_default;

    public static void notifyWithIntent(int icon, Context context, Intent intent, String title, String message) 
    {
        //if (NOTIFY_ID == 0)
        //	NOTIFY_ID = System.identityHashCode(context);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFY_ID, GetNotificaion(icon, context, title, message, intent));
        mNotificationManager = null;
    }
    
    @SuppressWarnings("deprecation")
	public static Notification GetNotificaion(int icon, Context context, String title, String message, Intent intent) 
    {
    	if(Build.VERSION.SDK_INT <=  16)
    	{
			Notification not = new Notification(icon, title, System.currentTimeMillis());
    		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
    		not.setLatestEventInfo(context, title, message, pendingIntent);
    		return not;	
    	}
		else {
			Notification.Builder notiBuilder= new Notification.Builder(context);
			notiBuilder.setSmallIcon(icon);
			notiBuilder.setContentTitle(title);
			notiBuilder.setContentText(message);
			notiBuilder.setWhen(System.currentTimeMillis());
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			notiBuilder.setContentIntent(pendingIntent);
			return notiBuilder.build(); 	
		}
    }

    public static void cancel(Context context) 
    {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.cancel(NOTIFY_ID);
		
        mNotificationManager = null;
    }
}
