package com.layla.cloudder.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.layla.cloudder.CloudServices.CloudServiceManager;
import com.layla.cloudder.activities.FragmentsManager;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

public class Utils {

	private static Context c;
	private static LinkedList<String> history;
	public static final String LOCAL_PATH = 
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Cloudder";
	public static final File DIRECTORY_DCIM = 
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
	public static SharedPreferences sharedPrefs;
	public static int queueSize;

	public static final String TAG = "Utils";


	public static void setContext(Context ctx) {
		c=ctx;
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		history = new LinkedList<String>();
		open();
		queueSize = getDBSize();
	}

	public static Context getApplicationContext() {
		return c;
	}

	public static String getPreviousPage() {
		String result = history.getLast();
		history.removeLast();
		return result;
	}

	public static void addToHistory(String dir) {
		history.add(dir);
	}

	public static void resetHistory() {
		history.clear();
	}

	public static boolean isUsingWiFi() {
		ConnectivityManager cm =
				(ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);


		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null)
			return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
		else return false;
	}

	public static boolean isUsingDataConnection() {
		ConnectivityManager cm =
				(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

		return activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
	}

	public static int FreeMemory() {
		StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
		int Free  = (statFs.getAvailableBlocks() * statFs.getBlockSize()) / 1048576;
		return Free;
	}

	public static ArrayList<DBFile> orderDirectory(List<DBFile> listParam) {
		List<DBFile> toRemove = new ArrayList<DBFile>();
		ArrayList<DBFile> result = new ArrayList<DBFile>();

		if (listParam != null) {
			for (DBFile file: listParam) {
				if (file.isDir()) {
					result.add(file);
					toRemove.add(file);
				}
			}
			listParam.removeAll(toRemove);
			result.addAll(listParam);
		}
		return result;
	}

	public static void handleNewPhoto(String path) {
		File file = new File(path);
		if (CloudServiceManager.getNumberServices() > 0)
			putFileInQueue(file, CloudServiceManager.getServices()[0]);
	}

	public static String getRealPathFromURI(Uri uri) {
		return FileUtils.getPath(c, uri);
	}

	public static void openFile(File f) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String ext=f.getName().substring(f.getName().indexOf(".")+1);
		String type = mime.getMimeTypeFromExtension(ext.toLowerCase(Locale.getDefault()));
		intent.setDataAndType(Uri.fromFile(f),type);

		try {
			c.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			showToast("No application can open this file.");
			e.printStackTrace();
		}
	}

	public static void deleteLocalFile(String path) {
		File file = new File(path);

		if (file.exists()) {
			file.delete();
			showToast("File deleted");
		}		
	}


	//check if there is a valid icon associated
	//if there's associated icon, find a convenient icon to be used
	public static String getValidExtention(String extension) {
		if (extension.equals("pptx"))
			return "ppt";
		if (extension.equals("docx"))
			return "doc";
		else
			return extension;
	}

	public static void showToast(String msg) {
		Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();		
	}

	public static void openDir(String path) {
		FragmentsManager.offlineList.clear();
		File f = new File(path);
		for (File file : f.listFiles()) 
			FragmentsManager.offlineList.add(new DBFile(file));

		FragmentsManager.offlineAdapter.notifyDataSetChanged();
	}

	public static String getMimetype(File file)  {
		String mimetype = "";
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			mimetype =  URLConnection.guessContentTypeFromStream(is);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mimetype;
	}

	/**
	 * DATABASE RELATED METHODS
	 */

	//origin = web U local
	private static SQLiteDatabase database;
	public static FileDatabase dbHelper;
	private static String[] allColumns = { FileDatabase.COLUMN_ID,
		FileDatabase.COLUMN_NAME, FileDatabase.COLUMN_DESTINY, 
		FileDatabase.COLUMN_PATH};

	public static SQLiteDatabase getDatabase() {
		return database;
	}

	public static void open() throws SQLException {
		if (dbHelper == null)
			dbHelper = new FileDatabase(c);
		database = dbHelper.getWritableDatabase();
	}

	public static void close() {
		dbHelper.close();
	}

	protected static void putFileInQueue(File file, String destiny) {
		queueSize++;
		ContentValues values = new ContentValues();
		values.put(FileDatabase.COLUMN_NAME, file.getName());
		values.put(FileDatabase.COLUMN_PATH, file.getAbsolutePath());
		values.put(FileDatabase.COLUMN_DESTINY, destiny);

		database.insert(FileDatabase.TABLE_FILES, null, values);
	}

	public static void deleteFileFromQueue(String target) {
		database.delete(FileDatabase.TABLE_FILES, FileDatabase.COLUMN_PATH +" = '"+target+"'", null);
		queueSize--;
	}

	public static void syncOneFile(String target) {

		if (!isUsingWiFi()) {
			showToast("I can't sync with no Internet...");
			return;
		}

		Cursor cursor = database.query(FileDatabase.TABLE_FILES, allColumns, FileDatabase.COLUMN_PATH+" = '"+target+"'", 
				null, null, null, null);
		cursor.moveToFirst();
		DBFile file;

		while (!cursor.isAfterLast()) {
			queueSize--;

			file = cursorToDBFile(cursor);
			String service = file.getDestiny();
			File f = new File(file.getPathToRoot());
			CloudServiceManager.UploadFile up = new CloudServiceManager.UploadFile(f, service, true);
			up.execute();	

			int index = cursor.getInt(cursor.getColumnIndex(FileDatabase.COLUMN_ID));
			database.delete(FileDatabase.TABLE_FILES, FileDatabase.COLUMN_ID + " = " +index, null);
			cursor.moveToNext();
		}
		showToast("File synced");
	}

	public static void syncAllFiles(boolean showToast) {
		if (!isUsingWiFi()) {
			showToast("I can't sync with no Internet...");
			return;
		}

		Cursor cursor = database.query(FileDatabase.TABLE_FILES, allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		DBFile file;
		if (showToast)
			showToast("Syncing files.");
		while (!cursor.isAfterLast()) {
			//			if (cursor.getString(cursor.getColumnIndex(FileDatabase.COLUMN_DESTINY)).equals("local")) {
			file = cursorToDBFile(cursor);
			String service = file.getDestiny();
			File f = new File(file.getPathToRoot());
			CloudServiceManager.UploadFile up = new CloudServiceManager.UploadFile(f, service, false);
			up.execute();	
			//			}

			int index = cursor.getInt(cursor.getColumnIndex(FileDatabase.COLUMN_ID));
			database.delete(FileDatabase.TABLE_FILES, FileDatabase.COLUMN_ID + " = " +index, null);
			cursor.moveToNext();
		}
		queueSize = 0;
		if (showToast)
			showToast("All files synced");
	}

	private static DBFile cursorToDBFile(Cursor cursor) {

		String path = cursor.getString(cursor.getColumnIndex(FileDatabase.COLUMN_PATH));
		String name = cursor.getString(cursor.getColumnIndex(FileDatabase.COLUMN_NAME));
		String destiny = cursor.getString(cursor.getColumnIndex(FileDatabase.COLUMN_DESTINY));

		DBFile file = new DBFile(path, name, destiny);
		return file;
	}

	public static void deleteAllFilesFromQueue() {
		database.execSQL("DELETE FROM "+FileDatabase.TABLE_FILES);
		queueSize = 0;
	}

	public static ArrayList<DBFile> getAllFiles() {
		ArrayList<DBFile> queue = new ArrayList<DBFile>();
		Cursor cursor = database.query(FileDatabase.TABLE_FILES, allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			queue.add(cursorToDBFile(cursor));
			cursor.moveToNext();
		}
		return queue;
	}

	public static String getExtension(String path) {
		String extension;
		if (path.split("\\.(?=[^\\.]+$)").length > 1) 
			extension = path.split("\\.(?=[^\\.]+$)")[1].toLowerCase(Locale.getDefault()).trim();
		else 
			extension = "_blank";
		return extension;
	}

	public static int getDBSize() {
		Cursor cursor = database.query(FileDatabase.TABLE_FILES, allColumns, null, null, null, null, null);
		int count = cursor.getCount();
		return count;
	}

	public static void putUploadFileInQueue(Uri uri, String service) {
		File file = new File(Utils.getRealPathFromURI(uri));
		Utils.putFileInQueue(file, service);
	}

	//View files from a service directory
	public static class ViewDirectoryFiles extends AsyncTask<String, Long, ArrayList<DBFile>> {
		ProgressDialog dialog;

		public ViewDirectoryFiles(SherlockListFragment activity) {	
			FragmentsManager.activityHelper = activity.getSherlockActivity();
			dialog = ProgressDialog.show(activity.getSherlockActivity(),"", 
					"Loading. Please wait...", true);
			dialog.show();
		}

		public ViewDirectoryFiles(SherlockFragmentActivity activity) {	
			FragmentsManager.activityHelper = activity;
			dialog = ProgressDialog.show(activity,"", 
					"Loading. Please wait...", true);
			dialog.show();
		}

		public ViewDirectoryFiles() {
			if (FragmentsManager.activityHelper != null) {
				dialog = ProgressDialog.show(FragmentsManager.activityHelper,"", 
						"Loading. Please wait...", true);
				dialog.show();
			}
		}

		@Override
		protected ArrayList<DBFile> doInBackground(String... arg0) {
			ArrayList<DBFile> metadata = CloudServiceManager.getFileList(arg0[0]);
			if (metadata != null)
				return metadata;
			else
				return null;
		}

		@Override
		protected void onPostExecute(ArrayList<DBFile> result) {
			if (dialog != null)
				dialog.dismiss();
			if (result != null) {
				FragmentsManager.mainList.clear();
				FragmentsManager.mainList.addAll(result);
				FragmentsManager.mainAdapter.notifyDataSetChanged();
			}
		}
	}
}