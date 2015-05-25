package com.pan.simplepicture.activity;

import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.image.impl.DefaultImageLoadHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pan.simplepicture.ConstantValue;
import com.pan.simplepicture.R;
import com.pan.simplepicture.manager.SystemBarTintManager;
import com.pan.simplepicture.manager.SystemBarTintManager.SystemBarConfig;
import com.pan.simplepicture.utils.MD5Utils;
import com.pan.simplepicture.widget.LoadingPage;
import com.pan.simplepicture.widget.LoadingPage.LoadResult;
import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends ActionBarActivity {

	/** UI 线程ID */
	private long mUIThreadId;
	private LayoutInflater mInflater;
	protected ActionBar mActionBar;
	public HashMap<String, String> map = new HashMap<String, String>();
	protected Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			onHandleMessage(msg);
		}
	};
	/** 记录处于前台的Activity */
	private static BaseActivity mForegroundActivity = null;
	/** 记录所有活动的Activity */
	private static final List<BaseActivity> mActivities = new LinkedList<BaseActivity>();
	/**
	 * 加载图片
	 */
	public ImageLoader imageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) {
			showStatus("onCreate");
		}
	/*	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true);
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setStatusBarTintResource(R.color.actionbar_color);
		}*/
		initInfo();
		init();
		initView();
		initActionBar();
	}

	/*@TargetApi(19)
	private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}*/

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		out2Left();
	}

	protected void initInfo() {
		mActivities.add(this);
		mUIThreadId = android.os.Process.myTid();
		// 初始化请求参数
		map.put("deviceModel", Build.MODEL.replace(" ", "+"));
		map.put("plamformVersion", Build.VERSION.RELEASE);
		map.put("deviceName", Build.MANUFACTURER);
		map.put("plamform", "Android");
		map.put("imieId", MD5Utils.MD5(ConstantValue.str + ConstantValue.str));

		// 加载图片
		imageLoader = ImageLoaderFactory.create(this);
		DefaultImageLoadHandler handler = new DefaultImageLoadHandler(this);
		handler.setLoadingImageColor(getStrColor());
		handler.setImageFadeIn(true);
		handler.setImageRounded(false, 0);
		imageLoader.setImageLoadHandler(handler);
	}

	@Override
	protected void onDestroy() {
		mActivities.remove(this);
		if (DEBUG) {
			showStatus("onDestroy");
		}
		super.onDestroy();
	}

	protected void initActionBar() {
		mActionBar = getSupportActionBar();
		setActionBarTitle();
		int titleId = Resources.getSystem().getIdentifier("action_bar_title",
				"id", "android");
		TextView actTitle = (TextView) findViewById(titleId);
		actTitle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				out2Left();
			}
		});
		actTitle.setTextColor(Color.WHITE);
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setIcon(android.R.color.transparent);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (android.R.id.home):
			finish();
			out2Left();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void setActionBarTitle() {

	}

	protected void init() {

	}

	protected void initView() {
		LoadingPage page = new LoadingPage(this) {

			@Override
			public LoadResult load() {
				// TODO Auto-generated method stub
				return BaseActivity.this.load();
			}

			@Override
			public View createLoadedView() {
				// TODO Auto-generated method stub
				return BaseActivity.this.createLoadedView();
			}
		};
		setContentView(page);
		page.show();
	}

	protected View createLoadedView() {
		// TODO Auto-generated method stub
		return null;
	}

	protected LoadResult load() {
		// TODO Auto-generated method stub
		return null;
	}

	/** 由子类实现如何处理事件 */
	protected void onHandleMessage(Message msg) {

	}

	@Override
	protected void onNewIntent(Intent intent) {
		mUIThreadId = android.os.Process.myTid();
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		mForegroundActivity = this;
		if (DEBUG) {
			showStatus("onResume");
		}
		MobclickAgent.onPageStart(this.getClass().getSimpleName());
		MobclickAgent.onResume(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		mForegroundActivity = null;
		if (DEBUG) {
			showStatus("onPause");
		}
		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		MobclickAgent.onPause(this);
		super.onPause();
	}

	/**
	 * 获取UI线程ID
	 * 
	 * @return UI线程ID
	 */
	public long getUIThreadId() {
		return mUIThreadId;
	}

	public Context getThemeContext() {
		return this;
	}

	public boolean post(Runnable run) {
		return mHandler.post(run);
	}

	public boolean postDelayed(Runnable run, long delay) {
		return mHandler.postDelayed(run, delay);
	}

	public void removeCallbacks(Runnable run) {
		mHandler.removeCallbacks(run);
	}

	public View inflate(int resId) {
		if (null == mInflater) {
			mInflater = LayoutInflater.from(getThemeContext());
		}
		return mInflater.inflate(resId, null);
	}

	public String[] getStringArray(int resId) {
		return getThemeContext().getResources().getStringArray(resId);
	}

	public int getDimen(int resId) {
		return getThemeContext().getResources().getDimensionPixelSize(resId);
	}

	public int getColor(int resId) {
		return getThemeContext().getResources().getColor(resId);
	}

	public ColorStateList getColorStateList(int resId) {
		return getThemeContext().getResources().getColorStateList(resId);
	}

	public Drawable getDrawable(int resId) {
		return getThemeContext().getResources().getDrawable(resId);
	}

	/** dip转换px */
	public int dip2px(int dip) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}

	/** pxz转换dip */
	public int px2dip(int px) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

	/** 对toast的简易封装。线程安全，可以在非UI线程调用。 */
	public void showToastSafe(final int resId, final int duration) {
		if (Process.myTid() == mUIThreadId) {
			// 调用在UI线程
			Toast.makeText(getBaseContext(), resId, duration).show();
		} else {
			// 调用在非UI线程
			post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getBaseContext(), resId, duration).show();
				}
			});
		}
	}

	public void out2Left() {
		overridePendingTransition(R.anim.new_dync_no,
				R.anim.new_dync_out_to_left);
	}

	public void int4Right() {
		overridePendingTransition(R.anim.new_dync_in_from_right,
				R.anim.new_dync_no);
	}

	/** 对toast的简易封装。线程安全，可以在非UI线程调用。 */
	public void showToastSafe(final CharSequence text, final int duration) {
		if (Process.myTid() == mUIThreadId) {
			// 调用在UI线程
			Toast.makeText(getBaseContext(), text, duration).show();
		} else {
			// 调用在非UI线程
			post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getBaseContext(), text, duration).show();
				}
			});
		}
	}

	private static final boolean DEBUG = false;

	@Override
	protected void onRestart() {
		super.onRestart();
		if (DEBUG) {
			showStatus("onRestart");
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG) {
			showStatus("onStop");
		}
	}

	private void showStatus(String status) {
		final String[] className = ((Object) this).getClass().getName()
				.split("\\.");
		Log.i("生命周期", String.format("%s------------%s",
				className[className.length - 1], status));
	}

	public String getStrColor() {
		Random random = new Random();
		int index = random.nextInt(resColors.length);
		return resColors[index];
	}

	public void setRandomColor() {
		((DefaultImageLoadHandler) imageLoader.getImageLoadHandler())
				.setLoadingImageColor(getStrColor());
	}

	private String resColors[] = { "#FFFBBF58", "#FFFF7342", "#FFF56773",
			"#FFE898BF", "#FF695B8E", "#FF13B7D2", "#FF2AB081", "#FF2D72D9",
			"#FFD94130", "#FF1ABC9C", "#FFBC5357" };

	public void showToast(boolean flag, String msg) {
		Toast toast = Toast.makeText(this, "自定义", 0);
		toast.setGravity(Gravity.CENTER, 0, 0);
		View view = View.inflate(this, R.layout.customer_toast, null);
		ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
		if (flag) {
			iv_icon.setImageResource(R.drawable.icon_toast_game_ok);
		} else {
			iv_icon.setImageResource(R.drawable.icon_toast_game_error);
		}
		TextView tv_des = (TextView) view.findViewById(R.id.tv_des);
		tv_des.setText(msg);
		toast.setView(view);
		toast.show();
	}
}
