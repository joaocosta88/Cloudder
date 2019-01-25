package com.layla.cloudder.CloudServices;
//package com.laila.cloudder.CloudServices;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import android.app.Activity;
//import android.app.Application;
//import android.util.Log;
//
//import com.laila.cloudder.activities.CloudChooser;
//import com.laila.cloudder.utils.DBFile;
//import com.laila.cloudder.utils.SkydriveAuxClass;
//import com.laila.cloudder.utils.Utils;
//import com.microsoft.live.LiveAuthClient;
//import com.microsoft.live.LiveAuthException;
//import com.microsoft.live.LiveAuthListener;
//import com.microsoft.live.LiveConnectClient;
//import com.microsoft.live.LiveConnectSession;
//import com.microsoft.live.LiveOperation;
//import com.microsoft.live.LiveOperationException;
//import com.microsoft.live.LiveOperationListener;
//import com.microsoft.live.LiveStatus;
//
//public class SkydriveService {
//	private final static String[] SCOPES = {"wl.signin",
//		"wl.basic",
//		"wl.offline_access",
//		"wl.skydrive_update",
//	"wl.contacts_create"};
//	private static final String CLIENT_ID = "00000000440E8AC1";
//	public static final String ROOT = "me/skydrive";
//
//	private static final String TAG = "SkydriveService";
//
//	private static SkydriveAuxClass mApp;
//
//
//	//has to be executed either user had already logged or not
//	public static void initAuth(Application application) {
//		mApp = (SkydriveAuxClass) application;
//		LiveAuthClient mAuthClient = new LiveAuthClient(mApp, CLIENT_ID);
//		mApp.setAuthClient(mAuthClient);
//	}
//
//	public static void register(Application application, Activity activity) {
//		if (mApp == null) 
//			initAuth(application);
//		
//		if (mApp.getAuthClient() == null) 
//			initAuth(application);
//		
//
//		mApp.getAuthClient().login(activity,
//				Arrays.asList(SCOPES),
//				new LiveAuthListener() {
//			@Override
//			public void onAuthComplete(LiveStatus status,
//					LiveConnectSession session,
//					Object userState) {
//				if (status != LiveStatus.CONNECTED) 
//					Log.w(TAG,"Initialize did not connect. Please try login in."+status);
//				else {
//					Log.w(TAG, "live connected");
//					authAux(session);
//				}
//
//			}
//
//			@Override
//			public void onAuthError(LiveAuthException exception, Object userState) {
//				exception.printStackTrace();
//			}
//		});
//	}
//
//	//called if user had already logged
//	public static void startAuthentication(Application application) {
//		if (mApp == null)
//			initAuth(application);
//		mApp.getAuthClient().initialize(Arrays.asList(SCOPES), new LiveAuthListener() {
//
//			@Override
//			public void onAuthError(LiveAuthException exception, Object userState) {
//				exception.printStackTrace();
//			}
//
//			@Override
//			public void onAuthComplete(LiveStatus status,
//					LiveConnectSession session,
//					Object userState) {
//
//				if (status != LiveStatus.CONNECTED) {
//					Log.w(TAG,"Initialize did not connect. Please try login in."+status);
//				}
//				else
//					authAux(session);
//
//			}
//		});
//	}
//
//	private static void authAux(LiveConnectSession session) {
//		mApp.setSession(session);
//		mApp.setConnectClient(new LiveConnectClient(session));
//	}
//
//	public static ArrayList<DBFile> getMetadata(String path) {
//		Log.w(TAG, (mApp.getConnectClient() == null)+"");
//		mApp.getConnectClient().getAsync(path + "/files", new LiveOperationListener() {
//			@Override
//			public void onComplete(LiveOperation operation) {
//
//				JSONObject result = operation.getResult();
//
//				JSONArray data = result.optJSONArray(com.laila.cloudder.utils.JsonKeys.DATA);
//				Utils.showToast("here");
//				//				for (int i = 0; i < data.length(); i++) {
//				//					SkyDriveObject skyDriveObj = SkyDriveObject.create(data.optJSONObject(i));
//				////					skyDriveObjs.add(skyDriveObj);
//				//				}
//			}
//
//			@Override
//			public void onError(LiveOperationException exception, LiveOperation operation) {
//				exception.printStackTrace();
//			}
//		});
//
//		return new ArrayList<DBFile>();
//	}
//}
