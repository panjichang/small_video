package com.pan.simplepicture.activity;

import net.youmi.android.AdManager;
import net.youmi.android.offers.OffersManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.pan.simplepicture.ConstantValue;
import com.pan.simplepicture.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

/**
 * 欢迎页
 * 
 * @author pan
 * 
 */
public class SplashActivity extends Activity {
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		UmengUpdateAgent.update(this);
		UmengUpdateAgent.setUpdateOnlyWifi(false);

		AdManager.getInstance(this).init(ConstantValue.YOUMI_APPID,
				ConstantValue.YOUMI_APPKEY);
		AdManager.getInstance(this).setUserDataCollect(true);
		OffersManager.getInstance(this).onAppLaunch();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent(SplashActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
		}, 2000);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getSimpleName());
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		MobclickAgent.onPause(this);
	}
}
