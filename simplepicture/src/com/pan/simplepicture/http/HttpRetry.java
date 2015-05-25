package com.pan.simplepicture.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.os.SystemClock;

public class HttpRetry implements HttpRequestRetryHandler {
	// 重试休息的时间
	private static final int RETRY_SLEEP_TIME_MILLIS = 1000;
	// 网络异常，继续
	private static HashSet<Class<?>> exceptionWhitelist = new HashSet<Class<?>>();
	// 用户异常，不继续（如，用户中断线程）
	private static HashSet<Class<?>> exceptionBlacklist = new HashSet<Class<?>>();

	static {
		// 以下异常不需要重试，这样异常都是用于造成或者是一些重试也无效的异常
		exceptionWhitelist.add(NoHttpResponseException.class);// 连上了服务器但是没有Response
		exceptionWhitelist.add(UnknownHostException.class);// host出了问题，一般是由于网络故障
		exceptionWhitelist.add(SocketException.class);// Socket问题，一般是由于网络故障
		// 以下异常可以重试
		exceptionBlacklist.add(InterruptedIOException.class);// 连接中断，一般是由于连接超时引起
		exceptionBlacklist.add(SSLHandshakeException.class);// SSL握手失败
	}

	private final int maxRetries;

	public HttpRetry(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		boolean retry = true;
		// 请求是否到达
		Boolean b = (Boolean) context.getAttribute(ExecutionContext.HTTP_REQ_SENT);
		boolean sent = (b != null && b.booleanValue());

		if (executionCount > maxRetries) {
			// 尝试次数超过用户定义的测试
			retry = false;
		} else if (exceptionBlacklist.contains(exception.getClass())) {
			// 线程被用户中断，则不继续尝试
			retry = false;
		} else if (exceptionWhitelist.contains(exception.getClass())) {
			// 出现的异常需要被重试
			retry = true;
		} else if (!sent) {
			// 请求没有到达
			retry = true;
		}
		// 如果需要重试
		if (retry) {
			// 获取request
			HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			// POST请求难道就不需要重试？
			retry = currentReq != null && !"POST".equals(currentReq.getMethod());
		}
		if (retry) {
			// 休眠1秒钟后再继续尝试
			SystemClock.sleep(RETRY_SLEEP_TIME_MILLIS);
		} else {
			exception.printStackTrace();
		}
		return retry;
	}
}