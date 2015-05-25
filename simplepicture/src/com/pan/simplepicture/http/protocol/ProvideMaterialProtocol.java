package com.pan.simplepicture.http.protocol;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pan.simplepicture.been.Comment;
import com.pan.simplepicture.been.User;
import com.pan.simplepicture.http.HttpHelper;

public class ProvideMaterialProtocol extends BaseProtocol<Boolean> {
	// http://115.28.54.40:8080/beautyideaInterface/api/v1/adviseres/save_adviseres?deviceModel=MI+2S&
	// adId=1D975AB95DCAC407C992D49FA9F160C0&
	// adviseContent=%E5%8F%AF%E5%8F%A3%E5%8F%AF%E4%B9%90%E4%BA%86&
	// plamformVersion=4.1.1&deviceName=Xiaomi&plamform=Android&
	// email=Pan%40qq.com&adviseresTitle=KTV%E8%80%81%E5%A7%9C&imieId=1D975AB95DCAC407C992D49FA9F160C0
	//
	//&adId=1D975AB95DCAC407C992D49FA9F160C0

	@Override
	protected String getKey() {
		return HttpHelper.URL_BEATY + "adviseres/save_adviseres";
	}

	@Override
	protected Boolean parseFromJson(String json) {
		ArrayList<Comment> list = new ArrayList<Comment>();
		try {
			JSONObject object = new JSONObject(json);
			JSONArray jsonArray = object.optJSONArray("comments");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Comment comment = new Comment();
				comment.commentId = jsonObject.optString("commentId");
				comment.content = jsonObject.optString("content");
				comment.flag = jsonObject.optString("flag");
				comment.published = jsonObject.optString("published");
				JSONObject userJson = jsonObject.optJSONObject("users");
				User user = new User();
				comment.user = user;
				list.add(comment);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
