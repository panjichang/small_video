package com.pan.simplepicture.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.pan.simplepicture.activity.BaseActivity;

public class IOUtils {
	public static boolean close(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * 从Assets中读取图片
	 */
	public static Bitmap getImageFromAssetsFile(BaseActivity mActivity,
			String fileName) {
		Bitmap image = null;
		AssetManager am = mActivity.getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return image;

	}
	/**
	 * 从assert中读取文件
	 * @param mActivity
	 * @return
	 */
	public static String getStringFromAssert(BaseActivity mActivity,String fileName) {
		BufferedReader reader = null;
		StringWriter writer = null;
		try {
			InputStream open = mActivity.getAssets().open(fileName);
			reader = new BufferedReader(new InputStreamReader(open));
			writer = new StringWriter();
			String line;
			while ((line = reader.readLine()) != null) {
				writer.write(line);
			}
			String json = writer.toString();
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} finally {
			release(reader, writer);
		}
	}

	/**
	 * 释放资源
	 * 
	 * @param readClose
	 * @param writeClose
	 */
	public static void release(Closeable readClose, Closeable writeClose) {
		if (readClose != null) {
			try {
				readClose.close();
			} catch (IOException e) {
				readClose = null;
				e.printStackTrace();
			}
		}
		if (writeClose != null) {
			try {
				writeClose.close();
			} catch (IOException e) {
				readClose = null;
				e.printStackTrace();
			}

		}
	}
}
