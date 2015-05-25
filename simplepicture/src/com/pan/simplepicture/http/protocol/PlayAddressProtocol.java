package com.pan.simplepicture.http.protocol;

import org.json.JSONObject;

import com.pan.simplepicture.http.HttpHelper;

public class PlayAddressProtocol extends BaseProtocol<String> {

	@Override
	protected String getKey() {
		return HttpHelper.URL_BEATY + "resources/getPlayAdressByIdAndLink";
	}

	@Override
	protected String parseFromJson(String json) {
		try {
			JSONObject jsonObj = new JSONObject(json);
			return jsonObj.optString("player");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
