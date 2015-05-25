package com.pan.simplepicture.holder;

import in.srain.cube.image.CubeImageView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.pan.simplepicture.R;
import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.been.Comment;
import com.pan.simplepicture.utils.StringUtils;

public class CommentHolder extends BaseHolder<Comment>{
	
	private CubeImageView touxiang;
	private TextView time;
	private TextView content;
	private TextView user;

	public CommentHolder(BaseActivity activity, Comment t) {
		super(activity, t);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	protected View initView() {
		View view = mActivity.inflate(R.layout.comment_item);
		touxiang = (CubeImageView) view.findViewById(R.id.touxiang);
		time = (TextView) view.findViewById(R.id.time);
		content = (TextView) view.findViewById(R.id.content);
		user = (TextView) view.findViewById(R.id.user);
		return view;
	}

	@Override
	protected void refreshView() {
		mActivity.setRandomColor();
		touxiang.loadImage(mActivity.imageLoader, t.user.profile_image_url);
		content.setText(t.content);
		user.setText(t.user.screen_name);
		time.setText(StringUtils.dayFormatter(t.published));
	}

}
