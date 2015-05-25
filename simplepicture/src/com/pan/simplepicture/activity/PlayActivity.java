package com.pan.simplepicture.activity;

import in.srain.cube.image.CubeImageView;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.image.impl.DefaultImageLoadHandler;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.baidu.cyberplayer.core.BVideoView;
import com.baidu.cyberplayer.core.BVideoView.OnCompletionListener;
import com.baidu.cyberplayer.core.BVideoView.OnErrorListener;
import com.baidu.cyberplayer.core.BVideoView.OnInfoListener;
import com.baidu.cyberplayer.core.BVideoView.OnPlayingBufferCacheListener;
import com.baidu.cyberplayer.core.BVideoView.OnPreparedListener;
import com.pan.simplepicture.ConstantValue;
import com.pan.simplepicture.R;
import com.pan.simplepicture.been.Comment;
import com.pan.simplepicture.been.ShortVideo;
import com.pan.simplepicture.been.User;
import com.pan.simplepicture.http.protocol.PlayAddressProtocol;
import com.pan.simplepicture.utils.AppUtils;
import com.pan.simplepicture.utils.MapUtil;
import com.pan.simplepicture.utils.SharedPreferencesUtils;
import com.pan.simplepicture.widget.AlertDialog;
import com.pan.simplepicture.widget.CommentLinearLayout;
import com.pan.simplepicture.widget.MeterailEditText;
import com.pan.simplepicture.widget.ShareDialog;
import com.pan.simplepicture.widget.ShareDialog.OnShareDataListener;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.sso.UMSsoHandler;
/**
 * 播放页
 * @author pan
 *
 */
public class PlayActivity extends BaseActivity implements OnPreparedListener,
		OnCompletionListener, OnErrorListener, OnInfoListener,
		OnPlayingBufferCacheListener {
	private final String TAG = "VideoViewPlayingActivity";
	private ShortVideo shortVideo;
	private ArrayList<Comment> list = new ArrayList<Comment>();;
	private boolean cache = true;
	private final int LIMIT = 5;

	@Override
	protected void init() {
		shortVideo = (ShortVideo) getIntent()
				.getSerializableExtra("shortVideo");
	}

	@Override
	protected void initView() {
		setContentView(R.layout.activity_play);
		loader = ImageLoaderFactory.create(this);
		DefaultImageLoadHandler defaultImageHandler = new DefaultImageLoadHandler(
				this);
		defaultImageHandler.setImageFadeIn(true);
		defaultImageHandler.setImageRounded(true, dip2px(45));
		loader.setImageLoadHandler(defaultImageHandler);
		RelativeLayout play_sreen = (RelativeLayout) findViewById(R.id.play_sreen);
		user_comment = (LinearLayout) findViewById(R.id.user_comment);
		View ll_play = inflate(R.layout.play_item);
		initPlay(ll_play);
		TextView title = (TextView) ll_play.findViewById(R.id.title);
		CubeImageView iv_default = (CubeImageView) ll_play
				.findViewById(R.id.detailPic);
		iv_default.loadImage(this.imageLoader, shortVideo.thumbnailV2);
		title.setText(shortVideo.title);
		TextView play_count = (TextView) ll_play.findViewById(R.id.play_count);
		TextView des = (TextView) ll_play.findViewById(R.id.des);
		des.setText(shortVideo.description);
		play_sreen.addView(ll_play);
		View edit = inflate(R.layout.edit_item);
		final MeterailEditText et_content = (MeterailEditText) edit
				.findViewById(R.id.et_content);
		ImageView iv_send = (ImageView) edit.findViewById(R.id.iv_send);
		comment_footer = (TextView) inflate(R.layout.comment_footer);
		user_comment.addView(edit);
		user_comment.addView(comment_footer);
		auto_Play = SharedPreferencesUtils.getBoolean(this, "auto_play", false);
		if (TextUtils.isEmpty(shortVideo.author)) {
			play_count.setText("标签 : " + shortVideo.tag);
			new PlayAddressAsyncTask().execute();
		} else {
			play_count.setText("作者 : " + shortVideo.author);
			mVideoSource = shortVideo.player;
			sendPlayMessage(auto_Play);
		}
		iv_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String ed_String = et_content.getText().toString().trim();
				if (TextUtils.isEmpty(ed_String)) {
					Toast.makeText(PlayActivity.this, "请输入评论内容", 0).show();
					return;
				}
				if (ConstantValue.user == null) {
					Intent intent = new Intent(PlayActivity.this,
							LoginActivity.class);
					startActivity(intent);
					int4Right();
					return;
				}
				final Comment comment = new Comment();
				comment.content = ed_String;
				comment.published = String.valueOf(System.currentTimeMillis());
				comment.user = ConstantValue.user;
				MapUtil.saveComment(comment, shortVideo.rsId,
						new SaveCallback() {
							@Override
							public void done(AVException arg0) {
								if (arg0 == null) {
									list.add(0, comment);
									showToast(true, "发表成功");
									et_content.setText("");
									CommentLinearLayout layout = new CommentLinearLayout(
											PlayActivity.this, comment, loader);
									user_comment.addView(layout, 1);
								} else {
									showToast(false, "发表失败");
								}
							}
						});
			}
		});
		comment_footer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new CommentAsyncTask().execute();
			}
		});
		new CommentAsyncTask().execute();
	}

	/**
	 * 获取评论
	 */
	private class CommentAsyncTask extends
			AsyncTask<Void, Void, List<AVObject>> {

		@Override
		protected List<AVObject> doInBackground(Void... arg0) {
			return MapUtil.getComments(shortVideo.rsId, LIMIT, list.size());
		}

		// 2015-02-02T04:58:28.046Z
		@Override
		protected void onPostExecute(List<AVObject> result) {
			if (result != null && result.size() > 0) {
				if (result.size() < LIMIT) {
					comment_footer.setText("没有更多评论了");
					comment_footer.setEnabled(false);
				}
				user_comment.removeViewAt(user_comment.getChildCount() - 1);
				for (AVObject aVObject : result) {
					Comment comment = new Comment();
					comment.content = aVObject.getString("content");
					comment.published = aVObject.getString("published");
					comment.user = new User(aVObject.getString("location"),
							aVObject.getString("gender"),
							aVObject.getString("screen_name"),
							aVObject.getString("profile_image_url"));
					CommentLinearLayout layout = new CommentLinearLayout(
							PlayActivity.this, comment, loader);
					list.add(comment);
					user_comment.addView(layout);
				}
				user_comment.addView(comment_footer);
			} else {
				comment_footer.setText("暂无任何评论");
				comment_footer.setEnabled(false);
			}
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}
	}

	/***************************************** 百度视频sdk ************************************************************/

	/**
	 * 您的ak
	 */
	private String AK = "t207G26112s0cbhYjtDuxBP7";
	/**
	 * //您的sk的前16位
	 */
	private String SK = "9s4Eof8Os1zvEht4ybem6U64Sw0R3Xtc";

	private String mVideoSource = null;

	private ImageButton mPlaybtn = null;

	private ImageButton pre_play_button = null;

	private RelativeLayout mController = null;

	private SeekBar mProgress = null;
	private TextView mDuration = null;
	private TextView mCurrPostion = null;

	/**
	 * 记录播放位置
	 */
	private int mLastPos = 0;

	/**
	 * 播放状态
	 */
	private enum PLAYER_STATUS {
		PLAYER_IDLE, PLAYER_PREPARING, PLAYER_PREPARED,
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_play, menu);
		MenuItem item = menu.findItem(R.id.action_important);
		String ids = SharedPreferencesUtils.getCollectionId(this);
		if (ids.contains(shortVideo.rsId)) {
			item.setIcon(R.drawable.ic_action_important);
		} else {
			item.setIcon(R.drawable.ic_action_not_important);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_share:
			shareVideo();
			break;
		case R.id.action_important:
			clickCollection(item);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean isSharePause = false;

	private void shareVideo() {
		if (mVV.isPlaying()) {
			videoPause();
			isSharePause = true;
		}
		ShareDialog dialog = new ShareDialog(this, getWindowManager()
				.getDefaultDisplay().getWidth() - 80, this.dip2px(400));
		dialog.setOnShareDataListener(new OnShareDataListener() {

			@Override
			public void onShareData(UMSocialService mController) {
				uMSocialService = mController;

				mController.setShareContent(shortVideo.description
						+ ",更多精彩创意视频,尽在<微视频>,应用下载地址:http://www.baidu.com");
				// 设置分享视频
				UMVideo umVideo = new UMVideo(shortVideo.link);
				umVideo.setMediaUrl(shortVideo.link);
				umVideo.setTargetUrl("http://www.baidu.com");
				// 设置视频缩略图
				umVideo.setThumb(shortVideo.thumbnailV2);
				umVideo.setTitle(shortVideo.title);
				mController.setShareMedia(umVideo);
			}
		});
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				if (isSharePause && !mVV.isPlaying()) {
					isSharePause = false;
					videoResume();
				}
			}
		});
		dialog.show();
	}

	private void clickCollection(MenuItem item) {
		String ids = SharedPreferencesUtils.getCollectionId(this);
		if (ids.contains(shortVideo.rsId)) {
			// 取消
			SharedPreferencesUtils.cancelCollection(this, shortVideo);
			item.setIcon(R.drawable.ic_action_not_important);
			Toast.makeText(this, "取消收藏", 0).show();
		} else {
			// 收藏
			SharedPreferencesUtils.saveCollection(this, shortVideo);
			item.setIcon(R.drawable.ic_action_important);
			Toast.makeText(this, "收藏成功", 0).show();
		}
	}

	private PLAYER_STATUS mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;

	private BVideoView mVV = null;

	private EventHandler mEventHandler;
	private HandlerThread mHandlerThread;

	private final Object SYNC_Playing = new Object();

	private WakeLock mWakeLock = null;
	private static final String POWER_LOCK = "VideoViewPlayingActivity";
	private static final int UI_CONTROLLER_INVISIBLE = 0;

	private final int EVENT_PLAY = 0;
	private final int UI_EVENT_UPDATE_CURRPOSITION = 1;

	class EventHandler extends Handler {
		public EventHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_PLAY:
				/**
				 * 如果已经播放了，等待上一次播放结束
				 */
				if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
					synchronized (SYNC_Playing) {
						try {
							SYNC_Playing.wait();
							Log.v(TAG, "wait player status to idle");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				/**
				 * 设置播放url
				 */
				mVV.setVideoPath(mVideoSource);

				/**
				 * 续播，如果需要如此
				 */
				if (mLastPos > 0) {

					mVV.seekTo(mLastPos);
					mLastPos = 0;
				}

				/**
				 * 显示或者隐藏缓冲提示
				 */
				mVV.showCacheInfo(true);

				/**
				 * 开始播放
				 */
				mVV.start();
				mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARING;
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onHandleMessage(Message msg) {
		switch (msg.what) {
		/**
		 * 更新进度及时间
		 */
		case UI_EVENT_UPDATE_CURRPOSITION:
			if (mVV.isPlaying()) {
				int currPosition = mVV.getCurrentPosition();
				int duration = mVV.getDuration();
				updateTextViewWithTimeFormat(mCurrPostion, currPosition);
				updateTextViewWithTimeFormat(mDuration, duration);
				mProgress.setMax(duration);
				mProgress.setProgress(currPosition);
			}
			mHandler.sendEmptyMessageDelayed(UI_EVENT_UPDATE_CURRPOSITION, 500);
			break;
		/**
		 * 隐藏控制UI
		 */
		case UI_CONTROLLER_INVISIBLE:
			if (mVV.isPlaying()) {
				updateControlBar(false);
			}
			break;
		default:
			break;
		}
	}

	protected void initPlay(View view) {

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE, POWER_LOCK);

		initUI(view);

		/**
		 * 开启后台事件处理线程
		 */
		mHandlerThread = new HandlerThread("event handler thread",
				Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();
		mEventHandler = new EventHandler(mHandlerThread.getLooper());
	}

	private class PlayAddressAsyncTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			map.put("rsId", shortVideo.rsId);
			map.put("link", shortVideo.link);
			PlayAddressProtocol protocol = new PlayAddressProtocol();
			return protocol.loadInfo(PlayActivity.this, map, cache);
		}

		@Override
		protected void onPostExecute(String result) {
			if (TextUtils.isEmpty(result)) {
				Toast.makeText(PlayActivity.this, "亲,服务器被大家蹂躏了哦!", 0).show();
				return;
			}
			mVideoSource = result;
			sendPlayMessage(auto_Play);
			super.onPostExecute(result);
		}
	}

	private void sendPlayMessage(boolean auto_Play) {
		if (!auto_Play) {
			return;
		}
		int flag = AppUtils.isWifi(this);
		boolean b = SharedPreferencesUtils.getBoolean(this, "prompt__not_wifi",
				true);
		if (flag == 2 && b) {
			new AlertDialog(this).builder().setTitle("温馨提示")
					.setMsg("当前播放使用的是非wifi环境(如果不想再提示,可以进入设置页面设置)，确定继续？")
					.setPositiveButton("继续", new OnClickListener() {
						@Override
						public void onClick(View v) {
							play();
						}
					}).setNegativeButton("取消", new OnClickListener() {
						@Override
						public void onClick(View v) {
						}
					}).show();
			return;
		}
		play();
	}

	private void play() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				pre_play_button.setVisibility(View.GONE);
				VideoAction.setVisibility(View.GONE);
				mPlaybtn.setBackgroundResource(R.drawable.pause_btn_style);
				shimmer.cancel();
			}
		});
		mEventHandler.sendEmptyMessage(EVENT_PLAY);
	}

	// 我的收藏,缓存,意见反馈,检测新版本,评论
	private ShimmerTextView shimmer_tv;
	private RelativeLayout VideoAction, rl_container;
	private Button zoom_btn;
	private boolean isFullScreen = false;

	/**
	 * 初始化界面
	 */
	private void initUI(View view) {
		mPlaybtn = (ImageButton) view.findViewById(R.id.play_btn);
		zoom_btn = (Button) view.findViewById(R.id.zoom_btn);
		rl_container = (RelativeLayout) view.findViewById(R.id.rl_container);
		pre_play_button = (ImageButton) view.findViewById(R.id.pre_play_button);
		mController = (RelativeLayout) view.findViewById(R.id.controlbar);
		shimmer_tv = (ShimmerTextView) view.findViewById(R.id.shimmer_tv);
		VideoAction = (RelativeLayout) view.findViewById(R.id.VideoAction);
		shimmer = new Shimmer();
		shimmer.start(shimmer_tv);
		mProgress = (SeekBar) view.findViewById(R.id.media_progress);
		mDuration = (TextView) view.findViewById(R.id.time_total);
		mCurrPostion = (TextView) view.findViewById(R.id.time_current);

		registerCallbackForControl();

		/**
		 * 设置ak及sk的前16位
		 */
		BVideoView.setAKSK(AK, SK);

		/**
		 * 获取BVideoView对象
		 */
		mVV = (BVideoView) view.findViewById(R.id.video_view);
		mVV.requestFocus();
		/**
		 * 注册listener
		 */
		mVV.setOnPreparedListener(this);
		mVV.setOnCompletionListener(this);
		mVV.setOnErrorListener(this);
		mVV.setOnInfoListener(this);
		mVV.setVideoScalingMode(1);
		/**
		 * 设置解码模式
		 */
		mVV.setDecodeMode(BVideoView.DECODE_SW);
	}

	private class PlayOnclickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (mVV.isPlaying()) {
				mPlaybtn.setBackgroundResource(R.drawable.play_btn_style);
				/**
				 * 暂停播放
				 */
				videoPause();
			} else {
				mPlaybtn.setBackgroundResource(R.drawable.pause_btn_style);
				/**
				 * 继续播放
				 */
				videoResume();
			}
		}
	}

	private void changeScreen() {
		if (isFullScreen) {
			setMinScreen();
		} else {
			setFullScreen();
		}
	}

	// 切换为全屏
	private void setFullScreen() {
		if (Build.VERSION.SDK_INT >= 9) {
			setRequestedOrientation(6);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		getWindow().addFlags(1024);
		mActionBar.hide();
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				getWindowManager().getDefaultDisplay().getWidth(),
				getWindowManager().getDefaultDisplay().getHeight());
		mVV.setLayoutParams(layoutParams);
		mController.setLayoutParams(layoutParams);
		VideoAction.setLayoutParams(layoutParams);
		zoom_btn.setBackgroundResource(R.drawable.screensize_zoomin_button);
		rl_container.setFocusable(true);
		rl_container.setFocusableInTouchMode(true);
		rl_container.requestFocus();
		isFullScreen = true;
	}

	// 切换为小屏幕
	private void setMinScreen() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().clearFlags(1024);
		mActionBar.show();
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				getWindowManager().getDefaultDisplay().getWidth(),
				getDimen(R.dimen.video_play_heigh));
		mVV.setLayoutParams(layoutParams);
		mController.setLayoutParams(layoutParams);
		VideoAction.setLayoutParams(layoutParams);
		zoom_btn.setBackgroundResource(R.drawable.screensize_zoomout_button);
		isFullScreen = false;
	}

	/**
	 * 为控件注册回调处理函数
	 */
	private void registerCallbackForControl() {
		PlayOnclickListener playListener = new PlayOnclickListener();
		rl_container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				updateControlBar(!barShow);
			}
		});
		mPlaybtn.setOnClickListener(playListener);
		zoom_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				changeScreen();
			}
		});
		pre_play_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sendPlayMessage(true);
			}
		});

		OnSeekBarChangeListener osbc1 = new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				// Log.v(TAG, "progress: " + progress);
				updateTextViewWithTimeFormat(mCurrPostion, progress);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				/**
				 * SeekBar开始seek时停止更新
				 */
				mHandler.removeMessages(UI_EVENT_UPDATE_CURRPOSITION);
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				int iseekPos = seekBar.getProgress();
				/**
				 * SeekBark完成seek时执行seekTo操作并更新界面
				 * 
				 */
				mVV.seekTo(iseekPos);
				Log.v(TAG, "seek to " + iseekPos);
				mHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
			}
		};
		mProgress.setOnSeekBarChangeListener(osbc1);
	}

	private void updateTextViewWithTimeFormat(TextView view, int second) {
		int hh = second / 3600;
		int mm = second % 3600 / 60;
		int ss = second % 60;
		String strTemp = null;
		if (0 != hh) {
			strTemp = String.format("%02d:%02d:%02d", hh, mm, ss);
		} else {
			strTemp = String.format("%02d:%02d", mm, ss);
		}
		view.setText(strTemp);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		/**
		 * 在停止播放前 你可以先记录当前播放的位置,以便以后可以续播
		 */
		if (mVV.isPlaying()) {
			mLastPos = mVV.getCurrentPosition();
			mVV.stopPlayback();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.v(TAG, "onResume");
		if (null != mWakeLock && (!mWakeLock.isHeld())) {
			mWakeLock.acquire();
		}
		/**
		 * 发起一次播放任务,当然您不一定要在这发起
		 */
		if (!mVV.isPlaying() && mLastPos != 0) {
			sendPlayMessage(true);
		}
	}

	// private long mTouchTime;
	private boolean barShow = true;
	private Shimmer shimmer;
	private boolean auto_Play = false;

	/*
	 * @Override public boolean onTouchEvent(MotionEvent event) {
	 * 
	 * // TODO Auto-generated method stub if (event.getAction() ==
	 * MotionEvent.ACTION_DOWN) mTouchTime = System.currentTimeMillis(); else if
	 * (event.getAction() == MotionEvent.ACTION_UP) { long time =
	 * System.currentTimeMillis() - mTouchTime; if (time < 400) {
	 * updateControlBar(!barShow); } }
	 * 
	 * return false; }
	 */

	public void updateControlBar(boolean show) {

		if (show) {
			mController.setVisibility(View.VISIBLE);
			mHandler.removeMessages(UI_CONTROLLER_INVISIBLE);
			mHandler.sendEmptyMessageDelayed(UI_CONTROLLER_INVISIBLE, 3000);
		} else {
			mController.setVisibility(View.INVISIBLE);
		}
		barShow = show;
	}

	private void videoPause() {
		if (mVV != null) {
			mLastPos = mVV.getCurrentPosition();
			mVV.pause();
		}
	}

	private void videoResume() {
		if (mVV != null) {
			mLastPos = 0;
			mVV.resume();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		/**
		 * 退出后台事件处理线程
		 */
		mHandlerThread.quit();
	}

	@Override
	public boolean onInfo(int what, int extra) {
		// TODO Auto-generated method stub
		switch (what) {
		/**
		 * 开始缓冲
		 */
		case BVideoView.MEDIA_INFO_BUFFERING_START:
			break;
		/**
		 * 结束缓冲
		 */
		case BVideoView.MEDIA_INFO_BUFFERING_END:
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * 当前缓冲的百分比， 可以配合onInfo中的开始缓冲和结束缓冲来显示百分比到界面
	 */
	@Override
	public void onPlayingBufferCache(int percent) {
		// TODO Auto-generated method stub

	}

	/**
	 * 播放出错
	 */
	@Override
	public boolean onError(int what, int extra) {
		// TODO Auto-generated method stub
		Log.v(TAG, "onError");
		synchronized (SYNC_Playing) {
			SYNC_Playing.notify();
		}
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(PlayActivity.this, "服务器被大家蹂躏了哦...", 0).show();
			}
		});
		releaseTask();
		return true;
	}

	/**
	 * 播放完成
	 */
	@Override
	public void onCompletion() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onCompletion");
		synchronized (SYNC_Playing) {
			SYNC_Playing.notify();
		}
		releaseTask();
	}

	private void releaseTask() {
		mVV.stopPlayback();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				VideoAction.setVisibility(View.VISIBLE);
				pre_play_button.setVisibility(View.VISIBLE);
				shimmer.start(shimmer_tv);
			}
		}, 1000);
		mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
		mHandler.removeMessages(UI_EVENT_UPDATE_CURRPOSITION);
	}

	/**
	 * 准备播放就绪
	 */
	@Override
	public void onPrepared() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onPrepared");
		mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARED;
		mHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** 使用SSO授权必须添加如下代码 */
		UMSsoHandler ssoHandler = uMSocialService.getConfig().getSsoHandler(
				requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	@Override
	public void onBackPressed() {
		if (isFullScreen) {
			setMinScreen();
		} else {
			super.onBackPressed();
		}
	}

	private UMSocialService uMSocialService;
	private LinearLayout user_comment;
	private TextView comment_footer;
	private ImageLoader loader;
}
