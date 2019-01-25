package com.layla.cloudder.activities;

import com.actionbarsherlock.app.SherlockActivity;
import com.box.androidlib.activities.BoxAuthentication;
import com.google.android.gms.common.AccountPicker;
import com.layla.cloudder.R;
import com.layla.cloudder.CloudServices.BoxService;
import com.layla.cloudder.CloudServices.CloudServiceManager;
import com.layla.cloudder.CloudServices.DriveService;
import com.layla.cloudder.CloudServices.DropboxService;
import com.layla.cloudder.utils.Utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CloudChooser extends SherlockActivity implements Runnable {

	private boolean clicked = false;
	private static final String TAG = "CloudChooser";
	public static final int DRIVE_LOGIN = 0;
	public static final int BOX_LOGIN = 1;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.service_chooser);

		Button db = (Button) findViewById(R.id.btn_db);
		Button gdrive = (Button) findViewById(R.id.btn_gdrive);
//	//	Button skydrive = (Button) findViewById(R.id.btn_skydrive);
		Button box = (Button) findViewById(R.id.btn_box);		

		db.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DropboxService.initAuth(CloudChooser.this);
				clicked = true;
			}
		});

		gdrive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent =
						AccountPicker.newChooseAccountIntent(null, null, 
								DriveService.ACCOUNT_TYPE, false, null, null, null, null);
				DriveService.initDrive();
				startActivityForResult(intent, DRIVE_LOGIN);

			}
		});

//		skydrive.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				SkydriveService.register(getApplication(), CloudChooser.this);
//				lastStep(CloudServiceManager.SKYDRIVE);
//				initActivity();
//			}
//		});

		box.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CloudChooser.this, BoxAuthentication.class);
				intent.putExtra("API_KEY", BoxService.API_KEY); // API_KEY is required
				startActivityForResult(intent, BOX_LOGIN);				
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (clicked){
			DropboxService.finnishAuth();
			lastStep(CloudServiceManager.DROPBOX);
			clicked = false;
			initActivity();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case DRIVE_LOGIN:
			if (data != null) {
				Bundle b = data.getExtras();
				String accountName = b.getString(AccountManager.KEY_ACCOUNT_NAME);

				if (accountName != null && accountName.length() > 0) {
					Account account = DriveService.getAccountManager().getAccountByName(accountName);
					DriveService.setAccount(account);
					Thread finish = new Thread(new CloudChooser());
					try {
						finish.join();
						finish.start();
					} catch (InterruptedException e) {
						Log.w(TAG, "Problem running thread");
						e.printStackTrace();
					}
				}
				lastStep(CloudServiceManager.DRIVE);
				initActivity();
			}
			break;
		case BOX_LOGIN: 
			if (resultCode == BoxAuthentication.AUTH_RESULT_SUCCESS) {
				// Store auth key in shared preferences.
				// BoxAuthentication activity will set the auth key into the
				// resulting intent extras, keyed as AUTH_TOKEN
				final SharedPreferences prefs = Utils.sharedPrefs;
				final SharedPreferences.Editor editor = prefs.edit();
				editor.putString(CloudServiceManager.BOX_AUTH_TOKEN, data.getStringExtra("AUTH_TOKEN"));
				editor.commit();
				
				lastStep(CloudServiceManager.BOX);
				initActivity();
			} else if (resultCode == BoxAuthentication.AUTH_RESULT_FAIL) 
				Log.w(TAG, "Unable to log into Box");

			finish();
			break;
		}
	}

	private void lastStep(String service) {
		CloudServiceManager.addRegisteredService(service);

		int id = CloudServiceManager.fromServiceNameToServiceId(service);
		CloudServiceManager.setCurrentServiceBasedOnPosition(id);
	}

	public void initActivity() {
		Intent i = new Intent(getApplicationContext(), CloudderActivity.class);
		startActivity(i);
		finish();
	}

	@Override
	public void run() {
		DriveService.finishAuth();		
	}
}
