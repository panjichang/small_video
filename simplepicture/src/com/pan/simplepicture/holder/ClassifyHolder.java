package com.pan.simplepicture.holder;

import net.youmi.android.listener.Interface_ActivityListener;
import net.youmi.android.offers.OffersManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pan.simplepicture.R;
import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.activity.ResourcesActivity;
import com.pan.simplepicture.been.Category;
import com.pan.simplepicture.utils.IOUtils;

public class ClassifyHolder extends BaseHolder<Category> {

	private TextView title;

	private ImageView iv_cube;

	public ClassifyHolder(BaseActivity mActivity, Category category) {
		super(mActivity, category);
	}

	@Override
	protected View initView() {
		View view = mActivity.inflate(R.layout.cate_item);
		title = (TextView) view.findViewById(R.id.title);
		iv_cube = (ImageView) view.findViewById(R.id.iv_cube);
		iv_cube.setOnClickListener(this);
		return view;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void refreshView() {
		title.setText(t.name);
		Bitmap bitmap = IOUtils.getImageFromAssetsFile(mActivity, t.icon);
		iv_cube.setBackgroundDrawable(new BitmapDrawable(bitmap));
	}

	@Override
	public void onClick(View arg0) {
		if (!"-1".equals(t.id)) {
			Intent intent = new Intent(mActivity, ResourcesActivity.class);
			intent.putExtra("category", t);
			mActivity.startActivity(intent);
			mActivity.int4Right();
		} else {
			// 调用方式二：直接打开全屏积分墙，并且监听积分墙退出的事件onDestory
			OffersManager.getInstance(mActivity).showOffersWall(
					new Interface_ActivityListener() {

						/**
						 * 但积分墙销毁的时候，即积分墙的Activity调用了onDestory的时候回调
						 */
						@Override
						public void onActivityDestroy(Context context) {
						}
					});
		}
	}

}
