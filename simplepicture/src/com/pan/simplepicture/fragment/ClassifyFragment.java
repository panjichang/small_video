package com.pan.simplepicture.fragment;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;

import java.util.ArrayList;

import android.view.View;
import android.widget.ListView;

import com.nhaarman.listviewanimations.appearance.simple.RotateBottomAnimationAdapter;
import com.pan.simplepicture.ConstantValue;
import com.pan.simplepicture.R;
import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.adapter.BaseListAdapter;
import com.pan.simplepicture.been.Category;
import com.pan.simplepicture.holder.ClassifyHolder;
import com.pan.simplepicture.utils.MapUtil;
import com.pan.simplepicture.widget.LoadingPage.LoadResult;

public class ClassifyFragment extends BaseFragment {
	private ArrayList<Category> list;
	private ListView list_categoty;

	public ClassifyFragment(BaseActivity activity) {
		super(activity);
	}

	@Override
	protected LoadResult load() {
		list = MapUtil.getCategory(mActivity);
		if (list == null) {
			return LoadResult.ERROR;
		}
		if (list.size() <= 0) {
			return LoadResult.EMPTY;
		}
		return LoadResult.SUCCEED;
	}

	@Override
	protected View createLoadedView() {
		View view = mActivity.inflate(R.layout.fragment_list);
		list_categoty = (ListView) view.findViewById(R.id.list_categoty);
		PtrFrameLayout ptrFrameLayout = (PtrFrameLayout) view
				.findViewById(R.id.fragment_ptr_home_ptr_frame);
		initPtrFrameLayout(ptrFrameLayout);
		BaseListAdapter<Category> adapter = new BaseListAdapter<Category>(mActivity, list,
				ClassifyHolder.class);
		RotateBottomAnimationAdapter swingBottomInAnimationAdapter = new RotateBottomAnimationAdapter(
				adapter);
		swingBottomInAnimationAdapter.setAbsListView(list_categoty);

		assert swingBottomInAnimationAdapter.getViewAnimator() != null;
		swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(
				ConstantValue.INITIAL_DELAY_MILLIS);

		list_categoty.setAdapter(swingBottomInAnimationAdapter);

		return view;
	}

	private void initPtrFrameLayout(final PtrFrameLayout ptrFrameLayout) {
		final StoreHouseHeader header = new StoreHouseHeader(mActivity);
		header.setPadding(0, mActivity.dip2px(15), 0, mActivity.dip2px(15));
		header.initWithString(ConstantValue.PULL_STRING);

		ptrFrameLayout.setDurationToCloseHeader(2000);
		ptrFrameLayout.setHeaderView(header);
		ptrFrameLayout.addPtrUIHandler(header);
		ptrFrameLayout.setPtrHandler(new PtrHandler() {
			@Override
			public boolean checkCanDoRefresh(PtrFrameLayout frame,
					View content, View header) {
				return PtrDefaultHandler.checkContentCanBePulledDown(frame,
						content, header);
			}

			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				ptrFrameLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						ptrFrameLayout.refreshComplete();
					}
				}, 1500);
			}
		});
	}
}
