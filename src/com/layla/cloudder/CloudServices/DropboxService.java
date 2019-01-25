package com.layla.cloudder.CloudServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DeltaEntry;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.dropbox.client2.session.Session.AccessType;
import com.layla.cloudder.utils.DBFile;
import com.layla.cloudder.utils.Utils;

public class DropboxService {

	final static private String APP_KEY = "6jlt0wevvw3do8z";
	final static private String APP_SECRET = "o765cbq8sm5kr8x";

	public final static String ROOT = "/";


	final static private String ACCESS_KEY_NAME = "DROPBOX_ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "DROPBOX_ACCESS_SECRET";

	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

	final static public String TAG ="DropboxService";

	private static DropboxAPI<AndroidAuthSession> mApi;
	private static SharedPreferences prefs;


	public static void initDBAuth() {
		prefs = Utils.sharedPrefs;
	}

	public static void initAuth(Context c) {
		prefs = Utils.sharedPrefs;
		buildNewSession();
		mApi.getSession().startAuthentication(c);
	}

	public static AndroidAuthSession startSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();

		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
			mApi = new DropboxAPI<AndroidAuthSession>(session);

			return session;
		}
		else
			return null;
	}

	public static AndroidAuthSession buildNewSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		mApi = new DropboxAPI<AndroidAuthSession>(session);

		return session;
	}

	public static void finnishAuth() {
		AndroidAuthSession session = mApi.getSession();

		if (session.authenticationSuccessful()) {
			try {
				session.finishAuthentication();

				// Store it locally in our app for later use
				TokenPair tokens = session.getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isLogged() {
		if (mApi != null) 
			if (mApi.getSession() != null)
				return mApi.getSession().isLinked();

		return false;

	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local
	 * store, rather than storing user name & password, and re-authenticating each
	 * time (which is not to be done, ever).
	 *
	 * @return Array of [access_key, access_secret], or null if none stored
	 */

	private static String[] getKeys() {
		String key = prefs.getString(ACCESS_KEY_NAME, "reee");
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local
	 * store, rather than storing user name & password, and re-authenticating each
	 * time (which is not to be done, ever).
	 * @return 
	 */
	private static void storeKeys(String key, String secret) {
		Log.w(TAG, "Keys stored");
		// Save the access key for later
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, key);
		edit.putString(ACCESS_SECRET_NAME, secret);

		Log.w(TAG, "Testing commit"+edit.commit());
	}

	@SuppressWarnings("unused")
	private static void clearKeys(Context c) {
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}


	public static ArrayList<String> delta() {
		ArrayList<String> result = null;
		try {
			List<DeltaEntry<Entry>> delta = mApi.delta(null).entries;
			result = new ArrayList<String>();

			for (DeltaEntry<Entry> entry : delta) 
				if (entry.metadata != null)
					result.add(entry.lcPath);


		} catch (DropboxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static ArrayList<DBFile> getMetadata(String dir) {
		ArrayList<DBFile> result = null;
		try {
			List<Entry> delta = mApi.metadata(dir, 10000, null, true, null).contents;
			result = new ArrayList<DBFile>();
			for (Entry entry: delta) {
				Date date = RESTUtility.parseDate(entry.modified);
				@SuppressWarnings("deprecation")
				String aux = "Last modified on "
						+date.getDay()+"/"+
						date.getMonth()+"/"+
						date.getYear()+ " at "+
						date.getHours()+":"+
						date.getMinutes();

				result.add(new DBFile(entry.fileName(), aux, entry.path, entry.isDir));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} 

		return result;
	}

	//Download a file from dropbox
	public static boolean downloadFile(String filePath, String fileName) {
		File f = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			FileOutputStream mFos = null;
			try {
				f = new File (Utils.LOCAL_PATH);
				if (!f.isDirectory())
					f.mkdir();

				mFos = new FileOutputStream(f.getAbsolutePath()+"/"+fileName);
				DropboxService.getSession().getFile(filePath, null, mFos, null);
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
			return true;
		}
		else 
			return false;
	}

	//upload a file to dropbox
	public static boolean uploadFile(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			String path = "/"+file.getName();
			DropboxService.getSession().putFileOverwrite(path, fis, file.length(), null);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean uploadFile(Uri uri) {
		File file = new File(Utils.getRealPathFromURI(uri));		
		return uploadFile(file);
	}

	public static DropboxAPI<AndroidAuthSession> getSession() {
		return mApi;
	}
}
