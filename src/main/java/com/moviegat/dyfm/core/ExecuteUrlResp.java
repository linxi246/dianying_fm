package com.moviegat.dyfm.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.moviegat.dyfm.bean.HttpProxyInfo;
import com.moviegat.dyfm.bean.db.UrlExecuteStatBean;
import com.moviegat.dyfm.exception.RespUrlException;
import com.moviegat.dyfm.service.htmlparse.IMovieParse;
import com.moviegat.dyfm.util.MovieDoMain;

/**
 * 执行Url请求
 * 
 * @author XX
 * 
 */
public class ExecuteUrlResp {
	private static Logger logger = Logger.getLogger(ExecuteUrlResp.class);

	private final static HttpHost host = new HttpHost(
			MovieDoMain.MOIVE_MAIN_URL);

	/**
	 * 通过Get方式获得单个Url结果集
	 * 
	 * @param httpClient
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws RespUrlException
	 */
	public static String getUrlRespByGet(HttpClient httpClient, String url)
			throws ClientProtocolException, IOException, RespUrlException {
		HttpGet httpGet = new HttpGet(url);
		String result = null;
		Boolean sign = false;
		int requestNum = 0;

		try {
			while (!sign) {
				try {
					result = httpClient.execute(host, httpGet,
							getResponseHandler());
					sign = true;
				} catch (RespUrlException rue) {
					logger.error("Url --> " + url + "  请求结果错误，重新请求(第"
							+ requestNum + "次)", rue.getCause());
				}

				requestNum++;
			}
		} finally {
			httpGet.releaseConnection();
			httpClient.getConnectionManager().shutdown();
		}

		return result;
	}

	/**
	 * 执行 url 集合，并将结果集放入 urlResults 集合中，将执行失败的url放入 urlExecBads中
	 * 
	 * @param respUrls
	 *            待要执行的url集合
	 * @param urlResults
	 *            url解析后的结果集
	 * @param movieParse
	 *            解析url返回值的类
	 * @param urlExecBads
	 *            记录执行出错的url
	 * @param threadNum
	 *            线程数
	 * @throws Exception
	 */
	public static <T> void doUrlResultByGetMethod(IPDyncDraw ipDynDraw,
			List<String> respUrls, List<T> urlResults,
			IMovieParse<T> movieParse, List<UrlExecuteStatBean> urlExecBads,
			Integer threadNum) throws Exception {
		if (respUrls.isEmpty())
			return;

		final HttpClient httpClient = MyHttpClient.getCoonPoolHttpClient();

		ExecutorService threadPool = Executors
				.newFixedThreadPool(threadNum == null ? 5 : threadNum);
		ExecutorCompletionService<UrlHandler> completionService = new ExecutorCompletionService<UrlHandler>(
				threadPool);
		// 构造新的url集合
		Collection<UrlHandler> urlStatColl = Collections2.transform(
				Lists.newArrayList(respUrls),
				new Function<String, UrlHandler>() {
					@Override
					public UrlHandler apply(String input) {
						return new UrlHandler(input + "", 0);
					}
				});

		Map<UrlHandler, T> urlResultMap = Maps.newConcurrentMap();

		final HttpGet httpGet = new HttpGet();

		int tempUrlsSize = urlStatColl.size();
		int loopNum = 0; // 循环次数

		try {
			while (tempUrlsSize != 0) {//

				final List<UrlHandler> tempUrlStatColl = Lists.newArrayList();
				// 注册线程
				for (int urlIndex = 0; urlIndex < tempUrlsSize; urlIndex++) {
					final UrlHandler urlRes = Iterables.get(urlStatColl,
							urlIndex);
					registerCompletionService(ipDynDraw, httpClient, httpGet,
							completionService, urlRes, tempUrlStatColl);
				}

				int loopSuccNum = 0;
				// 执行线程
				for (int urlIndex = 0; urlIndex < tempUrlsSize; urlIndex++) {
					Future<UrlHandler> future = completionService.take();
					if (future.isDone()) {
						UrlHandler urlResult = future.get();
						String result = urlResult.result;
						if (result != null) {// 成功
							urlResultMap.put(urlResult,
									movieParse.parseByResult(result));
							loopSuccNum++;
						}
					}
				}

				Set<UrlHandler> succUrl = urlResultMap.keySet();
				// 去除已经执行成功的url
				Iterables.removeAll(urlStatColl, Lists.newArrayList(succUrl));
				// 将失败的url重新放入循环体
				urlStatColl.clear();
				urlStatColl = tempUrlStatColl;

				logger.info("第 " + loopNum + " 执行完毕，共执行" + tempUrlsSize
						+ " 条链接，成功" + loopSuccNum + "条");

				tempUrlsSize = urlStatColl.size();
				loopNum++;
			}
		} catch (Exception e) {
			logger.error("错误 ： ", e);
		} finally {
			httpGet.releaseConnection();
			httpClient.getConnectionManager().shutdown();
		}

		for (String resp : respUrls) {
			UrlHandler findKey = new UrlHandler(resp, 0);

			if (urlResultMap.containsKey(findKey)) {
				T result = urlResultMap.get(findKey);
				urlResults.add(result);
			} else {
				urlResults.add(null);
			}
		}
	}

	private static void registerCompletionService(final IPDyncDraw ipDynDraw,
			final HttpClient httpClient, final HttpGet httpGet,
			ExecutorCompletionService<UrlHandler> completionService,
			final UrlHandler urlRes, final List<UrlHandler> tempUrlStatColl) {
		completionService.submit(new Callable<ExecuteUrlResp.UrlHandler>() {
			@Override
			public UrlHandler call() throws Exception {
				HttpProxyInfo httpProxy = ipDynDraw.getProxy();

				if (httpClient.getParams().getParameter(
						ConnRoutePNames.DEFAULT_PROXY) == null) {
					httpClient.getParams()
							.setParameter(
									ConnRoutePNames.DEFAULT_PROXY,
									new HttpHost(httpProxy.getIp(), httpProxy
											.getPort()));
				}

				String url = urlRes.url;
				int execNum = urlRes.executeNum;

				HttpHost useProxy = (HttpHost) httpClient.getParams()
						.getParameter(ConnRoutePNames.DEFAULT_PROXY);
				String useIP = useProxy.getHostName();
				int usePort = useProxy.getPort();

				HttpHost myHost = host;
				logger.info("url --> " + url + ",正常请求,第" + execNum
						+ "次,使用代理ip --> " + useIP + ",端口 --> " + usePort);

				String result = null;
				httpGet.setURI(URI.create(url));

				try {
					Thread.sleep(1000 * 5);

					result = httpClient.execute(myHost, httpGet,
							getResponseHandler());

					Thread.sleep(1000 * 5);
					// 每成功执行一次，计数器加一
					httpProxy.addExecTotal();
				} catch (Exception e) {
					logger.error("url --> " + url, e);

					synchronized (httpClient) {

						HttpHost updateProxy = (HttpHost) httpClient
								.getParams().getParameter(
										ConnRoutePNames.DEFAULT_PROXY);
						String updateIP = updateProxy.getHostName();
						int updatePort = updateProxy.getPort();

						if (useIP.equals(updateIP) && usePort == updatePort) {
							ipDynDraw.addProxyIndex();

							httpProxy = ipDynDraw.getProxy();

							httpClient.getParams().setParameter(
									ConnRoutePNames.DEFAULT_PROXY,
									new HttpHost(httpProxy.getIp(), httpProxy
											.getPort()));
							logger.info("更换代理,ip --> " + httpProxy.getIp()
									+ ", port --> " + httpProxy.getPort());

						}
					}

					urlRes.executeNum = execNum + 1;
					tempUrlStatColl.add(urlRes);
				}
				return new UrlHandler(url, result);
			}
		});
	}

	/**
	 * url辅助类
	 * 
	 * @author XX
	 * 
	 */
	private static class UrlHandler {

		UrlHandler(String url, String result) {
			this.url = url;
			this.result = result;
		}

		UrlHandler(String url, int executeNum) {
			this.url = url;
			this.executeNum = executeNum;
		}

		String url;
		String result;
		int executeNum;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((url == null) ? 0 : url.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UrlHandler other = (UrlHandler) obj;
			if (url == null) {
				if (other.url != null)
					return false;
			} else if (!url.equals(other.url))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "UrlHandler [url=" + url + "]";
		}
	}

	/**
	 * 相应拦截器，处理相应结果集，验证，并返回
	 * 
	 * @return
	 */
	private static ResponseHandler<String> getResponseHandler() {
		return new ResponseHandler<String>() {
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				checkRespStatusCode(response.getStatusLine());
				HttpEntity entity = response.getEntity();
				String result = null;
				if (entity != null) {
					InputStream content = entity.getContent();
					result = CharStreams.toString(new InputStreamReader(
							content, Charsets.UTF_8));
				}
				EntityUtils.consume(entity);
				checkRespHaveAlertError(result);
				return result;
			}
		};
	}

	private static void checkRespStatusCode(StatusLine statusLine)
			throws RespUrlException {
		if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
			throw new RespUrlException("请求状态码错误，错误码为："
					+ statusLine.getStatusCode());
		}
	}

	/**
	 * 检查返回内容是否错误
	 * 
	 * @param html
	 * @throws RespUrlException
	 */
	private static void checkRespHaveAlertError(String html)
			throws RespUrlException {
		Document doc = Jsoup.parse(html);

		Elements htmlEle = doc.select("html");
		String key = null;
		if (!htmlEle.isEmpty()) {
			key = doc.select("html").first().attr("xmlns:wb");
		}
		Elements eles = doc.select("div.alert-error");
		if (!eles.isEmpty()) {
			throw new RespUrlException("请求页面结果错误");
		} else if (!"http://open.weibo.com/wb".equals(key)) {
			throw new RespUrlException("代理返回结果错误");
		}
	}
}
