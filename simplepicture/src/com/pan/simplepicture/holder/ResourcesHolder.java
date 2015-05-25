package com.pan.simplepicture.holder;

import in.srain.cube.image.CubeImageView;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pan.simplepicture.R;
import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.activity.PlayActivity;
import com.pan.simplepicture.been.ShortVideo;

public class ResourcesHolder extends BaseHolder<ShortVideo> {

	private CubeImageView iv_cube;
	private TextView duration;
	private TextView title;

	public ResourcesHolder(BaseActivity activity, ShortVideo t) {
		super(activity, t);
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == R.id.ll_item) {
			Intent intent = new Intent(mActivity, PlayActivity.class);
			intent.putExtra("shortVideo", t);
			mActivity.startActivity(intent);
			mActivity.int4Right();
		}
	}

	@Override
	protected View initView() {
		View view = mActivity.inflate(R.layout.res_item);
		iv_cube = (CubeImageView) view.findViewById(R.id.iv_cube);
		LinearLayout ll_item = (LinearLayout) view.findViewById(R.id.ll_item);
		ll_item.setOnClickListener(this);
		duration = (TextView) view.findViewById(R.id.duration);
		title = (TextView) view.findViewById(R.id.title);
		return view;
	}

	@Override
	protected void refreshView() {
		duration.setText(t.duration);
		title.setText(t.title);
		mActivity.setRandomColor();
		iv_cube.loadImage(mActivity.imageLoader, t.thumbnailV2);
	}

}
