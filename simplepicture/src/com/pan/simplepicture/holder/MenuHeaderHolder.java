package com.pan.simplepicture.holder;

import in.srain.cube.image.CubeImageView;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.image.impl.DefaultImageLoadHandler;

import java.util.Date;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.pan.simplepicture.ConstantValue;
import com.pan.simplepicture.R;
import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.activity.LoginActivity;
import com.pan.simplepicture.been.User;
import com.pan.simplepicture.utils.SharedPreferencesUtils;

public class MenuHeaderHolder {
	private BaseActivity mActivity;
	private final String onLine = "ONLINEDAY";
	private CubeImageView lightordark;
	private RelativeLayout icon;
	private ImageView iv;
	private TextView sreen_name;
	private ImageLoader imageLoader;

	public MenuHeaderHolder(BaseActivity mActivity) {
		this.mActivity = mActivity;
		imageLoader = ImageLoaderFactory.create(mActivity);
		DefaultImageLoadHandler handler = new DefaultImageLoadHandler(mActivity);
		handler.setImageFadeIn(true);
		handler.setImageRounded(true, mActivity.dip2px(80));
		imageLoader.setImageLoadHandler(handler);

		Gson gson = new Gson();
		String json = SharedPreferencesUtils.getString(mActivity, "user", "");
		ConstantValue.user = gson.fromJson(json, User.class);

	}

	public View getRootView() {
		View view = mActivity.inflate(R.layout.activity_menu);
		lightordark = (CubeImageView) view.findViewById(R.id.lightordark);
		icon = (RelativeLayout) view.findViewById(R.id.icon);
		iv = (ImageView) view.findViewById(R.id.iv);
		sreen_name = (TextView) view.findViewById(R.id.sreen_name);
		TextView totalday = (TextView) view.findViewById(R.id.totalday);
		totalday.setText("您已经在这里待了" + getDay() + "天了!");
		icon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ConstantValue.user == null) {
					// 登录
					Intent intent = new Intent(mActivity, LoginActivity.class);
					mActivity.startActivity(intent);
					mActivity.int4Right();
				}

			}
		});
		setData();
		return view;
	}

	public void setData() {
		if (ConstantValue.user != null) {
			lightordark.loadImage(imageLoader,
					ConstantValue.user.profile_image_url);
			sreen_name.setText("欢迎回来 , " + ConstantValue.user.screen_name
					+ " ! ");
			iv.setVisibility(View.GONE);
		} else {
			Date date = new Date();
			int hour = date.getHours();
			if (hour >= 6 && hour < 18) {
				lightordark.setImageResource(R.drawable.ic_light);
				iv.setImageResource(R.drawable.ic_light);
			} else {
				lightordark.setImageResource(R.drawable.ic_dark);
				iv.setImageResource(R.drawable.ic_dark);
			}
			iv.setVisibility(View.VISIBLE);
			sreen_name.setText("      欢迎回来 , 朋友 ! ");
		}
	}

	private String getDay() {
		String day = "";
		String strTime = SharedPreferencesUtils
				.getString(mActivity, onLine, "");
		if (TextUtils.isEmpty(strTime)) {
			SharedPreferencesUtils.saveString(mActivity, onLine,
					+System.currentTimeMillis() + ":" + 1);
			day = "1";
		} else {
			String[] split = strTime.split(":");
			day = String.valueOf(split[1]);
			long timeMillis = System.currentTimeMillis();
			if (new Date(timeMillis).getDay() != new Date(
					Long.parseLong(split[0])).getDay()) {
				SharedPreferencesUtils.saveString(mActivity, onLine,
						+timeMillis + ":" + ((Integer.parseInt(split[1]) + 1)));
				day = String.valueOf(Integer.parseInt(split[1]) + 1);
			}
		}
		return day;
	}
}
