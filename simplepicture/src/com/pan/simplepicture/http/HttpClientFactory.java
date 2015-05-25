package com.pan.simplepicture.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

/**
 * Created by efida on 2014/6/7.
 */
public class HttpClientFactory {
	/** http请求最大并发连接数 */
	private static final int MAX_CONNECTIONS = 10;
	/** 超时时间 */
	private static final int TIMEOUT = 10 * 1000;
	/** 缓存大小 */
	private static final int SOCKET_BUFFER_SIZE = 8 * 1024; // 8KB
	/** 错误尝试次数，错误异常表请在RetryHandler添加 */
	private static final int MAX_RETRIES = 5;
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";


	public static DefaultHttpClient create(boolean isHttps) {
		HttpParams params = createHttpParams();
		DefaultHttpClient httpClient = null;
		if (isHttps) {
			// 支持http与https
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			// ThreadSafeClientConnManager线程安全管理类
			ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
			httpClient = new DefaultHttpClient(cm, params);
		} else {
			httpClient = new DefaultHttpClient(params);
		}
		return httpClient;
	}

	private static HttpParams createHttpParams() {
		final HttpParams params = new BasicHttpParams();
		// 设置是否启用旧连接检查，默认是开启的。关闭这个旧连接检查可以提高一点点性能，但是增加了I/O错误的风险（当服务端关闭连接时）。
		// 开启这个选项则在每次使用老的连接之前都会检查连接是否可用，这个耗时大概在15-30ms之间
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);// 设置链接超时时间
		HttpConnectionParams.setSoTimeout(params, TIMEOUT);// 设置socket超时时间
		HttpConnectionParams.setSocketBufferSize(params, SOCKET_BUFFER_SIZE);// 设置缓存大小
		HttpConnectionParams.setTcpNoDelay(params, true);// 是否不使用延迟发送(true为不延迟)
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); // 设置协议版本
		HttpProtocolParams.setUseExpectContinue(params, true);// 设置异常处理机制
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);// 设置编码
		HttpClientParams.setRedirecting(params, false);// 设置是否采用重定向

		ConnManagerParams.setTimeout(params, TIMEOUT);// 设置超时
		ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(MAX_CONNECTIONS));// 多线程最大连接数
		ConnManagerParams.setMaxTotalConnections(params, 10); // 多线程总连接数
		return params;
	}

	private static void createHttpClient(DefaultHttpClient httpClient) {
		// 添加request的拦截器，添加必要的头信息
		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(HttpRequest request, HttpContext context) {
				if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
					request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
				}
			}
		});

		// 添加response拦截器，预先对response进行一些处理
		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
			public void process(HttpResponse response, HttpContext context) {
				final HttpEntity entity = response.getEntity();
				if (entity == null) {
					return;
				}
				final Header encoding = entity.getContentEncoding();
				if (encoding != null) {
					for (HeaderElement element : encoding.getElements()) {
						// 如果是以GZIP压缩的数据，利用内部的填充器包装一层Gzip的流
						if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
							response.setEntity(new InflatingEntity(response.getEntity()));
							break;
						}
					}
				}
			}
		});
		// 设置重试次数
		httpClient.setHttpRequestRetryHandler(new HttpRetry(MAX_RETRIES));
	}

	/** 当服务器返回的数据是以Gzip压缩的过后的数据，填充Response返回的实体数据 (Description)，则返回GZIP解压流 */
	private static class InflatingEntity extends HttpEntityWrapper {
		public InflatingEntity(HttpEntity wrapped) {
			super(wrapped);
		}

		@Override
		public InputStream getContent() throws IOException {
			return new GZIPInputStream(wrappedEntity.getContent());
		}

		// 因为数据是压缩数据，所以实际长度无法估计，可以返回-1
		@Override
		public long getContentLength() {
			return -1;
		}
	}

	/** 自定义的安全套接字协议的实现，目前采用默认的，未使用到 */
	private static class SSLSocketFactoryEx extends SSLSocketFactory {
		// 此类的实例表示安全套接字协议的实现，它充当用于安全套接字工厂或 SSLEngine 的工厂。用可选的一组密钥和信任管理器及安全随机字节源初始化此类。
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super(truststore);
			// TrustManager负责管理做出信任决定时使用的的信任材料，也负责决定是否接受同位体提供的凭据。
			// X509TrustManager此接口的实例管理使用哪一个 X509 证书来验证远端的安全套接字。决定是根据信任的证书授权、证书撤消列表、在线状态检查或其他方式做出的。
			TrustManager tm = new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;// 返回受验证同位体信任的认证中心的数组。
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
					// 给出同位体提供的部分或完整的证书链，构建到可信任的根的证书路径，并且返回是否可以确认和信任将其用于基于验证类型的客户端 SSL 验证。
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
					// 给出同位体提供的部分或完整的证书链，构建到可信任的根的证书路径，并且返回是否可以确认和信任将其用于基于验证类型的服务器 SSL 验证。
				}
			};
			sslContext.init(null, new TrustManager[]{tm}, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
}
