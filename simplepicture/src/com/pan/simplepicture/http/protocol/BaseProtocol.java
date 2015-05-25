package com.pan.simplepicture.http.protocol;

import java.net.URLEncoder;
import java.util.Map;

import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.http.HttpHelper;
import com.pan.simplepicture.http.HttpHelper.HttpResult;
import com.pan.simplepicture.utils.MapUtil;
import com.pan.simplepicture.utils.SharedPreferencesUtils;

/**
 * Created by efida on 2014/6/7.
 */
public abstract class BaseProtocol<T> {
	public static final String cachePath = "";

	/**
	 * 从本来加载缓存数据
	 * 
	 * @param context
	 *            上下文
	 * @param map
	 *            请求参数
	 * @return
	 */
	protected String loadFromLocal(BaseActivity mActivity,
			Map<String, String> map) {
		return SharedPreferencesUtils.getString(mActivity, getKey()
				+ getParames(map, true), "");
	}

	/**
	 * 判断是否加载成功
	 * 
	 * @param json
	 * @return
	 * @throws Exception
	 */
	protected boolean isSuccess(String json) throws Exception {
		if (!TextUtils.isEmpty(json)) {
			JSONObject o = new JSONObject(json);
			int status = o.optInt("status");
			if (200 == status) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 缓存数据
	 * 
	 * @param context
	 * @param str
	 * @param index
	 */
	protected void saveToLocal(BaseActivity mActivity, String json,
			Map<String, String> map) {
		SharedPreferencesUtils.saveString(mActivity,
				getKey() + getParames(map, true), json);
	}

	// ========================================new==========================================//
	/**
	 * 加载数据
	 * 
	 * @param context
	 * @param map
	 *            请求参数
	 * @param cache
	 *            CacheType 缓存
	 * @return
	 */
	public T loadInfo(BaseActivity mActivity, Map<String, String> map,
			boolean cache) {
		String json = null;
		if (cache) {
			String net = loadFromNet(map);
			if (net != null) {
				json = net;
				// 3.把数据保存到本地保存到本地
				saveToLocal(mActivity, json, map);
			} else {
				json = loadFromLocal(mActivity, map);
			}
		}
		if (TextUtils.isEmpty(json)) {
			return null;
		}

		return parseFromJson(json);
	}

	/**
	 * 从网络上加载数据
	 * 
	 * @param map
	 * @return
	 */
	private String loadFromNet(Map<String, String> map) {
		String result = null;
		String url = getKey() + getParames(map, false);
		Log.i(getKey(), url);
		HttpResult httpResult = HttpHelper.get(url);
		if (httpResult != null) {
			result = httpResult.getString();
			httpResult.close();
		}
		return result;
	}

	/** 该协议的访问地址 */
	protected abstract String getKey();

	/**
	 * 把参数拼接成url参数的形式
	 * 
	 * @param map
	 *            请求参数的集合
	 * @return
	 */
	protected String getParames(Map<String, String> map, boolean isSave) {
		Object[] keys = MapUtil.getKeyArray(map);
		StringBuffer sb = new StringBuffer();
		for (Object key : keys) {
			String strKey = key.toString();
			if (isSave
					&& ("timestamp".equals(strKey) || "access_token"
							.equals(strKey))) {
				continue;
			}
			sb.append("&" + strKey + "=" + map.get(strKey));
		}
		return sb.toString().replaceFirst("&", "?");
	}

	/** 从json中解析 */
	protected abstract T parseFromJson(String json);
}
