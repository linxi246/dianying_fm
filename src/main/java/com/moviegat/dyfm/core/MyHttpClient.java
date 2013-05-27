package com.moviegat.dyfm.core;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

/**
 * 
 * @author XX
 * 
 */
public class MyHttpClient {
	final static Logger logger = Logger.getLogger(MyHttpClient.class);

	/**
	 * 获得没有任何设置的实例
	 * 
	 * @return
	 */
	public static HttpClient getCleanHttpClient() {
		DefaultHttpClient httpClient = new DefaultHttpClient(getHttpParams());
		// httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		// new HttpHost("localhost", 8888));

		httpClient.addRequestInterceptor(getRequestInter());
		httpClient.addResponseInterceptor(getResponseInter());
		httpClient.setHttpRequestRetryHandler(buildMyRetryHandler());

		return httpClient;
	}

	/**
	 * 获得连接池的实例
	 * 
	 * @return
	 */
	public static HttpClient getCoonPoolHttpClient() {
		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
		cm.setMaxTotal(100);
		cm.setDefaultMaxPerRoute(20);

		DefaultHttpClient httpClient = new DefaultHttpClient(cm,
				getHttpParams());

		httpClient.addRequestInterceptor(getRequestInter());
		httpClient.addResponseInterceptor(getResponseInter());
		httpClient.setHttpRequestRetryHandler(buildMyRetryHandler());
		
		return httpClient;
	}

	/**
	 * 自定义参数
	 * 
	 * @return
	 */
	private static HttpParams getHttpParams() {
		HttpParams params = new BasicHttpParams();
		// http 版本
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		// 请求编码
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		// .....
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpProtocolParams
				.setUserAgent(
						params,
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31");

		// 超时设置
		/* 连接超时 4s */
		HttpConnectionParams.setConnectionTimeout(params, 1000 * 2);
		/* 请求超时 10s */
		HttpConnectionParams.setSoTimeout(params, 1000 * 10);

		return params;
	}

	/**
	 * 自定义请求异常恢复策略
	 * 
	 * @return
	 */
	private static HttpRequestRetryHandler buildMyRetryHandler() {

		return new HttpRequestRetryHandler() {
			public boolean retryRequest(IOException exception,
					int executionCount, HttpContext context) {
				if (executionCount >= 3) {
					// 超过最大次数则不需要重试
					return false;
				}
				if (exception instanceof NoHttpResponseException) {
					// 服务停掉则重新尝试连接
					return true;
				}
				if (exception instanceof SSLHandshakeException) {
					// SSL异常不需要重试
					return false;
				}
				HttpRequest request = (HttpRequest) context
						.getAttribute(ExecutionContext.HTTP_REQUEST);

				boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
				if (idempotent) {
					// 请求内容相同则重试
					return true;
				}
				return false;
			}
		};
	}

	/**
	 * 定义请求拦截器
	 * 
	 * @return
	 */
	private static HttpRequestInterceptor getRequestInter() {
		return new HttpRequestInterceptor() {
			public void process(final HttpRequest request,
					final HttpContext context) throws HttpException,
					IOException {
				if (!request.containsHeader("Accept-Encoding")) {
					request.addHeader("Accept-Encoding", "gzip");
				}
			}
		};
	}

	/**
	 * 定义响应拦截器
	 * 
	 * @return
	 */
	private static HttpResponseInterceptor getResponseInter() {
		return new HttpResponseInterceptor() {
			public void process(HttpResponse response, final HttpContext context)
					throws HttpException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					Header ceheader = entity.getContentEncoding();
					if (ceheader != null) {
						HeaderElement[] codecs = ceheader.getElements();
						for (int i = 0; i < codecs.length; i++) {
							if (codecs[i].getName().equalsIgnoreCase("gzip")) {
								response.setEntity(new GzipDecompressingEntity(
										response.getEntity()));
								return;
							}
						}
					}
				}
			}
		};
	}
}
