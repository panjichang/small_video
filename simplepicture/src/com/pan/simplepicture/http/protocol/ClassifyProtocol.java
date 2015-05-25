package com.pan.simplepicture.http.protocol;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.pan.simplepicture.been.Module;
import com.pan.simplepicture.http.HttpHelper;

public class ClassifyProtocol extends BaseProtocol<ArrayList<Module>> {

	@Override
	protected String getKey() {
		return HttpHelper.URL_BEATY+"modules/getModules";
	}

	@Override
	protected ArrayList<Module> parseFromJson(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		ArrayList<Module> list = new ArrayList<Module>();
		try {
			JSONObject obj = new JSONObject(json);
			JSONArray jsonArray = obj.optJSONArray("modules");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				Module module = new Module();
				module.flag = object.optString("flag");
				module.icon = object.optString("icon");
				module.modulesName = object.optString("modulesName");
				module.modulesId = object.optInt("modulesId");
				module.sort = object.optInt("sort");
				list.add(module);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("size:" + list.size());
		return list;
	}
}
