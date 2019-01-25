package com.layla.cloudder.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.layla.cloudder.R;
import com.layla.cloudder.CloudServices.CloudServiceManager;
import com.layla.cloudder.utils.DBFile;
import com.layla.cloudder.utils.MySimpleAdapter;
import com.layla.cloudder.utils.Utils;

public class FragmentsManager {

	//Putting these files in this class eases the calling 
	//of notifyDataSetChanged
	public static ArrayList<DBFile> mainList;
	public static MySimpleAdapter mainAdapter;

	public static ArrayList<DBFile> offlineList;
	public static MySimpleAdapter offlineAdapter;

	public static ArrayList<DBFile> queueList;
	public static MySimpleAdapter queueAdapter;

	public static SherlockFragmentActivity activityHelper;

	public static View mainView = null;

	public static final String TAG = "FragmentsManager";
	public static Utils.ViewDirectoryFiles viewer;

	public static void initFragmentManager() {
		mainList = new ArrayList<DBFile>();
		offlineList = new ArrayList<DBFile>();
		queueList = new ArrayList<DBFile>();
	}

	public static void reloadAllData() {
		reloadMainFragData();
		reloadOfflineFragData();
		reloadQueueFragData();
	}

	public static void reloadMainFragData() {
		FragmentsManager.viewer = new Utils.ViewDirectoryFiles();
		Utils.resetHistory();
		checkIfRoot();
		FragmentsManager.viewer.execute(CloudServiceManager.currentPath);
	}	

	public static void reloadOfflineFragData() {
		Utils.openDir(Utils.LOCAL_PATH);
		offlineAdapter.notifyDataSetChanged();
	}

	public static void reloadQueueFragData() {
		queueList.clear();
		queueList.addAll(Utils.getAllFiles());
		queueAdapter.notifyDataSetChanged();
	}

	public static ArrayList<HashMap<String, String>> createUploadDialog() {

		String services[] = CloudServiceManager.getServices();
		int images[] = CloudServiceManager.getDrawablesId();

		ArrayList<HashMap<String, String>> resources = new ArrayList<HashMap<String, String>>();

		for (int i = 0; i < CloudServiceManager.getNumberServices(); i++) {
			HashMap<String, String> listData = new HashMap<String, String>();
			listData.put("text", services[i]);
			listData.put("image", Integer.toString(images[i]));

			resources.add(listData);
		}

		return resources;
	}

	public static void checkIfRoot() {
		if (FragmentsManager.mainView != null) 
				if (CloudServiceManager.currentPath.equals(CloudServiceManager.getRoot())) 
					FragmentsManager.mainView.findViewById(R.id.go_back).setVisibility(View.GONE);
				else
					FragmentsManager.mainView.findViewById(R.id.go_back).setVisibility(View.VISIBLE);
	}
}
