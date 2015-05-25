package com.pan.simplepicture.fragment;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;

import java.util.List;
import java.util.TreeMap;

import android.view.View;
import android.widget.ListView;

import com.nhaarman.listviewanimations.appearance.simple.RotateBottomAnimationAdapter;
import com.pan.simplepicture.ConstantValue;
import com.pan.simplepicture.R;
import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.adapter.BaseListAdapter;
import com.pan.simplepicture.adapter.BaseListAdapter.OnLoadMoreListener;
import com.pan.simplepicture.been.ShortVideo;
import com.pan.simplepicture.holder.ResourcesHolder;
import com.pan.simplepicture.http.protocol.HomeProtocol;
import com.pan.simplepicture.utils.MD5Utils;
import com.pan.simplepicture.widget.LoadingPage.LoadResult;

public class AllFragment extends BaseFragment implements
		OnLoadMoreListener<ShortVideo> {

	private List<ShortVideo> list;
	private int page = 1;
	private TreeMap<String, String> map;

	public AllFragment(BaseActivity activity) {
		super(activity);
	}

	@Override
	protected LoadResult load() {
		map = new TreeMap<String, String>();
		long time = System.currentTimeMillis() / 1000L;
		map.put("api_key", "android");
		map.put("timestamp", String.valueOf(time));
		map.put("page", String.valueOf(page));
		String str = MD5Utils.getAccessToken(map);
		map.put("access_token", str);
		HomeProtocol protocol = new HomeProtocol();
		list = protocol.loadInfo(mActivity, map, true);
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
		ListView list_categoty = (ListView) view
				.findViewById(R.id.list_categoty);

		PtrFrameLayout ptrFrameLayout = (PtrFrameLayout) view
				.findViewById(R.id.fragment_ptr_home_ptr_frame);
		initPtrFrameLayout(ptrFrameLayout);

		BaseListAdapter<ShortVideo> mAdapter = new BaseListAdapter<ShortVideo>(
				mActivity, list, ResourcesHolder.class);
		mAdapter.setOnLoadMoreListener(this);
		RotateBottomAnimationAdapter swingBottomInAnimationAdapter = new RotateBottomAnimationAdapter(
				mAdapter);
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
	public List<ShortVideo> OnLoadMore(int starIndex) {
		long time = System.currentTimeMillis() / 1000L;
		map.clear();
		map.put("api_key", "android");
		map.put("timestamp", String.valueOf(time));
		map.put("page", String.valueOf(++page));
		String str = MD5Utils.getAccessToken(map);
		map.put("access_token", str);
		HomeProtocol protocol = new HomeProtocol();
		return protocol.loadInfo(mActivity, map, true);
	}

}
