package com.layla.cloudder.services;

import com.layla.cloudder.R;
import com.layla.cloudder.activities.CloudderActivity;
import com.layla.cloudder.utils.Utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class CloudderService extends Service {
	private final IBinder mBinder = new MyBinder();
	private static final int HELLO_ID = 1;

	private final String TAG = "CloudderService";
	
	String ns = Context.NOTIFICATION_SERVICE;
	NotificationManager mNotificationManager;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.w(TAG, "service is running");

		int notifications = Utils.queueSize;

		Utils.setContext(getApplicationContext());
		Utils.open();
		Utils.syncAllFiles(false);

		if (notifications>0) {
			if (Utils.sharedPrefs.getBoolean("useNotifications", true)) {
				mNotificationManager = (NotificationManager) getSystemService(ns);
				sendNotification(notifications);
			}
		}
		AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(getApplicationContext(), MyStartServiceReceiver.class);
		manager.cancel(PendingIntent.getBroadcast(getApplicationContext(), 0, i,PendingIntent.FLAG_UPDATE_CURRENT));
		
		Utils.close();
		
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	public class MyBinder extends Binder {
		public CloudderService getService() {
			return CloudderService.this;
		}
	}

	@SuppressWarnings("deprecation")
	public void sendNotification(int number) {
		int icon = R.drawable.logo6;
		CharSequence notificationText = number+" files were synced.";
		long when = System.currentTimeMillis();

		Context context = getApplicationContext();
		CharSequence contentTitle="Cloudder Notification";
		CharSequence contentText ="";
		if (number>1)
			contentText=number+" files were synced.";
		
		else 
			contentText=number+" file was synced.";
		
		Intent notificationIntent = new Intent(this, CloudderActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification notification = new Notification(icon, notificationText, when);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(HELLO_ID, notification);
	}
}
