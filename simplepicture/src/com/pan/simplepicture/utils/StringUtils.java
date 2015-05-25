package com.pan.simplepicture.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
	public static String timeFormatter(String mm) {
		float f = Float.parseFloat(mm);
		DecimalFormat df = new DecimalFormat("00.00");
		return df.format(f / 60);
	}

	/**
	 * yyyyMMddHHmmss
	 * 
	 * @param time
	 * @return
	 */
	public static String dayFormatter(String time) {
		long xTime = new Date().getTime() - Long.parseLong(time);
		// 秒
		if ((xTime = xTime / 1000) < 60) {
			return "刚刚";
		}
		// 分
		if ((xTime = xTime / 60) < 60) {
			return xTime + "分钟前";
		}
		// 小时
		if ((xTime = xTime / 60) < 24) {
			return xTime + "小时前";
		}
		// 天
		if ((xTime = xTime / 24) < 7) {
			return xTime + "天前";
		}
		if (xTime < 30 && xTime >= 7) {
			return xTime / 7 + "周前";
		}
		// 月
		if (xTime >= 30 && xTime < 365) {
			return xTime / 30 + "月前";
		}
		if (xTime >= 365) {
			return xTime / 365 + "年前";
		}
		return "";
	}

	public static String formatFileSize(long len) {
		return formatFileSize(len, false);
	}

	public static String formatFileSize(long len, boolean keepZero) {
		String size;
		DecimalFormat formatKeepTwoZero = new DecimalFormat("#.00");
		DecimalFormat formatKeepOneZero = new DecimalFormat("#.0");
		if (len < 1024) {
			size = String.valueOf(len + "B");
		} else if (len < 10 * 1024) {
			// [0, 10KB)，保留两位小数
			size = String.valueOf(len * 100 / 1024 / (float) 100) + "KB";
		} else if (len < 100 * 1024) {
			// [10KB, 100KB)，保留一位小数
			size = String.valueOf(len * 10 / 1024 / (float) 10) + "KB";
		} else if (len < 1024 * 1024) {
			// [100KB, 1MB)，个位四舍五入
			size = String.valueOf(len / 1024) + "KB";
		} else if (len < 10 * 1024 * 1024) {
			// [1MB, 10MB)，保留两位小数
			if (keepZero) {
				size = String.valueOf(formatKeepTwoZero.format(len * 100 / 1024
						/ 1024 / (float) 100))
						+ "MB";
			} else {
				size = String.valueOf(len * 100 / 1024 / 1024 / (float) 100)
						+ "MB";
			}
		} else if (len < 100 * 1024 * 1024) {
			// [10MB, 100MB)，保留一位小数
			if (keepZero) {
				size = String.valueOf(formatKeepOneZero.format(len * 10 / 1024
						/ 1024 / (float) 10))
						+ "MB";
			} else {
				size = String.valueOf(len * 10 / 1024 / 1024 / (float) 10)
						+ "MB";
			}
		} else if (len < 1024 * 1024 * 1024) {
			// [100MB, 1GB)，个位四舍五入
			size = String.valueOf(len / 1024 / 1024) + "MB";
		} else {
			// [1GB, ...)，保留两位小数
			size = String.valueOf(len * 100 / 1024 / 1024 / 1024 / (float) 100)
					+ "GB";
		}
		return size;
	}
}
