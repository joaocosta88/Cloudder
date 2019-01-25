package com.layla.cloudder.CloudServices;

import java.io.File;
import java.util.ArrayList;

import com.layla.cloudder.R;
import com.layla.cloudder.activities.FragmentsManager;
import com.layla.cloudder.utils.DBFile;
import com.layla.cloudder.utils.Utils;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class CloudServiceManager implements Runnable {

	public static String currentPath = "";

	final static private String CURRENT_SERVICE_KEY = "current_service";

	final static private String NUMBER_SERVICES = "number_of_services";
	final static public String DROPBOX = "Dropbox";
	final static public String DRIVE = "Google Drive";
	final static public String BOX = "Box";
	//final static public String SKYDRIVE = "Microsoft Skydrive";

	public static final String BOX_AUTH_TOKEN = "BOX_AUTH_TOKEN";

	private static final String TAG = "CloudServiceManager";

	public static boolean uploadFile(File file, String service) {
		if (service.equals(DROPBOX)) {
			DropboxService.uploadFile(file);
		}
		else if (service.equals(DRIVE)) {
			DriveService.uploadFile(file);
		}
		else if (service.equals(BOX)) {
			BoxService.uploadFile(file);
		}
//		else if (service.equals(SKYDRIVE)) {
//
//		}

		return false;
	}

	public static boolean downloadFile(String path, String filename, String service) {
		if (service.equals(DROPBOX)) {
			DropboxService.downloadFile(path, filename);
		}
		else if (service.equals(DRIVE)) {
			DriveService.downloadFile(path, filename);
		}
		else if (service.equals(BOX)) {
			BoxService.downloadFile(path, filename);
		}
//		else if (service.equals(SKYDRIVE)) {
//
//		}

		return true;	
	}

	public static ArrayList<DBFile> getFileList(String path) {
		String service = fromServiceIdToServiceName(getCurrentService());

		ArrayList<DBFile> metadata = new ArrayList<DBFile>();

		if (service.equals(DROPBOX)) 
			metadata = DropboxService.getMetadata(path);

		else if (service.equals(DRIVE))
			try {
				metadata = DriveService.getMetadata();
			} catch (Exception e) {
				e.printStackTrace();
			}
		else if (service.equals(BOX)) 
			metadata = BoxService.getMetadata(path);

//		else if (service.equals(SKYDRIVE)) 
//			metadata = SkydriveService.getMetadata(path);



		return Utils.orderDirectory(metadata);		
	}

	public static String getRoot() {
		String service = fromServiceIdToServiceName(getCurrentService());

		if (service.equals(DROPBOX)) 
			return DropboxService.ROOT;
		else if (service.equals(DRIVE))
			return "";
		else if (service.equals(BOX)) 
			return BoxService.ROOT;
//		else if (service.equals(SKYDRIVE)) 
//			return SkydriveService.ROOT;

		return "";		
	}

	public static int currentNavigationItem() {
		String registeredServices[] = getServices();
		String service = fromServiceIdToServiceName(getCurrentService());
		for (int i = 0; i< registeredServices.length; i++) {
			if (registeredServices[i].equals(service)) 
				return i;
		}
		return -1;
	}

	//CAREFUL!
	//itemposition is the position in the array of registered services
	public static void setCurrentServiceBasedOnPosition(int itemPosition) {
		int currentService = fromServiceNameToServiceId(getServices()[itemPosition]);
		setCurrentService(currentService);
	}

	public static void setCurrentService(int currentService) {
		SharedPreferences prefs = Utils.sharedPrefs;
		Editor edit = prefs.edit();
		edit.putInt(CURRENT_SERVICE_KEY, currentService);
		edit.commit();
	}

	public static int getCurrentService() {
		SharedPreferences prefs = Utils.sharedPrefs;
		int service = prefs.getInt(CURRENT_SERVICE_KEY, -1);

		if (service == -1) {
			setCurrentService(0);
			service = 0;
		}
		return service;
	}

	public static int fromServiceNameToServiceId(String service) {
		String[] services = getServices();
		for (int i = 0; i < services.length; i++)  
			if (services[i].equals(service))
				return i;
		
		return -1;
	}

	public static String fromServiceIdToServiceName(int id) {
		return getServices()[id];
	}

	public static boolean hasRegisteredServices() {
		SharedPreferences prefs = Utils.sharedPrefs;

		if (prefs.getInt(NUMBER_SERVICES, 0)>0)
			return true;
		else
			return false;
	}

	public static int getNumberServices() {
		SharedPreferences prefs = Utils.sharedPrefs;

		return prefs.getInt(NUMBER_SERVICES, 0);
	}

	public static void initServices(Application application) {

		//init vars		
//		if (getServices()[0].equals(SKYDRIVE)) {
//			currentPath = SkydriveService.ROOT;
//		}
		if (getServices()[0].equals(DROPBOX))  {
			currentPath = DropboxService.ROOT;
		}
		if (getServices()[0].equals(DRIVE)) {
			currentPath = "";		
		}
		if (getServices()[0].equals(BOX)) {
			currentPath = BoxService.ROOT;
		}

		//init services
		for (String service : getServices()) {
//			if (service.equals(SKYDRIVE)) {
//				SkydriveService.startAuthentication(application);
//			}
			if (service.equals(DROPBOX))  {
				DropboxService.initDBAuth();
				DropboxService.startSession();
			}

			if (service.equals(DRIVE)) {
				Thread auth = new Thread(new CloudServiceManager());
				try {
					auth.join();
					auth.start();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
			}
			if (service.equals(BOX)) {
				BoxService.initService();
			}
		}
	}

	public static String[] getServices() {
		SharedPreferences prefs = Utils.sharedPrefs;

		int numServices = prefs.getInt(NUMBER_SERVICES, 0);
		int count = 0;

		Context c = Utils.getApplicationContext();
		String[] services = c.getResources().getStringArray(R.array.names);
		String[] array = new String[numServices];

		
		//cycle through all available services and return only those that are registered
		for (int i = 0; i < services.length; i++) {
			if (prefs.getBoolean(services[i], false)) {
				array[count] = services[i];
				count++;
			}				
		}

		return array;
	}

	public static Drawable[] getServiceDrawables() {
		SharedPreferences prefs = Utils.sharedPrefs;
		Context c = Utils.getApplicationContext();

		int numServices = prefs.getInt(NUMBER_SERVICES, 0);
		int count = 0;

		TypedArray drawables = c.getResources().obtainTypedArray(R.array.icons);
		String[] services = c.getResources().getStringArray(R.array.names);

		Drawable[] array = new Drawable[numServices];

		for (int i = 0; i < services.length; i++) {
			if (prefs.getBoolean(services[i], false)) {
				array[count] = drawables.getDrawable(i);
				count++;
			}
		}

		drawables.recycle();
		return array;
	}

	public static int[] getDrawablesId() {
		SharedPreferences prefs = Utils.sharedPrefs;
		Context c = Utils.getApplicationContext();

		int numServices = prefs.getInt(NUMBER_SERVICES, 0);
		int count = 0;

		TypedArray drawables = c.getResources().obtainTypedArray(R.array.icons);
		String[] services = c.getResources().getStringArray(R.array.names);

		int[] array = new int[numServices];

		for (int i = 0; i < services.length; i++) {
			if (prefs.getBoolean(services[i], false)) {
				array[count] = drawables.getResourceId(i,0);
				count++;
			}
		}
		drawables.recycle();
		return array;
	}

	public static void addRegisteredService(String serviceName) {
		SharedPreferences prefs = Utils.sharedPrefs;
		if (!isRegistered(serviceName)) {
			int numServices = prefs.getInt(NUMBER_SERVICES, 0);
			numServices++;
			Editor editor = prefs.edit();
			editor.putBoolean(serviceName, true);
			editor.putInt(NUMBER_SERVICES, numServices);
			editor.commit();		
		}
	}

	private static boolean isRegistered(String serviceName) {
		for (String service: getServices())
			if (service.equals(serviceName))
				return true;

		return false;
	}

	public Dialog createUploadDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Utils.getApplicationContext());
		builder.setTitle("Choose service to upload file");

		builder.setItems(getServices(), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// The 'which' argument contains the index position
				// of the selected item
			}
		});
		return builder.create();	
	}

	public static class UploadFile extends AsyncTask<String, Long, Boolean> {

		File file;
		boolean showToast;
		String service;

		public UploadFile(File file, String service, boolean showToast) {
			this.file = file;
			this.service = service;
			this.showToast = showToast;
			if (showToast)
				Utils.showToast("Uploading file...");
		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			if (file != null)
				uploadFile(file, service);

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (showToast) 
				if (result)
					Utils.showToast("File uploaded");
				else
					Utils.showToast("File not uploaded, something went wrong!");
			
			//only show this if method was not started by background service
			FragmentsManager.reloadAllData();
		}
	}

	public static class DownloadFile extends AsyncTask<String, Long, Boolean> {

		boolean showToast;

		public DownloadFile(boolean showToast) {
			this.showToast = showToast;
			if (showToast)
				Utils.showToast("Downloading file...");
		}

		/*arg0[0]: path to root
		 * arg0[1]: filename
		 * arg0[2]: service
		 */
		protected Boolean doInBackground(String... arg0) {
			return downloadFile(arg0[0], arg0[1], arg0[2]);
		}

		@Override
		protected void onPostExecute(Boolean result) {

			if (showToast) 
				if (result)
					Utils.showToast("File downloaded");
				else
					Utils.showToast("File not downloaded, something went wrong!");	

			FragmentsManager.reloadOfflineFragData();
			FragmentsManager.reloadQueueFragData();
		}
	}

	@Override
	public void run() {
		DriveService.initDrive();
		DriveService.finishAuth();		
	}
}
