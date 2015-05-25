package com.pan.simplepicture.widget;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pan.simplepicture.ConstantValue;
import com.pan.simplepicture.R;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

public class ShareDialog extends Dialog {

	private static int default_width = 160; // 默认宽度

	private static int default_height = 120;// 默认高度

	private Context mContext;

	public ShareDialog(Context context, View layout, int style) {

		super(context, style);

		mContext = context;
	}

	public ShareDialog(Context context, int width, int height) {
		super(context, R.style.dialog);
		view = View.inflate(context, R.layout.share_dialog, null);
		setContentView(view);
		mContext = context;
		initSocialSDK();
		initPlatformMap();
		Window window = getWindow();

		WindowManager.LayoutParams params = window.getAttributes();
		params.height = height;
		params.width = width;
		params.gravity = Gravity.CENTER;
		window.setAttributes(params);
	}

	@Override
	public void show() {
		startShare();
		super.show();
	};

	private void startShare() {
		// TODO Auto-generated method stub
		if (mOnShareDataListener != null) {
			mOnShareDataListener.onShareData(mController);
		}
		showCustomUI(false);
	}

	Map<String, SHARE_MEDIA> mPlatformsMap = new HashMap<String, SHARE_MEDIA>();
	private final UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");

	/**
	 * 初始化SDK，添加一些平台
	 */
	private void initSocialSDK() {
		// 添加QQ平台
		UMQQSsoHandler qqHandler = new UMQQSsoHandler((Activity) mContext,
				ConstantValue.QQ_APPID, ConstantValue.QQ_APPKEY);
		qqHandler.addToSocialSDK();

		// 添加QQ空间平台
		QZoneSsoHandler qzoneHandler = new QZoneSsoHandler((Activity) mContext,
				ConstantValue.QQ_APPID, ConstantValue.QQ_APPKEY);
		qzoneHandler.addToSocialSDK();

		// wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
		String appID = ConstantValue.WEIXIN_APPID;
		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(mContext, appID);
		wxHandler.addToSocialSDK();

		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(mContext, appID);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();

		// 设置新浪SSO handler
		mController.getConfig().setSsoHandler(new SinaSsoHandler());

		// 设置腾讯微博SSO handler
		mController.getConfig().setSsoHandler(new TencentWBSsoHandler());

	}

	private int[] imgs = { R.drawable.wechat_selector,
			R.drawable.wechatmoment_selector, R.drawable.qq_selector,
			R.drawable.qzone_selector, R.drawable.sina_selector,
			R.drawable.tencent_selector };

	/**
	 * 初始化平台map
	 */
	private void initPlatformMap() {
		mPlatformsMap.put("微信好友", SHARE_MEDIA.WEIXIN);
		mPlatformsMap.put("朋友圈", SHARE_MEDIA.WEIXIN_CIRCLE);
		mPlatformsMap.put("QQ好友", SHARE_MEDIA.QQ);
		mPlatformsMap.put("QQ空间", SHARE_MEDIA.QZONE);
		mPlatformsMap.put("新浪微博", SHARE_MEDIA.SINA);
		mPlatformsMap.put("腾讯微博", SHARE_MEDIA.TENCENT);
	}

	/**
	 * 分享监听器
	 */
	SnsPostListener mShareListener = new SnsPostListener() {

		@Override
		public void onStart() {
			Toast.makeText(mContext, "开始分享.", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onComplete(SHARE_MEDIA platform, int stCode,
				SocializeEntity entity) {
			if (stCode == 200) {
				Toast.makeText(mContext, "分享成功", Toast.LENGTH_SHORT).show();
			} else {
				String eMsg = "";
				if (stCode == -101) {
					eMsg = "没有授权";
				}

			}
		}
	};
	String[] strings;

	private View view;

	private void showCustomUI(final boolean isDirectShare) {
		ImageView lightordark = (ImageView) view.findViewById(R.id.lightordark);
		int hour = new Date().getHours();
		if (hour >= 6 && hour < 18) {
			lightordark.setImageResource(R.drawable.ic_light);
		} else {
			lightordark.setImageResource(R.drawable.ic_dark);
		}
		GridView gv_share = (GridView) view.findViewById(R.id.gv_share);
		Button comment_cancle_button = (Button) view
				.findViewById(R.id.comment_cancle_button);
		strings = mContext.getResources().getStringArray(R.array.share);
		gv_share.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SHARE_MEDIA platform = mPlatformsMap.get(strings[position]);
				dismiss();
				if (isDirectShare) {
					// 调用直接分享
					mController.directShare(mContext, platform, mShareListener);
				} else {
					// 调用直接分享, 但是在分享前用户可以编辑要分享的内容
					mController.postShare(mContext, platform, mShareListener);
				}
			}
		});
		MyAdapter mAdapter = new MyAdapter();
		gv_share.setAdapter(mAdapter);
		comment_cancle_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return imgs.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView != null) {
				holder = (ViewHolder) convertView.getTag();
			} else {
				holder = new ViewHolder();
				convertView = View.inflate(mContext, R.layout.share_item, null);
				holder.iv_icon = (ImageView) convertView
						.findViewById(R.id.iv_icon);
				holder.tv_name = (TextView) convertView
						.findViewById(R.id.tv_name);
				convertView.setTag(holder);
			}
			holder.iv_icon.setImageResource(imgs[position]);
			holder.tv_name.setText(strings[position]);
			return convertView;
		}

	}

	public interface OnShareDataListener {
		public void onShareData(UMSocialService mController);
	}

	private OnShareDataListener mOnShareDataListener;

	public void setOnShareDataListener(OnShareDataListener mOnShareDataListener) {
		this.mOnShareDataListener = mOnShareDataListener;
	}

	class ViewHolder {
		ImageView iv_icon;
		TextView tv_name;
	}

}
