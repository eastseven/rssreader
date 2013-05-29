package org.dongq.android.rssreader;

import android.app.Application;
import android.util.Log;

public final class RssReader extends Application {

	private static final String tag = "RR_Application";
	
//	private SharedPreferences sp;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(tag, "onCreate...");
		
//		this.sp = getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
//		boolean isFirstLogin = this.sp.getBoolean(Constant.IS_FIRST_LOGIN, true);
//		Intent i = null;
//		if(isFirstLogin) {
//			i = new Intent(this, HelloActivity.class);
//			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			startActivity(i);
//		} 
//		else {
//			startActivity(new Intent(this, MainActivity.class));
//		}
	}
	
}
