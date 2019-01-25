package com.layla.cloudder.CloudServices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.box.androidlib.Box;
import com.box.androidlib.BoxSynchronous;
import com.box.androidlib.DAO.BoxFile;
import com.box.androidlib.DAO.BoxFolder;
import com.box.androidlib.ResponseListeners.FileDownloadListener;
import com.box.androidlib.ResponseListeners.FileUploadListener;
import com.box.androidlib.ResponseParsers.AccountTreeResponseParser;
import com.layla.cloudder.utils.DBFile;
import com.layla.cloudder.utils.Utils;

public class BoxService {

	public static final String API_KEY = "8iur0oihebvwpcke0t40yxqv7rve9uqn";

	private static final String TAG = "BoxService";

	public final static String ROOT = "0";
	public final static long ROOT_LONG = 0l;

	private static String authToken = "";

	public static void initService() {
		authToken = Utils.sharedPrefs.getString(CloudServiceManager.BOX_AUTH_TOKEN, null);
	}


	public static ArrayList<DBFile> getMetadata(String path) {

		long folderId = 0;
		if (!path.equals(ROOT))
			folderId = Long.valueOf(path);

		AccountTreeResponseParser tree = null;
		ArrayList<DBFile> result = new ArrayList<DBFile>();
		BoxSynchronous box = BoxSynchronous.getInstance(API_KEY);


		try {
			tree = box.getAccountTree(authToken, folderId, new String[] {Box.PARAM_ONELEVEL, Box.PARAM_SIMPLE, Box.PARAM_SHOW_PATH_IDS, Box.PARAM_SHOW_PATH_NAMES});
		} catch (IOException e) {
			e.printStackTrace();
		}

		BoxFolder boxFolder = tree.getFolder(); 
		DBFile file = new DBFile();

		Iterator<? extends BoxFolder> foldersIterator = boxFolder.getFoldersInFolder().iterator();
		while (foldersIterator.hasNext()) {
			BoxFolder subfolder = foldersIterator.next();
			file = new DBFile();
			file.setDir(true);
			file.setName(subfolder.getFolderName());
			file.setPathToRoot(String.valueOf(subfolder.getId()));
			result.add(file);
		}

		Iterator<? extends BoxFile> filesIterator = boxFolder.getFilesInFolder().iterator();
		while (filesIterator.hasNext()) {
			file = new DBFile();
			file.setDir(false);
			BoxFile boxFile = filesIterator.next();
			file.setName(boxFile.getFileName());
			file.setPathToRoot(String.valueOf(boxFile.getId()));						
			result.add(file);
		}
		return result;
	}


	public static void uploadFile(File file) {

		BoxSynchronous box = BoxSynchronous.getInstance(API_KEY);

		if (!file.isFile() || !file.canRead())
			Log.w(TAG, "oooops");

		try {
			Looper.getMainLooper().prepareMainLooper();
			box.upload(authToken, Box.UPLOAD_ACTION_UPLOAD, file, file.getName(), ROOT_LONG, new FileUploadListener() {

				@Override
				public void onIOException(IOException e) {
					e.printStackTrace();					
				}

				@Override
				public void onProgress(long bytesTransferredCumulative) {
					Log.w(TAG, "progress"+bytesTransferredCumulative);
				}

				@Override
				public void onComplete(BoxFile boxFile, String status) {
					Log.w(TAG, "COmpleted with status "+status);
				}

				@Override
				public void onFileNotFoundException(FileNotFoundException e) {
					e.printStackTrace();					
				}

				@Override
				public void onMalformedURLException(MalformedURLException e) {
					e.printStackTrace();					
				}

			}, new Handler(){

				@Override
				public void handleMessage(Message msg) {
					Log.w(TAG, "received msg "+msg);
				}

			});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void downloadFile(String path, String filename) {
		BoxSynchronous box = BoxSynchronous.getInstance(API_KEY);

		File f = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			FileOutputStream mFos = null;
			try {
				
				Looper.prepare();

				f = new File (Utils.LOCAL_PATH);
				if (!f.isDirectory())
					f.mkdir();

				mFos = new FileOutputStream(f.getAbsolutePath()+"/"+filename);

				box.download(authToken, Long.parseLong(path), mFos, null, new FileDownloadListener() {

					@Override
					public void onIOException(IOException e) {
						e.printStackTrace();
					}

					@Override
					public void onProgress(long bytesDownloaded) {
					}

					@Override
					public void onComplete(String status) {
						Log.w(TAG, "completed with status "+status);

					}
				}, new Handler(){

					@Override
					public void handleMessage(Message msg) {
						Log.w(TAG, "received msg "+msg);
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (mFos != null)
					try {
						mFos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}


	}
}
