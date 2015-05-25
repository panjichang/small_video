package com.pan.simplepicture.widget;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pan.simplepicture.R;
import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.manager.ThreadManager;
import com.pan.simplepicture.utils.ViewUtils;

public abstract class LoadingPage extends FrameLayout {
	private static final int STATE_UNLOADED = 0;
	private static final int STATE_LOADING = 1;
	private static final int STATE_ERROR = 3;
	private static final int STATE_EMPTY = 4;
	private static final int STATE_SUCCEED = 5;
	private static final int TEXT_LOADING = 6;

	private View mLoadingView;
	private View mErrorView;
	private View mEmptyView;
	private View mSucceedView;
	private int point = 1;
	private int mState;
	private BaseActivity mActivity;

	public LoadingPage(BaseActivity activity) {
		super(activity);
		mActivity = activity;
		init();
	}

	private void init() {
		setBackgroundColor(mActivity.getColor(R.color.bg_page));
		mState = STATE_UNLOADED;
		mLoadingView = createLoadingView();
		if (null != mLoadingView) {
			addView(mLoadingView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		}

		mErrorView = createErrorView();
		if (null != mErrorView) {
			addView(mErrorView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		}

		mEmptyView = createEmptyView();
		if (null != mEmptyView) {
			addView(mEmptyView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		}
		setPageVisiableSafe();
	}

	private void setPageVisiableSafe() {
	//	mHandler.removeMessages(TEXT_LOADING);
		long currentThreadId = Thread.currentThread().getId();
		long mainThreadId = mActivity.getMainLooper().getThread().getId();
		if (currentThreadId == mainThreadId) {
			setPageVisiable();
		} else {
			mActivity.post(new Runnable() {
				@Override
				public void run() {
					setPageVisiable();
				}
			});
		}
	}

	private void setPageVisiable() {
		if (null != mLoadingView) {
			mLoadingView.setVisibility(mState == STATE_UNLOADED
					|| mState == STATE_LOADING ? View.VISIBLE : View.GONE);
		}
		if (null != mErrorView) {
			mErrorView.setVisibility(mState == STATE_ERROR ? View.VISIBLE
					: View.GONE);
		}
		if (null != mEmptyView) {
			mEmptyView.setVisibility(mState == STATE_EMPTY ? View.VISIBLE
					: View.GONE);
		}
		if (mState == STATE_SUCCEED) {
			if (mSucceedView == null) {
				mSucceedView = createLoadedView();
				ViewUtils.removeSelfFromParent(mSucceedView);
				addView(mSucceedView, new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			}
			mSucceedView.setVisibility(View.VISIBLE);
		} else if (null != mSucceedView) {
			mSucceedView.setVisibility(View.GONE);
		}
	}

	public void reset() {
		setState(STATE_UNLOADED);
	}

	protected boolean needReset() {
		return mState == STATE_ERROR || mState == STATE_EMPTY;
	}

	protected synchronized void setState(int state) {
		if (state < STATE_UNLOADED || state > STATE_SUCCEED) {
			return;
		}
		mState = state;
		setPageVisiableSafe();
	}

	public synchronized void show() {
		if (needReset()) {
			mState = STATE_UNLOADED;
		}
		if (mState == STATE_UNLOADED) {
			mState = STATE_LOADING;
			LoadingTask task = new LoadingTask();
			ThreadManager.getLongPool().execute(task);
		}
		setPageVisiableSafe();
	}

	protected View createLoadingView() {
		View view = mActivity.inflate(R.layout.loading_page_loading);
		ImageView imageView = (ImageView) view.findViewById(R.id.pb_loading);
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView
				.getBackground();
		animationDrawable.start();
		return view;
	}

	protected View createEmptyView() {
		return mActivity.inflate(R.layout.loading_page_empty);
	}

	protected View createErrorView() {
		View view = mActivity.inflate(R.layout.loading_page_error);
		view.findViewById(R.id.page_bt).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						show();
					}
				});
		return view;
	}

	public abstract View createLoadedView();

	public abstract LoadResult load();

	class LoadingTask implements Runnable {
		@Override
		public void run() {
			LoadResult result = load();
			int state;
			if (result == LoadResult.ERROR) {
				state = STATE_ERROR;
			} else if (result == LoadResult.EMPTY) {
				state = STATE_EMPTY;
			} else {
				state = STATE_SUCCEED;
			}
			setState(state);
		}
	}

	public enum LoadResult {
		ERROR, EMPTY, SUCCEED
	}
}
