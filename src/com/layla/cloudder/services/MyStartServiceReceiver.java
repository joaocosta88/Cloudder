package com.layla.cloudder.services;

import com.layla.cloudder.utils.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class MyStartServiceReceiver extends BroadcastReceiver {
	
	private static final String TAG = "MyStartServiceReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.w(TAG,"received");
		Intent service = new Intent(context, CloudderService.class);

		//batery
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
		int rawlevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		int level = -1;	
		if (rawlevel >= 0 && scale > 0)
			level = (rawlevel * 100) / scale;
		service.putExtra("status", level);

		if (Utils.queueSize > 0 && Utils.sharedPrefs.getBoolean("useSmartUpload", false)) {
			String batLevel = Utils.sharedPrefs.getString("pref_key_minimum_battery", "20");
			if (level > Integer.parseInt(batLevel) && Utils.isUsingWiFi()) {
				ComponentName name = context.startService(service);
				Log.w(TAG, "teste "+(name == null));
			}			
		}
		else {
			AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, MyStartServiceReceiver.class);
			manager.cancel(PendingIntent.getBroadcast(context, 0, i,PendingIntent.FLAG_UPDATE_CURRENT));				
		}				

	}
}
