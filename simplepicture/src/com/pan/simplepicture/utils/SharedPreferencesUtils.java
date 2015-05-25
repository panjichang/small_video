package com.pan.simplepicture.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.been.ShortVideo;

public class SharedPreferencesUtils {
	public static void saveString(Context mActivity, String key,
			String value) {
		SharedPreferences sp = mActivity.getSharedPreferences("config",
				Context.MODE_PRIVATE);
		sp.edit().putString(key, value).commit();
	}

	public static String getString(Context mActivity, String key,
			String defValue) {
		SharedPreferences sp = mActivity.getSharedPreferences("config",
				Context.MODE_PRIVATE);
		return sp.getString(key, defValue);
	}

	public static void setBoolean(BaseActivity mActivity, String key,
			boolean value) {
		SharedPreferences sp = mActivity.getSharedPreferences("config",
				Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}

	public static boolean isFirst(BaseActivity mActivity) {
		return getBoolean(mActivity, "isfirst", true);
	}

	public static boolean getBoolean(BaseActivity mActivity, String key,
			boolean defValue) {
		SharedPreferences sp = mActivity.getSharedPreferences("config",
				Context.MODE_PRIVATE);
		return sp.getBoolean(key, defValue);
	}

	public static void saveHeight(BaseActivity mActivity, int height) {
		SharedPreferences sp = mActivity.getSharedPreferences("config",
				Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putInt("height", height);
		edit.commit();
	}

	public static int getHeight(BaseActivity mActivity) {
		SharedPreferences sp = mActivity.getSharedPreferences("config",
				Context.MODE_PRIVATE);
		return sp.getInt("height", 0);
	}

	public static void saveCollection(BaseActivity mActivity,
			ShortVideo shortVideo) {
		SharedPreferences sp = mActivity.getSharedPreferences("collection",
				Context.MODE_PRIVATE);
		String json = sp.getString("collect", "");
		ArrayList<ShortVideo> list = null;
		Gson gson = new Gson();
		if (TextUtils.isEmpty(json)) {
			list = new ArrayList<ShortVideo>();
			sp.edit().putString("ids", shortVideo.rsId).commit();
		} else {
			list = gson.fromJson(json, new TypeToken<ArrayList<ShortVideo>>() {
			}.getType());
			sp.edit()
					.putString("ids",
							sp.getString("ids", "") + "," + shortVideo.rsId)
					.commit();
		}
		list.add(shortVideo);
		sp.edit().putString("collect", gson.toJson(list)).commit();
	}

	public static ArrayList<ShortVideo> getCollection(BaseActivity mActivity) {
		SharedPreferences sp = mActivity.getSharedPreferences("collection",
				Context.MODE_PRIVATE);
		String json = sp.getString("collect", "");
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		Gson gson = new Gson();
		return gson.fromJson(json, new TypeToken<ArrayList<ShortVideo>>() {
		}.getType());
	}

	public static String getCollectionId(BaseActivity mActivity) {
		SharedPreferences sp = mActivity.getSharedPreferences("collection",
				Context.MODE_PRIVATE);
		return sp.getString("ids", "");
	}

	public static void cancelCollection(BaseActivity mActivity,
			ShortVideo shortVideo) {
		ArrayList<ShortVideo> list = getCollection(mActivity);
		String ids = getCollectionId(mActivity);
		if (!ids.contains(",")) {
			ids = ids.replace(shortVideo.rsId, "");
		} else if (ids.startsWith(shortVideo.rsId)) {
			ids = ids.replace(shortVideo.rsId + ",", "");
		} else {
			ids = ids.replace("," + shortVideo.rsId, "");
		}
		SharedPreferences sp = mActivity.getSharedPreferences("collection",
				Context.MODE_PRIVATE);
		sp.edit().putString("ids", ids).commit();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).rsId.equals(shortVideo.rsId)) {
				list.remove(i);
				break;
			}
		}
		Gson gson = new Gson();
		sp.edit().putString("collect", gson.toJson(list)).commit();
	}
}
