package com.pan.simplepicture.activity;

import java.util.ArrayList;

import net.youmi.android.offers.OffersManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.pan.simplepicture.R;
import com.pan.simplepicture.adapter.BaseListAdapter;
import com.pan.simplepicture.fragment.BaseFragment;
import com.pan.simplepicture.fragment.FragmentFactory;
import com.pan.simplepicture.holder.MenuHeaderHolder;
import com.pan.simplepicture.holder.MenuHolder;
import com.pan.simplepicture.utils.AppUtils;
import com.pan.simplepicture.utils.MapUtil;
import com.pan.simplepicture.widget.ActionBarDrawerToggle;
import com.pan.simplepicture.widget.DrawerArrowDrawable;
import com.pan.simplepicture.widget.PagerSlidingTabStrip;
import com.pan.simplepicture.widget.ShareDialog;
import com.pan.simplepicture.widget.ShareDialog.OnShareDataListener;
import com.umeng.fb.FeedbackAgent;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.UMSsoHandler;
/**
 * 主页
 * @author pan
 *
 */
public class MainActivity extends BaseActivity {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerArrowDrawable drawerArrow;
	private boolean drawerArrowColor;
	private FeedbackAgent agent = null;

	@Override
	protected void initView() {
		agent = new FeedbackAgent(this);
		agent.sync();
		setContentView(R.layout.activity_sample);
		initContent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_index, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (menuHeaderHolder != null) {
			menuHeaderHolder.setData();
		}
	}

	/** ViewPager的适配器 */
	public class MainPagerAdapter extends FragmentPagerAdapter implements
			OnPageChangeListener {
		String[] mTabTitle;
		BaseFragment[] mFragments;
		int mCurrentIndex;

		public MainPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
			mTabTitle = getStringArray(R.array.tab_names);
			mFragments = new BaseFragment[mTabTitle.length];
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTabTitle[position];
		}

		@Override
		public int getCount() {
			return mTabTitle.length;
		}

		@Override
		public Fragment getItem(int position) {
			if (mFragments[position] == null
					|| !(mFragments[position] instanceof BaseFragment)) {
				mFragments[position] = FragmentFactory.createFragment(position,
						MainActivity.this);
			}
			if (position == mCurrentIndex) {
				mCurrentIndex = -1;
				mFragments[position].show();
			}
			return mFragments[position];
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			// ViewPager滑动状态改变的回调
		}

		@Override
		public void onPageScrolled(int index, float offset, int offsetPx) {
			// ViewPager滑动时的回调
		}

		@Override
		public void onPageSelected(int index) {
			// ViewPager页面被选中的回调
			if (index < mFragments.length) {
				if (mFragments[index] != null) {
					mFragments[index].show();
				} else {
					mCurrentIndex = index;
				}
			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		OffersManager.getInstance(this).onAppExit();
	}

	private PagerSlidingTabStrip mPageTabs;
	private ViewPager mPager;
	private MainPagerAdapter mAdapter;

	private void initContent() {
		mPageTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mPager = (ViewPager) findViewById(R.id.pager);
		mAdapter = new MainPagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mAdapter);
		mPageTabs.setViewPager(mPager);
		mPageTabs.setOnPageChangeListener(mAdapter);
	}

	@Override
	protected void initActionBar() {
		mActionBar = getSupportActionBar();
		mActionBar.setTitle(R.string.app_name);
		mActionBar.setIcon(android.R.color.transparent);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		menuHeaderHolder = new MenuHeaderHolder(this);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.navdrawer);
		mDrawerList.addHeaderView(menuHeaderHolder.getRootView());

		drawerArrow = new DrawerArrowDrawable(this) {
			@Override
			public boolean isLayoutRtl() {
				return false;
			}
		};
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				drawerArrow, R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

		ArrayList<com.pan.simplepicture.been.Menu> list = MapUtil.getMenu(this);
		BaseListAdapter<com.pan.simplepicture.been.Menu> mAdapter = new BaseListAdapter<com.pan.simplepicture.been.Menu>(
				this, list, MenuHolder.class);
		mDrawerList.setFooterDividersEnabled(false);
		mDrawerList.setAdapter(mAdapter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
			return true;
		}
		// 赏个好评
		if (item.getItemId() == R.id.action_important) {
			AppUtils.goMarket(this);
			return true;
		}
		// 用户反馈
		if (item.getItemId() == R.id.action_feedback) {
			agent.startFeedbackActivity();
			int4Right();
			return true;
		}
		// 推荐好友
		if (item.getItemId() == R.id.action_recommend) {
			ShareDialog dialog = new ShareDialog(this, getWindowManager()
					.getDefaultDisplay().getWidth() - 80, this.dip2px(400));
			dialog.setOnShareDataListener(new OnShareDataListener() {

				@Override
				public void onShareData(UMSocialService mController) {
					MainActivity.this.mController = mController;
					UMImage umImage = new UMImage(MainActivity.this,
							R.drawable.ic_launcher);
					umImage.setTargetUrl("http://www.itlanbao.com");
					umImage.setTitle("微视频");
					umImage.setThumb("http://www.itlanbao.com");
					// 设置分享内容
					mController
							.setShareContent("《微视频》是一款短视频应用,里面收集了世界上各国比较有创意.新颖趣事,广告以及其他.快快来下载吧!htt://www.itlanbao.com");
					mController.setShareMedia(umImage);
				}
			});
			dialog.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private UMSocialService mController;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
			}
		}
	}

	private long exitTime = 0;
	private MenuHeaderHolder menuHeaderHolder;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** 使用SSO授权必须添加如下代码 */
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(
				requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
