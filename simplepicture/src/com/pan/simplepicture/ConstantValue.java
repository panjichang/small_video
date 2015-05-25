package com.pan.simplepicture;

import android.os.Build;

import com.pan.simplepicture.been.User;

public abstract class ConstantValue {
	public static String str = "35" + Build.BOARD.length() % 10
			+ Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10
			+ Build.DEVICE.length() % 10 + Build.DISPLAY.length() % 10
			+ Build.HOST.length() % 10 + Build.ID.length() % 10
			+ Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10
			+ Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10
			+ Build.TYPE.length() % 10 + Build.USER.length() % 10;

	public static final int INITIAL_DELAY_MILLIS = 1000;

	/**
	 * 下拉刷新文字
	 */
	public static final String PULL_STRING = "VSTAR";

	/**
	 * QQ授权 appid
	 */
	public static final String QQ_APPID = "1104167669";
	/**
	 * QQ授权 appkey
	 */
	public static final String QQ_APPKEY = "2NmgvDSPVEiIHkPD";

	/**
	 * 微信平台 appid
	 */
	public static final String WEIXIN_APPID = "wx03767b047a99b246";
	
	/**
	 * 有米 appid
	 */
	public static final String YOUMI_APPID="3c22ba1162fa846f";
	/**
	 * 有米 appid
	 */
	public static final String YOUMI_APPKEY="64480e6680b66c92";
	/**
	 * AVOS appid
	 */
	public static final String AVOS_APPID="mlzmkqw1vlpe833ro2m4h5oo3p5isc0i27vtmso7m4fqd2vu";
	/**
	 * AVOS appid
	 */
	public static final String AVOS_APPKEY="xtycr8ds169x5pywzd70egw7ph3xbrxmsvo4bzkzk3dtalo1";
	/**
	 * 用户登录
	 */
	public static User user; 
}
