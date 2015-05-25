package com.pan.simplepicture.activity;

import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pan.simplepicture.ConstantValue;
import com.pan.simplepicture.R;
import com.pan.simplepicture.been.User;
import com.pan.simplepicture.utils.SharedPreferencesUtils;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMDataListener;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.utils.Log;
/**
 * 登录
 * @author pan
 *
 */
public class LoginActivity extends Activity implements OnClickListener {
	UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.login");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUMLogin();
		setContentView(R.layout.activity_login);
		initView();
	}

	private void initView() {
		RelativeLayout rl_qq = (RelativeLayout) findViewById(R.id.rl_qq);
		RelativeLayout rl_sina = (RelativeLayout) findViewById(R.id.rl_sina);
		rl_qq.setOnClickListener(this);
		rl_sina.setOnClickListener(this);
	}

	private void initUMLogin() {
		// TODO Auto-generated method stub
		mController.getConfig().setSsoHandler(new SinaSsoHandler());

		// 参数1为当前Activity， 参数2为开发者在QQ互联申请的APP ID，参数3为开发者在QQ互联申请的APP kEY.
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this,
				ConstantValue.QQ_APPID, ConstantValue.QQ_APPKEY);
		qqSsoHandler.addToSocialSDK();
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

	private void getSinaInfo() {
		mController.getPlatformInfo(this, SHARE_MEDIA.SINA,
				new UMDataListener() {
					@Override
					public void onStart() {
					}

					@Override
					public void onComplete(int status, Map<String, Object> info) {
						if (status == 200 && info != null) {
							ConstantValue.user = new User();
							ConstantValue.user.screen_name = (String) info
									.get("screen_name");
							ConstantValue.user.location = (String) info
									.get("location");
							ConstantValue.user.gender = info.get("gender")
									.toString().equals("1") ? "男" : "女";
							ConstantValue.user.profile_image_url = (String) info
									.get("profile_image_url");
							Gson gson = new Gson();
							SharedPreferencesUtils.saveString(
									LoginActivity.this, "user",
									gson.toJson(ConstantValue.user));
							finish();
							overridePendingTransition(R.anim.new_dync_no,
									R.anim.new_dync_out_to_left);
						} else {
							Log.d("TestData", "发生错误：" + status);
						}
					}
				});
	}

	private void loginBySina() {
		mController.doOauthVerify(this, SHARE_MEDIA.SINA, new UMAuthListener() {
			@Override
			public void onError(SocializeException e, SHARE_MEDIA platform) {
			}

			@Override
			public void onComplete(Bundle value, SHARE_MEDIA platform) {
				if (value != null && !TextUtils.isEmpty(value.getString("uid"))) {
					getSinaInfo();
				} else {
					Toast.makeText(LoginActivity.this, "授权失败",
							Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancel(SHARE_MEDIA platform) {
			}

			@Override
			public void onStart(SHARE_MEDIA platform) {
			}
		});
	}

	private void loginByQQ() {
		mController.doOauthVerify(LoginActivity.this, SHARE_MEDIA.QQ,
				new UMAuthListener() {
					@Override
					public void onStart(SHARE_MEDIA platform) {
					}

					@Override
					public void onError(SocializeException e,
							SHARE_MEDIA platform) {
						Toast.makeText(LoginActivity.this, "授权错误",
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onComplete(Bundle value, SHARE_MEDIA platform) {
						getQQInfo();
					}

					@Override
					public void onCancel(SHARE_MEDIA platform) {
						Toast.makeText(LoginActivity.this, "授权取消",
								Toast.LENGTH_SHORT).show();
					}
				});
	}

	private void getQQInfo() {
		// 获取相关授权信息
		mController.getPlatformInfo(LoginActivity.this, SHARE_MEDIA.QQ,
				new UMDataListener() {
					@Override
					public void onStart() {
					}

					@Override
					public void onComplete(int status, Map<String, Object> info) {
						if (status == 200 && info != null) {
							ConstantValue.user = new User();
							ConstantValue.user.screen_name = (String) info
									.get("screen_name");
							ConstantValue.user.location = (String) info
									.get("province")
									+ " "
									+ (String) info.get("city");
							ConstantValue.user.gender = (String) info
									.get("gender");
							ConstantValue.user.profile_image_url = (String) info
									.get("profile_image_url");
							Gson gson = new Gson();
							SharedPreferencesUtils.saveString(
									LoginActivity.this, "user",
									gson.toJson(ConstantValue.user));
							finish();
							overridePendingTransition(R.anim.new_dync_no,
									R.anim.new_dync_out_to_left);
						} else {
							Log.d("TestData", "发生错误：" + status);
						}
					}
				});
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.rl_qq:
			loginByQQ();
			break;
		case R.id.rl_sina:
			loginBySina();
			break;
		default:
			break;
		}
	}
}
