package com.pan.simplepicture.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.SaveCallback;
import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.been.Category;
import com.pan.simplepicture.been.Comment;
import com.pan.simplepicture.been.Material;
import com.pan.simplepicture.been.Menu;

/**
 * 
 * @author lhq
 * 
 */
public class MapUtil {

	/**
	 * map 按照key 排序
	 * 
	 * @param map
	 * @return
	 */
	public static Object[] getKeyArray(Map<String, String> map) {
		Object[] key = map.keySet().toArray();
		// Arrays.sort(key);
		return key;
	}

	/**
	 * 获取请求参数
	 * 
	 * @return
	 */
	public static HashMap<String, String> getParams() {
		HashMap<String, String> params = new HashMap<String, String>();

		return params;
	}

	/**
	 * 获取分类数据
	 */
	public static ArrayList<Category> getCategory(BaseActivity mActivity) {
		String json = IOUtils.getStringFromAssert(mActivity, "category.json");
		ArrayList<Category> list = new ArrayList<Category>();
		try {
			JSONObject object = new JSONObject(json);
			JSONArray jsonArray = object.getJSONArray("category");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Category cate = new Category();
				cate.count = jsonObject.optString("count");
				cate.id = jsonObject.optString("id");
				cate.name = jsonObject.optString("name");
				cate.icon = jsonObject.optString("icon");
				list.add(cate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 获取侧滑菜单列表
	 */
	public static ArrayList<Menu> getMenu(BaseActivity mActivity) {
		String json = IOUtils.getStringFromAssert(mActivity, "menu.json");
		ArrayList<Menu> list = new ArrayList<Menu>();
		try {
			JSONObject object = new JSONObject(json);
			JSONArray jsonArray = object.getJSONArray("menu");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Menu menu = new Menu();
				menu.name = jsonObject.optString("name");
				menu.icon = jsonObject.optString("icon");
				menu.id = jsonObject.optInt("id");
				list.add(menu);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 储存评论
	 */
	public static void saveComment(Comment comment, String rsId,
			SaveCallback callBack) {
		AVObject avComment = new AVObject("Comment");
		avComment.put("content", comment.content);
		avComment.put("rsId", rsId);
		avComment.put("published", comment.published);
		avComment.put("location", comment.user.location);
		avComment.put("gender", comment.user.gender);
		avComment.put("screen_name", comment.user.screen_name);
		avComment.put("profile_image_url", comment.user.profile_image_url);
		avComment.saveInBackground(callBack);
	}

	/**
	 * 储存评论
	 */
	public static void saveProvideMaterial(Material material,
			SaveCallback callBack) {
		AVObject avComment = new AVObject("Material");
		avComment.put("email", material.email);
		avComment.put("title", material.title);
		avComment.put("des", material.des);
		avComment.saveInBackground(callBack);
	}

	/**
	 * 获取评论
	 */
	public static List<AVObject> getComments(String rsId, int limit, int skip) {
		AVQuery<AVObject> query = new AVQuery<AVObject>("Comment");
		query.whereEqualTo("rsId", rsId);
		query.setLimit(5);
		query.setSkip(0);
		query.orderByDescending("published");
		try {
			return query.find();
		} catch (AVException e) {
			e.printStackTrace();
		}
		return null;
	}
}
