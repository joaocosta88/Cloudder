package com.layla.cloudder.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.layla.cloudder.R;
import com.layla.cloudder.CloudServices.CloudServiceManager;
import com.layla.cloudder.utils.SpinnerAdapter;
import com.layla.cloudder.utils.Utils;
import com.layla.cloudder.utils.ViewPagerAdapter;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.TitlePageIndicator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CloudderActivity extends SherlockFragmentActivity 
implements SensorEventListener {


	private static final int ACTIVITY_PREFS=0;
	private static final int ACTIVITY_UPLOAD=1;

	private SensorManager sensorManager;
	private int item = 0;

	private ViewPager mPager;
	private ViewPagerAdapter mAdapter;
	private TitlePageIndicator  mIndicator;
	private long lastUpdate;
	private Intent latestIntent;

	private static final String TAG = "CloudderActivity";
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.open();
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		lastUpdate = System.currentTimeMillis();


	}

	@Override
	public void onResume() {
		super.onResume();
		setContentView(R.layout.main);

		if(!Utils.getDatabase().isOpen())
			Utils.open();

		CloudServiceManager.initServices(getApplication());

		String[] stringArray = CloudServiceManager.getServices();
		Drawable[] logos = CloudServiceManager.getServiceDrawables();

		Context context = getSupportActionBar().getThemedContext();
		SpinnerAdapter adapter = new SpinnerAdapter(context, logos, stringArray);


		getSupportActionBar().setListNavigationCallbacks(adapter, new OnNavigationListener() {

			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				if (itemPosition != CloudServiceManager.getCurrentService()) {
					CloudServiceManager.setCurrentService(itemPosition);
					CloudServiceManager.currentPath = CloudServiceManager.getRoot();
					FragmentsManager.reloadMainFragData();
				}
				return true;
			}
		});

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);

		ProgressDialog dialog = null;


		dialog = ProgressDialog.show(this,"", 
				"Loading. Please wait...", true);
		dialog.show();
		load();
		dialog.dismiss();
	}


	@SuppressLint("NewApi")
	@Override
	public void onStop() {
		super.onStop();
		sensorManager.unregisterListener(this);
		invalidateOptionsMenu();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			getAccelerometer(event);
	}

	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;
		// Movement
		float x = values[0];
		float y = values[1];
		float z = values[2];

		float accelationSquareRoot = (x * x + y * y + z * z)
				/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		long actualTime = System.currentTimeMillis();
		if (accelationSquareRoot >= 4) //
		{
			if (actualTime - lastUpdate < 2000) {
				return;
			}
			lastUpdate = actualTime;
			if (Utils.isUsingWiFi() && Utils.queueSize > 0)
				Utils.syncAllFiles(true);
			else
				Utils.showToast("Not the best time to sync files.");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getSupportMenuInflater();
		switch (item) {
		case 0:
			inflater.inflate(R.menu.main_frag_menu, menu);
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			getSupportActionBar().setSelectedNavigationItem(CloudServiceManager.currentNavigationItem());
			break;
		case 1:
			inflater.inflate(R.menu.offline_frag_menu, menu);
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

			break;
		case 2:
			inflater.inflate(R.menu.queue_frag_menu, menu);
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

			break;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			displayPreferences();
			return true;
		case R.id.upload:
			uploadFile();
			return true;
		case R.id.refresh:
			FragmentsManager.reloadAllData();
			return true;
		case R.id.delete_all:
			Utils.deleteAllFilesFromQueue();
			FragmentsManager.reloadOfflineFragData();
			return true;
		case R.id.sync_all:
			if (Utils.isUsingWiFi() && Utils.queueSize > 0)
				Utils.syncAllFiles(true);
			else
				Utils.showToast("Not the best time to sync files.");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void uploadFile() {
		//createFileTypeDialog().show();
		Intent target = FileUtils.createGetContentIntent();
		Intent intent = Intent.createChooser(target, "Choose file to upload");
		try {
			startActivityForResult(intent, ACTIVITY_UPLOAD);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVITY_UPLOAD) {
			Log.w(TAG, "resulted"+resultCode);
			if (resultCode == Activity.RESULT_OK && CloudServiceManager.getNumberServices() > 0) {

				//handles the dialog creation
				//and decides if puts files in queue or uploads them
				Log.w(TAG, "file upload chooser returned");
				latestIntent = data;
				if (CloudServiceManager.getNumberServices() > 1)
					onCreateDialog(0).show();
				else {
					String service = CloudServiceManager.getServices()[0];
					processUpload(service);	
				}
			}
		}
	}

	private void processUpload(String service) {
		if ( !Utils.isUsingWiFi()) {
			Utils.putUploadFileInQueue(latestIntent.getData(), service);
			Intent intent = new Intent("ADDED_TO_QUEUE");
			sendBroadcast(intent);
			Utils.showToast("Added to Queue");
			FragmentsManager.reloadQueueFragData();
		} else {
			Log.w(TAG, "returned URI:"+latestIntent.getData());
			String path = Utils.getRealPathFromURI(latestIntent.getData());
			File file = new File(path);
			CloudServiceManager.UploadFile upload = new CloudServiceManager.UploadFile(file, service, true);
			upload.execute();
		}
	}



	@Override
	public Dialog onCreateDialog(int id) {
		final Dialog dialog = new Dialog(this);
		dialog.setTitle("Choose a service");
		dialog.setContentView(R.layout.service_upload_dialog);

		String[] from = {"image", "text"};
		int[] to = {R.id.image_dialog, R.id.service_entry};

		ArrayList<HashMap<String, String>> resources = FragmentsManager.createUploadDialog();

		SimpleAdapter listAdapter = new SimpleAdapter(getApplicationContext(), resources,
				R.layout.upload_dialog_row, from, to);
		ListView lst1 = (ListView) dialog.findViewById(android.R.id.list);
		lst1.setAdapter(listAdapter);
		lst1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String service = CloudServiceManager.fromServiceIdToServiceName(position);
				dialog.dismiss();
				processUpload(service);				
			}
		});

		return dialog;
	}

	@SuppressLint("NewApi")
	public void load() {

		mAdapter = new ViewPagerAdapter(getSupportFragmentManager());

		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setOffscreenPageLimit(2);
		mPager.setAdapter(mAdapter);

		mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
		mIndicator.setOnPageChangeListener(new PageChangeListener());
		mIndicator.setBackgroundColor(Color.parseColor("#FF000000"));

		mIndicator.setViewPager(mPager);

		mPager.setCurrentItem(item);

		getSupportActionBar().setDisplayShowTitleEnabled(false);


		if (!Utils.isUsingWiFi()) {
			Utils.showToast("Dude, there's not internet");
			mIndicator.setCurrentItem(1);	
		}
	}

	private void displayPreferences() {
		Intent i = new Intent(this, AppPreferences.class);
		startActivityForResult(i, ACTIVITY_PREFS);
	}

	protected class PageChangeListener extends ViewPager.SimpleOnPageChangeListener {
		@SuppressLint("NewApi")
		@Override
		public void onPageSelected(int position) {
			item = position;
			invalidateOptionsMenu();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}	

	/*
	 * DEPRECATED
	 */

	//choose between music/image/video to upload
	//also handles the creation of the new activity
	//	public Dialog createFileTypeDialog() {
	//	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	//
	//		builder.setTitle("Choose type of file to upload");
	//		
	//		builder.setItems(R.array.file_types, new OnClickListener() {
	//			
	//			@Override
	//			public void onClick(DialogInterface dialog, int which) {
	//				Intent i = null;
	//				switch (which) {
	//				//sound
	//				case 0:
	//					i = new Intent(Intent.ACTION_PICK, 
	//							android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
	//					break;
	//				//image/Video
	//				case 1:
	//					i = new Intent(Intent.ACTION_PICK, 
	//							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	//					break;
	//				}
	//				startActivityForResult(i, ACTIVITY_UPLOAD);
	//			}
	//		});
	//		
	//		return builder.create();
	//	}
}