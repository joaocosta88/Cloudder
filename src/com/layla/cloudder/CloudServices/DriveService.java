package com.layla.cloudder.CloudServices;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.layla.cloudder.utils.DBFile;
import com.layla.cloudder.utils.Utils;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class DriveService {

	private static Account mAccount;
	private static GoogleAccountManager mAccountManager;
	private static Drive service;
	private static final String TAG = "DriveService";
	public static final String[] ACCOUNT_TYPE = new String[] {GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
	private static Context context;


	//init service vars
	public static void initDrive() {
		context = Utils.getApplicationContext();

		mAccountManager = new GoogleAccountManager(context);
		mAccount =  mAccountManager.getAccountByName(Utils.sharedPrefs.getString("selected_account_preference", ""));
	}

	public static void finishAuth() {
		if (service == null) {
			try {
				GoogleAccountCredential credential =
						GoogleAccountCredential.usingOAuth2(context, DriveScopes.DRIVE);
				credential.setSelectedAccountName(mAccount.name);
				credential.getToken();
				service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
						new GsonFactory(), credential).build();
			} catch (Exception e) {
				Log.e(TAG, "Failed to get token");
				// If the Exception is User Recoverable, we display a notification that will trigger the
				// intent to fix the issue.
				if (e instanceof UserRecoverableAuthException) {
					UserRecoverableAuthException exception = (UserRecoverableAuthException) e;
					NotificationManager notificationManager = (NotificationManager) 
							context.getSystemService(Context.NOTIFICATION_SERVICE);
					Intent authorizationIntent = exception.getIntent();
					authorizationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(
							Intent.FLAG_FROM_BACKGROUND);
					PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
							authorizationIntent, 0);
					Notification notification = new NotificationCompat.Builder(context)
					.setSmallIcon(android.R.drawable.ic_dialog_alert)
					.setTicker("Permission requested")
					.setContentTitle("Permission requested")
					.setContentText("for account " + mAccount.name)
					.setContentIntent(pendingIntent).setAutoCancel(true).build();
					notificationManager.notify(0, notification);
				} else {
					e.printStackTrace();
				}
			}
		}
	}

	public static Account getPreferenceAccount() {
		return mAccountManager.getAccountByName(Utils.sharedPrefs.getString("selected_account_preference",
				""));
	}

	/**
	 * Set the new account to use with the app.
	 * 
	 * @param account New account to use.
	 */
	public static void setAccount(Account account) {
		mAccount = account;
		if (account != null) {       
			SharedPreferences.Editor editor =
					Utils.sharedPrefs.edit();
			editor.putString("selected_account_preference", account.name);
			editor.commit();
		}
	}

	public static GoogleAccountManager getAccountManager() {
		return mAccountManager;
	}

	public static Account getDriveAccount() {
		return mAccount;
	}

	public static void uploadFile(java.io.File file) {
		File body = new File();

		String mimetype = Utils.getMimetype(file);
		String title = file.getName();

		body.setMimeType(mimetype);
		body.setTitle(title);

		FileContent fileContent = new FileContent(mimetype, file);

		try {
			service.files().insert(body, fileContent).execute();;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean downloadFile(String path, String filename) {
		java.io.File f = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			FileOutputStream out = null;
			InputStream in = null;
			try {
				f = new java.io.File (Utils.LOCAL_PATH);
				if (!f.isDirectory())
					f.mkdir();

				out = new FileOutputStream(f.getAbsolutePath()+"/"+filename);
				//DropboxFileInfo e = DropboxService.getSession().getFile(filePath, null, mFos, null);
				HttpResponse resp =
						service.getRequestFactory().buildGetRequest(new GenericUrl(path))
						.execute();
				in = resp.getContent();

				int read = 0;
				byte[] bytes = new byte[1024];

				while ((read = in.read(bytes)) != -1) 
					out.write(bytes, 0, read);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.flush();
						out.close();
					}
					if (in != null)
						in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return true;
		}
		else 
			return false;

	}

	public static ArrayList<DBFile> getMetadata() throws IOException {
		Log.w(TAG, "getting metadata");
		ArrayList<DBFile> result = new ArrayList<DBFile>();

		Files.List request = service.files().list();

		do {
			try {

				FileList files = request.execute();
				DBFile auxFile = new DBFile();
				for (File f: files.getItems()) {
					if (f != null) {
						if (f.getOriginalFilename() != null && !f.getOriginalFilename().equals("Untitled")) {
							auxFile.setName(f.getTitle());
							auxFile.setDate(f.getModifiedDate().toString());
							auxFile.setPathToRoot(f.getDownloadUrl());
							auxFile.setDir(false);
							result.add(auxFile);
						}
					}
				}

				request.setPageToken(files.getNextPageToken());
			} catch (Exception e) {
				e.printStackTrace();
				Log.w(TAG, "An error: "+e);
			}
		} while (request.getPageToken() != null &&
				request.getPageToken().length() > 0);

		return result;
	}
}