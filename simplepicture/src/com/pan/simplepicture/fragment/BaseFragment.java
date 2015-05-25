package com.pan.simplepicture.fragment;

import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.utils.ViewUtils;
import com.pan.simplepicture.widget.LoadingPage;
import com.pan.simplepicture.widget.LoadingPage.LoadResult;
import com.umeng.analytics.MobclickAgent;

public abstract class BaseFragment extends Fragment {
	protected BaseActivity mActivity;
	protected LoadingPage mContentView;
	protected int index = 1;
	protected int pageSize = 10;
	protected HashMap<String, String> params;

	public BaseFragment(BaseActivity activity) {
		mActivity = activity;
		mContentView = new LoadingPage(mActivity) {
			@Override
			public LoadResult load() {
				return BaseFragment.this.load();
			}

			@Override
			public View createLoadedView() {
				return BaseFragment.this.createLoadedView();
			}
		};
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewUtils.removeSelfFromParent(mContentView);
		return mContentView;
	}

	public void update() {
	}

	public void show() {
		if (mContentView != null) {
			mContentView.show();
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(mActivity);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(mActivity);
	}

	protected abstract LoadResult load();

	protected abstract View createLoadedView();
}
