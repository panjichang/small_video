package com.pan.simplepicture.http.protocol;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pan.simplepicture.been.ShortVideo;
import com.pan.simplepicture.http.HttpHelper;

public class HomeProtocol extends BaseProtocol<List<ShortVideo>> {

	@Override
	protected String getKey() {
		return HttpHelper.URL_AT + "animelist_v4";
	}

	@Override
	protected List<ShortVideo> parseFromJson(String json) {
		List<ShortVideo> list = new ArrayList<ShortVideo>();
		try {
			JSONObject object1 = new JSONObject(json);
			JSONObject object2 = object1.optJSONObject("data");
			if ("true".equals(object2.optString("result"))) {
				JSONObject object3 = object2.optJSONObject("list");
				JSONArray jsonArray = object3.optJSONArray("anime");
				for (int i = 0; i < jsonArray.length(); i++) {
					ShortVideo shortVideo = new ShortVideo();
					JSONObject object4 = jsonArray.getJSONObject(i);
					shortVideo.author = object4.optString("Author");
					shortVideo.description = object4.optString("Brief");
					shortVideo.thumbnailV2 = object4.optString("DetailPic");
					shortVideo.duration = object4.optString("Duration");
					shortVideo.thumbnail = object4.optString("HomePic");
					shortVideo.rsId = object4.optString("Id");
					shortVideo.title = object4.optString("Name");
					shortVideo.link = object4.optString("VideoUrl");
					shortVideo.published = object4.optString("UpdateTime");
					JSONObject object5 = object4.optJSONObject("VideoSource");
					shortVideo.player = object5.optString("sd");
					list.add(shortVideo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
