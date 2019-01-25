package com.layla.cloudder.activities;

import com.layla.cloudder.R;
import com.layla.cloudder.CloudServices.CloudServiceManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class AppPreferences extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		Preference serviceManager = (Preference) findPreference("register_service");
		serviceManager.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				createWarningDialog().show();
				return true;
			}
		});

//		ListPreference defaultService = (ListPreference) findPreference("pref_key_default_service_co");
//		String[] services = CloudServiceManager.getServices();
//		if (services != null) {
//			defaultService.setEntries(services);
//			defaultService.setEntryValues(services);
//
//			if (defaultService.getValue() == null)
//				defaultService.setValueIndex(0);
//		}
	}

	private Dialog createWarningDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Warning");
		builder.setMessage("At the moment it is only possible to register one account per service. " +
				"If you register a second account in a service, the first one will be removed");
		builder.setPositiveButton("Register anyway", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(getApplicationContext(), CloudChooser.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		return builder.create();
	}
}
