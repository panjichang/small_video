package com.pan.simplepicture.http.protocol;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pan.simplepicture.been.ShortVideo;
import com.pan.simplepicture.http.HttpHelper;
import com.pan.simplepicture.utils.StringUtils;

public class HotProtocol extends BaseProtocol<ArrayList<ShortVideo>> {
	private boolean flag = false;

	public HotProtocol(boolean flag) {
		this.flag = flag;
	}

	@Override
	protected String getKey() {
		if (flag) {
			return HttpHelper.URL_BEATY + "resources/getHotResources";
		} else {
			return HttpHelper.URL_AT + "animelist_v4";
		}
	}

	@Override
	protected ArrayList<ShortVideo> parseFromJson(String json) {
		ArrayList<ShortVideo> list = new ArrayList<ShortVideo>();
		try {
			if (flag) {
				JSONObject object = new JSONObject(json);
				JSONArray jsonArray = object.optJSONArray("resources");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject o = jsonArray.getJSONObject(i);
					ShortVideo video = new ShortVideo();
					video.description = o.optString("description");
					video.duration = StringUtils.timeFormatter(o
							.optString("duration"));
					video.link = o.optString("link");
					video.player = o.optString("player");
					video.published = o.optString("published");
					video.rsId = o.optString("rsId");
					video.tag = o.optString("tag");
					video.thumbnail = o.optString("thumbnail");
					video.thumbnailV2 = o.optString("thumbnailV2");
					video.title = o.optString("title");
					list.add(video);
				}
			} else {
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
						JSONObject object5 = object4
								.optJSONObject("VideoSource");
						shortVideo.player = object5.optString("sd");
						list.add(shortVideo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

}
