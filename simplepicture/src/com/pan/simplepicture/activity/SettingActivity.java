package com.pan.simplepicture.activity;

import in.srain.cube.image.ImageProvider;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.pan.simplepicture.ConstantValue;
import com.pan.simplepicture.R;
import com.pan.simplepicture.utils.AppUtils;
import com.pan.simplepicture.utils.SharedPreferencesUtils;
import com.pan.simplepicture.utils.StringUtils;
import com.pan.simplepicture.widget.AlertDialog;
import com.pan.simplepicture.widget.ShareDialog;
import com.pan.simplepicture.widget.ShareDialog.OnShareDataListener;
import com.umeng.fb.FeedbackAgent;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.UMSsoHandler;
/**
 * 设置
 * @author pan
 *
 */
public class SettingActivity extends BaseActivity implements OnClickListener,
		OnCheckedChangeListener {
	private String title;

	@Override
	protected void initView() {
		setContentView(R.layout.activity_setting);
		agent = new FeedbackAgent(this);
		agent.sync();
		ToggleButton prompt__not_wifi = (ToggleButton) findViewById(R.id.prompt__not_wifi);
		TextView clear_cache = (TextView) findViewById(R.id.clear_cache);
		TextView feedback = (TextView) findViewById(R.id.feedback);
		TextView remmend_firend = (TextView) findViewById(R.id.remmend_firend);
		TextView assess = (TextView) findViewById(R.id.assess);
		logout = (Button) findViewById(R.id.logout);
		ToggleButton auto_play = (ToggleButton) findViewById(R.id.auto_play);
		boolean auto = SharedPreferencesUtils.getBoolean(this, "auto_play",
				false);
		auto_play.setChecked(auto);
		boolean wifi = SharedPreferencesUtils.getBoolean(this,
				"prompt__not_wifi", true);
		prompt__not_wifi.setChecked(wifi);

		if (ConstantValue.user != null) {
			logout.setText("注销");
		} else {
			logout.setText("登录");
		}
		auto_play.setOnCheckedChangeListener(this);
		prompt__not_wifi.setOnCheckedChangeListener(this);
		clear_cache.setOnClickListener(this);
		feedback.setOnClickListener(this);
		remmend_firend.setOnClickListener(this);
		assess.setOnClickListener(this);
		logout.setOnClickListener(this);
	}

	@Override
	protected void init() {
		title = getIntent().getStringExtra("title");
	}

	@Override
	protected void setActionBarTitle() {
		mActionBar.setTitle(title);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.clear_cache:
			// 清除缓存
			new AlertDialog(SettingActivity.this).builder().setTitle("清除缓存")
					.setMsg("清除缓存可能会使你现有已经登录天数记录归零 ，确定清除？")
					.setPositiveButton("确认", new OnClickListener() {
						@Override
						public void onClick(View v) {
							ImageProvider imageProvider = imageLoader
									.getImageProvider();
							long usedMemory = imageProvider
									.getFileCacheUsedSpace();
							imageProvider.clearDiskCache();
							Toast.makeText(
									SettingActivity.this,
									"恭喜释放了"
											+ StringUtils
													.formatFileSize(usedMemory)
											+ "空间!", 0).show();
						}
					}).setNegativeButton("取消", new OnClickListener() {
						@Override
						public void onClick(View v) {

						}
					}).show();
			break;
		case R.id.feedback:
			agent.startFeedbackActivity();
			int4Right();
			// 反馈意见
			break;
		case R.id.remmend_firend:
			// 推荐好友
			ShareDialog dialog = new ShareDialog(this, getWindowManager()
					.getDefaultDisplay().getWidth() - 80, this.dip2px(400));
			dialog.setOnShareDataListener(new OnShareDataListener() {

				@Override
				public void onShareData(UMSocialService mController) {
					SettingActivity.this.mController = mController;
					UMImage umImage = new UMImage(SettingActivity.this,
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
			break;
		case R.id.assess:
			// 赏个好评
			Toast.makeText(this, "赏个好评", 0).show();
			AppUtils.goMarket(SettingActivity.this);
			break;
		case R.id.logout:
			if (ConstantValue.user != null) {
				// 退出登录
				new AlertDialog(SettingActivity.this).builder()
						.setTitle("退出当前账号").setMsg("退出当前帐号可能会导致一些功能无法使用，确定退出？")
						.setPositiveButton("确认退出", new OnClickListener() {
							@Override
							public void onClick(View v) {
								ConstantValue.user = null;
								SharedPreferencesUtils.saveString(
										SettingActivity.this, "user", "");
								logout.setText("登录");
							}
						}).setNegativeButton("取消", new OnClickListener() {
							@Override
							public void onClick(View v) {

							}
						}).show();
			} else {
				// 登录
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
				finish();
				int4Right();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		SharedPreferencesUtils.setBoolean(this, (String) arg0.getTag(), arg1);
	}

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

	private UMSocialService mController;
	private FeedbackAgent agent;
	private Button logout;
}
