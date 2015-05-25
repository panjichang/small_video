package com.pan.simplepicture.activity;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;

import java.util.List;
import java.util.TreeMap;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.nhaarman.listviewanimations.appearance.simple.RotateBottomAnimationAdapter;
import com.pan.simplepicture.ConstantValue;
import com.pan.simplepicture.R;
import com.pan.simplepicture.adapter.BaseListAdapter;
import com.pan.simplepicture.adapter.BaseListAdapter.OnLoadMoreListener;
import com.pan.simplepicture.been.Category;
import com.pan.simplepicture.been.ShortVideo;
import com.pan.simplepicture.holder.ResourcesHolder;
import com.pan.simplepicture.http.protocol.HomeProtocol;
import com.pan.simplepicture.http.protocol.HotProtocol;
import com.pan.simplepicture.http.protocol.ResourcesProtocol;
import com.pan.simplepicture.utils.MD5Utils;
import com.pan.simplepicture.widget.LoadingPage.LoadResult;

/**
 * 视频资源
 * 
 * @author pan
 * 
 */
public class ResourcesActivity extends BaseActivity implements
		OnLoadMoreListener<ShortVideo> {

	private List<ShortVideo> list;
	private int pageNo = 0;
	private int pageSize = 10;
	private ResourcesProtocol resProtocol;
	private Category category;
	private int page = 1;
	private TreeMap<String, String> treeMap;
	private HomeProtocol homeProtocol;
	private String title;
	private HotProtocol hotProtocol;
	private boolean cache = true;

	@Override
	protected void init() {
		Intent intent = getIntent();
		category = (Category) intent.getSerializableExtra("category");
		title = intent.getStringExtra("title");
	}

	@Override
	protected void setActionBarTitle() {
		if (category != null) {
			mActionBar.setTitle(category.name);
		} else {
			mActionBar.setTitle(title);
		}
	}

	@Override
	protected LoadResult load() {
		if (category != null && "0".equals(category.id)) {
			map.put("pageNo", String.valueOf(pageNo));
			map.put("pageSize", String.valueOf(pageSize));
			resProtocol = new ResourcesProtocol();
			list = resProtocol.loadInfo(this, map, cache);
		} else if (category != null) {
			treeMap = new TreeMap<String, String>();
			long time = System.currentTimeMillis() / 1000L;
			treeMap.put("api_key", "android");
			treeMap.put("limit", "10");
			treeMap.put("category", category.id);
			treeMap.put("timestamp", String.valueOf(time));
			treeMap.put("page", String.valueOf(page));
			String str = MD5Utils.getAccessToken(treeMap);
			treeMap.put("access_token", str);
			homeProtocol = new HomeProtocol();
			list = homeProtocol.loadInfo(this, treeMap, cache);
		} else if (!TextUtils.isEmpty(title)) {
			if ("大家都在看".equals(title)) {
				map.put("pageNo", String.valueOf(pageNo));
				map.put("pageSize", String.valueOf(pageSize));
				hotProtocol = new HotProtocol(true);
				list = hotProtocol.loadInfo(this, map, cache);
			} else {
				treeMap = new TreeMap<String, String>();
				long time = System.currentTimeMillis() / 1000L;
				treeMap.put("api_key", "android");
				treeMap.put("limit", "10");
				treeMap.put("order", "random");
				treeMap.put("timestamp", String.valueOf(time));
				String str = MD5Utils.getAccessToken(treeMap);
				treeMap.put("access_token", str);
				hotProtocol = new HotProtocol(false);
				list = hotProtocol.loadInfo(this, treeMap, cache);
			}
		}
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
		View view = inflate(R.layout.fragment_list);
		ListView list_categoty = (ListView) view
				.findViewById(R.id.list_categoty);

		PtrFrameLayout ptrFrameLayout = (PtrFrameLayout) view
				.findViewById(R.id.fragment_ptr_home_ptr_frame);
		initPtrFrameLayout(ptrFrameLayout);

		BaseListAdapter<ShortVideo> mAdapter = new BaseListAdapter<ShortVideo>(
				this, list, ResourcesHolder.class);
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
	public List<ShortVideo> OnLoadMore(int startIndex) {
		// TODO Auto-generated method stub
		Log.i("OnLoadMore", "加载更多");
		if (category != null && "0".equals(category.id)) {
			map.put("pageNo", String.valueOf(startIndex));
			return resProtocol.loadInfo(this, map, cache);
		} else if (category != null) {
			treeMap.clear();
			treeMap.put("api_key", "android");
			treeMap.put("limit", "10");
			treeMap.put("category", category.id);
			treeMap.put("timestamp",
					String.valueOf(System.currentTimeMillis() / 1000L));
			treeMap.put("page", String.valueOf(++page));
			treeMap.put("access_token", MD5Utils.getAccessToken(treeMap));
			return homeProtocol.loadInfo(this, treeMap, cache);
		} else if (!TextUtils.isEmpty(title)) {
			if ("大家都在看".equals(title)) {
				map.put("pageNo", String.valueOf(startIndex));
				return hotProtocol.loadInfo(this, map, cache);
			} else {
				return hotProtocol.loadInfo(this, treeMap, cache);
			}
		}
		return null;
	}
}
