package com.layla.cloudder.activities;

import com.actionbarsherlock.app.SherlockListFragment;
import com.layla.cloudder.R;
import com.layla.cloudder.CloudServices.CloudServiceManager;
import com.layla.cloudder.utils.MySimpleAdapter;
import com.layla.cloudder.utils.Utils;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainFragment extends SherlockListFragment  {
	private static final int CONTEXT_MENU_DOWNLOAD_ID = 0;
//	private static final int CONTEXT_MENU_QUEUE_ID = 2;
	private LayoutInflater inflater;
	private ListView lv;

	private static final String TAG = "MainFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		this.inflater = inflater;
		
		if (Utils.isUsingWiFi()) 
			FragmentsManager.mainView = inflater.inflate(R.layout.list, null);
		else
			FragmentsManager.mainView = inflater.inflate(R.layout.no_connection_main_frag, null);

		return FragmentsManager.mainView;
	}

	@Override
	@SuppressLint("NewApi")
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);

		lv = getListView();

		FragmentsManager.mainAdapter = new MySimpleAdapter(inflater, lv.getId(), FragmentsManager.mainList);
		setListAdapter(FragmentsManager.mainAdapter);		
		registerForContextMenu(lv);		

		//listener da lista
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				String newPath = (String) ((TextView)v.findViewById(R.id.path_to_root)).getText();
				String isDir = (String) ((TextView) v.findViewById(R.id.is_dir)).getText();
				if (isDir.equals("true")) {
					reloadData(newPath);
				}
				else {
					Utils.showToast("You have to download the file to open it.");
				}
			}
		});

		if (Utils.isUsingWiFi()) {

			FragmentsManager.checkIfRoot();
			
			FragmentsManager.viewer = new Utils.ViewDirectoryFiles(this);
			FragmentsManager.viewer.execute(CloudServiceManager.currentPath);
	
			//listener do back
			//so aparece ao usar wifi
			LinearLayout goBack = (LinearLayout) FragmentsManager.mainView.findViewById(R.id.go_back);
			goBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CloudServiceManager.currentPath = Utils.getPreviousPage();
					reloadData(CloudServiceManager.currentPath);
				}
			});
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, View v,
			final ContextMenuInfo menuInfo)	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_MENU_DOWNLOAD_ID, 0, "Download File");
		//menu.add(0, CONTEXT_MENU_QUEUE_ID, 0, "Add to Queue");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		String target = (String) ((TextView) info.targetView.findViewById(R.id.path_to_root)).getText();
		String fileName = (String) ((TextView) info.targetView.findViewById(R.id.name_entry)).getText();
		String isDir = (String) ((TextView) info.targetView.findViewById(R.id.is_dir)).getText();

		//String service = (String) ((TextView) info.targetView.findViewById(R.id.service)).getText(); 

		//Since it is only possible to view, at a certain moment, the files from ONE provider
		//the current provider is the service from which the file will be downloaded
		String service = CloudServiceManager.fromServiceIdToServiceName(CloudServiceManager.getCurrentService());

		if (item.getItemId() == CONTEXT_MENU_DOWNLOAD_ID) {
			if (isDir.equals("false")) {
				CloudServiceManager.DownloadFile downloader = new CloudServiceManager.DownloadFile(true);
				downloader.execute(target, fileName, service);
			}
			else 
				Utils.showToast("Directory download not supported, yet.");
			return true;
		}
		return false;
	}

	//if is root folder, the "go back" button is hidden

	public void reloadData(String path) {
		FragmentsManager.viewer = new Utils.ViewDirectoryFiles(this);
		Utils.addToHistory(CloudServiceManager.currentPath);
		CloudServiceManager.currentPath = path;
		FragmentsManager.viewer.execute(path);
		FragmentsManager.checkIfRoot();
	}
}

