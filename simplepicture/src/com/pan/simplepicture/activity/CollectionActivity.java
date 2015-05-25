package com.pan.simplepicture.activity;

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
import com.pan.simplepicture.adapter.BaseListAdapter;
import com.pan.simplepicture.been.ShortVideo;
import com.pan.simplepicture.holder.ResourcesHolder;
import com.pan.simplepicture.utils.SharedPreferencesUtils;
import com.pan.simplepicture.widget.LoadingPage.LoadResult;
/**
 * 我的收藏
 * @author pan
 *
 */
public class CollectionActivity extends BaseActivity {

	private ArrayList<ShortVideo> list;

	@Override
	protected LoadResult load() {
		list = SharedPreferencesUtils.getCollection(this);
		if (list == null || list.size() == 0) {
			return LoadResult.EMPTY;
		}
		return LoadResult.SUCCEED;
	}

	private ListView list_collection;
	private BaseListAdapter<ShortVideo> mAdapter;

	@Override
	protected View createLoadedView() {
		View view = inflate(R.layout.fragment_list);
		list_collection = (ListView) view.findViewById(R.id.list_categoty);
		PtrFrameLayout ptrFrameLayout = (PtrFrameLayout) view
				.findViewById(R.id.fragment_ptr_home_ptr_frame);
		initPtrFrameLayout(ptrFrameLayout);
		mAdapter = new BaseListAdapter<ShortVideo>(this, list,
				ResourcesHolder.class);
		RotateBottomAnimationAdapter swingBottomInAnimationAdapter = new RotateBottomAnimationAdapter(
				mAdapter);
		swingBottomInAnimationAdapter.setAbsListView(list_collection);
		assert swingBottomInAnimationAdapter.getViewAnimator() != null;
		swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(
				ConstantValue.INITIAL_DELAY_MILLIS);
		list_collection.setAdapter(swingBottomInAnimationAdapter);
		return view;
	}

	private void initPtrFrameLayout(final PtrFrameLayout ptrFrameLayout) {
		final StoreHouseHeader header = new StoreHouseHeader(this);
		header.setPadding(0, this.dip2px(15), 0, this.dip2px(15));
		header.initWithString(ConstantValue.PULL_STRING);

		ptrFrameLayout.setDurationToCloseHeader(2000);
		ptrFrameLayout.setHeaderView(header);
		ptrFrameLayout.addPtrUIHandler(header);
		ptrFrameLayout.setPtrHandler(new PtrHandler() {
			@Override
			public boolean checkCanDoRefresh(PtrFrameLayout frame,
					View content, View header) {
				return PtrDefaultHandler.checkContentCanBePulledDown(
						ptrFrameLayout, content, header);
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

	@Override
	protected void setActionBarTitle() {
		mActionBar.setTitle("我的收藏");
	}
}
