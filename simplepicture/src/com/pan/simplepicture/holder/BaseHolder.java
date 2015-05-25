package com.pan.simplepicture.holder;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.pan.simplepicture.activity.BaseActivity;

/**
 * 
 */
public abstract class BaseHolder<T> implements OnClickListener{
	protected BaseActivity mActivity;
	protected View mRootView;
	protected int mPosition;
	protected T t;

	public BaseHolder(BaseActivity activity, T t) {
		mActivity = activity;
		this.t = t;
		mRootView = initView();
		mRootView.setTag(this);
		setData(t);
	}


	public BaseActivity getActivity() {
		return mActivity;
	}

	public View getRootView() {
		return mRootView;
	}

	public void setData(T t) {
		this.t = t;
		refreshView();
	}

	public T getData() {
		return this.t;
	}

	public void setPosition(int position) {
		mPosition = position;
	}

	public int getPosition() {
		return mPosition;
	}

	public void recycleImageView(ImageView view) {
		Object tag = view.getTag();
		if (tag != null && tag instanceof String) {
			String key = (String) tag;
			// mActivity.imageLoader.cancel(key);
			// view.setImageDrawable(null);
		}
	}

	/** 子类必须覆盖用于实现UI初始化 */
	protected abstract View initView();

	/** 子类必须覆盖用于实现UI刷新 */
	protected abstract void refreshView();

	/** 用于回收 */
	public void recycle() {

	}
}
