package com.pan.simplepicture;

import com.avos.avoscloud.AVOSCloud;
import com.umeng.analytics.MobclickAgent;

import in.srain.cube.Cube;
import android.app.Application;

public class PanApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		MobclickAgent.openActivityDurationTrack(false);
		AVOSCloud.initialize(getApplicationContext(), ConstantValue.AVOS_APPID,
				ConstantValue.AVOS_APPKEY);
	}
}
