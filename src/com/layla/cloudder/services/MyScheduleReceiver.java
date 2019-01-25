package com.layla.cloudder.services;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyScheduleReceiver extends BroadcastReceiver {
	
	//interval in ms in which the service is restarted
	private static final long REPEAT_TIME = 1000 * 1;
	
	private static final String TAG = "MyScheduleReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		//register file observer
//		RecursiveFileObserver  mObserver = 
//				new RecursiveFileObserver(Utils.DIRECTORY_DCIM.getAbsolutePath(), RecursiveFileObserver.CREATE);
//		mObserver.startWatching();
		
		Log.w(TAG, "received");
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, MyStartServiceReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,PendingIntent.FLAG_UPDATE_CURRENT);
		Calendar cal = Calendar.getInstance();
		
		// Start 3 minutes after boot completed
		cal.add(Calendar.SECOND, 4);
		
		manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
	}
}
