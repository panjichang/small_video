package com.pan.simplepicture.widget;

import in.srain.cube.image.CubeImageView;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.image.impl.DefaultImageLoadHandler;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pan.simplepicture.R;
import com.pan.simplepicture.been.Comment;
import com.pan.simplepicture.utils.StringUtils;

public class CommentLinearLayout extends RelativeLayout {
	private Comment comment;
	private ImageLoader imageLoader;

	public CommentLinearLayout(Context context, Comment comment,
			ImageLoader imageLoader) {
		super(context);
		this.comment = comment;
		this.imageLoader = imageLoader;
		initView(context);
	}

	private void initView(Context context) {
		View view = View.inflate(context, R.layout.comment_item, this);
		CubeImageView touxiang = (CubeImageView) view
				.findViewById(R.id.touxiang);
		TextView user = (TextView) view.findViewById(R.id.user);
		TextView time = (TextView) view.findViewById(R.id.time);
		TextView content = (TextView) view.findViewById(R.id.content);
		touxiang.loadImage(imageLoader, comment.user.profile_image_url);
		user.setText(comment.user.screen_name);
		time.setText(StringUtils.dayFormatter(comment.published));
		content.setText(comment.content);
	}
}
