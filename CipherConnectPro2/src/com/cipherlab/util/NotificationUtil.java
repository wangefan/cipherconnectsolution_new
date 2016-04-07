package com.cipherlab.util;



import com.cipherlab.cipherconnectpro2.CipherConnectManagerService.connDisconnectBDRec;
import com.cipherlab.cipherconnectpro2.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

/**
 * @author william.lu
 *
 */
public class NotificationUtil {
	public static final String TAG = "NotificationUtil";
	public final static int NOTIFY_ID = 101;

    public static void notifyWithIntent(int icon, Context context, Intent intent, String title, String message, boolean bEnableDisconn) 
    {
        //if (NOTIFY_ID == 0)
        //	NOTIFY_ID = System.identityHashCode(context);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFY_ID, GetNotificaion(icon, context, title, message, intent, context.getPackageName(), bEnableDisconn));
        mNotificationManager = null;
    }
    
    @SuppressWarnings("deprecation")
	public static Notification GetNotificaion(int icon, Context context, String title, String message, Intent intent, String strPackageName, boolean bEnableDisconn) 
    {
    	
		Notification.Builder notiBuilder= new Notification.Builder(context);
		notiBuilder.setSmallIcon(icon);
		notiBuilder.setContentTitle(title);
		notiBuilder.setContentText(message);
		notiBuilder.setWhen(System.currentTimeMillis());
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		notiBuilder.setContentIntent(pendingIntent);
		
		//Custom notification 
		RemoteViews remoteViews = new RemoteViews(strPackageName, R.layout.custom_notification);
		remoteViews.setImageViewResource(R.id.notifiation_image, icon);
		remoteViews.setTextViewText(R.id.noti_title, title);
		remoteViews.setTextViewText(R.id.noti_msg, message);
		remoteViews.setBoolean(R.id.noti_disconnect, "setEnabled", false);
		if(bEnableDisconn)
		{
			remoteViews.setBoolean(R.id.noti_disconnect, "setEnabled", true);
			Intent connDisconnIntent = new Intent(context, connDisconnectBDRec.class);
		    PendingIntent pendingDisconnIntent = PendingIntent.getBroadcast(context, 0, 
		    		connDisconnIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.noti_disconnect, pendingDisconnIntent);
		}
		
		notiBuilder.setContent(remoteViews);
		
		return notiBuilder.build(); 	
    }

    public static void cancel(Context context) 
    {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.cancel(NOTIFY_ID);
		
        mNotificationManager = null;
    }
}
