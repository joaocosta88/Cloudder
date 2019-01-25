package com.layla.cloudder.activities;

import com.layla.cloudder.CloudServices.CloudServiceManager;
import com.layla.cloudder.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
	
	private String TAG = "MainActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.setContext(getApplicationContext());
		FragmentsManager.initFragmentManager();

		if (CloudServiceManager.hasRegisteredServices()) {
			CloudServiceManager.initServices(getApplication());			
			Intent i = new Intent(this, CloudderActivity.class);
			startActivity(i);
		}
		else {
			//TODO check if there's connectivity
			Intent i = new Intent(this, CloudChooser.class);
			startActivity(i);
		}
		Utils.close();
		finish();
	}
}
