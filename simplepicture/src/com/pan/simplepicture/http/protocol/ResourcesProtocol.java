package com.pan.simplepicture.http.protocol;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pan.simplepicture.been.ShortVideo;
import com.pan.simplepicture.http.HttpHelper;
import com.pan.simplepicture.utils.StringUtils;

public class ResourcesProtocol extends BaseProtocol<ArrayList<ShortVideo>> {

	@Override
	protected String getKey() {
		// TODO Auto-generated method stub
		return HttpHelper.URL_BEATY+"resources/getResources";
	}

	@Override
	protected ArrayList<ShortVideo> parseFromJson(String json) {
		ArrayList<ShortVideo> list = new ArrayList<ShortVideo>();
		try {
			JSONObject object = new JSONObject(json);
			JSONArray jsonArray = object.optJSONArray("resources");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject o = jsonArray.getJSONObject(i);
				ShortVideo video = new ShortVideo();
				video.description = o.optString("description");
				video.duration = StringUtils.timeFormatter(o.optString("duration"));
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

}
