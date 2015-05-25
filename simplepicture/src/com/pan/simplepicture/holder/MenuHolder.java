package com.pan.simplepicture.holder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pan.simplepicture.R;
import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.activity.CollectionActivity;
import com.pan.simplepicture.activity.ProvideMaterialActivity;
import com.pan.simplepicture.activity.ResourcesActivity;
import com.pan.simplepicture.activity.SettingActivity;
import com.pan.simplepicture.been.Menu;
import com.pan.simplepicture.utils.IOUtils;

public class MenuHolder extends BaseHolder<Menu> {

	private ImageView menu_icon;
	private TextView menu_name;

	public MenuHolder(BaseActivity activity, Menu t) {
		super(activity, t);
	}

	@Override
	public void onClick(View arg0) {
		Intent intent = null;
		switch (t.id) {
		case 1:
			intent = new Intent(mActivity, CollectionActivity.class);
			break;
		case 2:
		case 3:
			intent = new Intent(mActivity, ResourcesActivity.class);
			break;
		case 4:
			intent = new Intent(mActivity, ProvideMaterialActivity.class);
			break;
		case 5:
			intent = new Intent(mActivity, SettingActivity.class);
			break;
		default:
			break;
		}
		if (intent != null) {
			intent.putExtra("title", t.name);
			mActivity.startActivity(intent);
			mActivity.int4Right();
		}
	}

	@Override
	protected View initView() {
		View view = mActivity.inflate(R.layout.menu_item);
		view.setOnClickListener(this);
		menu_icon = (ImageView) view.findViewById(R.id.menu_icon);
		menu_name = (TextView) view.findViewById(R.id.menu_name);
		return view;
	}

	@Override
	protected void refreshView() {
		menu_name.setText(t.name);
		Bitmap bitmap = IOUtils.getImageFromAssetsFile(mActivity, t.icon);
		menu_icon.setImageDrawable(new BitmapDrawable(bitmap));
	}

}
