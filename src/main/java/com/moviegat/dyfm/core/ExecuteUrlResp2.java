package com.moviegat.dyfm.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.moviegat.dyfm.bean.HttpProxyInfo;
import com.moviegat.dyfm.bean.db.MovieBasic;
import com.moviegat.dyfm.bean.db.UrlExecuteStatBean;
import com.moviegat.dyfm.exception.ResourceNotFountException;
import com.moviegat.dyfm.exception.RespUrlException;
import com.moviegat.dyfm.service.htmlparse.IMovieParse;
import com.moviegat.dyfm.util.MovieDoMain;
import com.moviegat.dyfm.util.RespUrlType;

public class ExecuteUrlResp2<F extends MovieBasic, T> {
	private static Logger logger = Logger.getLogger(ExecuteUrlResp2.class);

	private final HttpHost host;
	private final HttpClient httpClient;
	private final HttpGet httpGet;
	private List<F> respUrls = Lists.newArrayList();

	private List<UrlHandler> urlHandlerColl = Lists.newArrayList();
	private AtomicInteger atomicInt = new AtomicInteger(0);

	public ExecuteUrlResp2(Collection<F> respUrls, HttpClient httpClient,
			HttpGet httpGet, HttpHost host) {
		this.respUrls = Lists.newArrayList(respUrls);
		this.httpClient = httpClient;
		this.httpGet = httpGet;
		this.host = host;
	}

	public Boolean next() {
		return !respUrls.isEmpty() || !urlHandlerColl.isEmpty();
	}

	public void doUrlResultByGetMethod(ExecutorService threadPool,
			IPDyncDraw ipDynDraw, Map<F, T> urlAndResult,
			IMovieParse<T> movieParse,
			Map<F, UrlExecuteStatBean> urlAndExecStats, Integer threadNum,
			RespUrlType respUrlType) throws Exception {
		ExecutorCompletionService<UrlHandler> completionService = new ExecutorCompletionService<UrlHandler>(
				threadPool);
		int currExecRespUrlSize = 0;
		if (!urlHandlerColl.isEmpty()) {
			currExecRespUrlSize = urlHandlerColl.size();
		}

		int respUrlSize = respUrls.size();
		List<F> currExecRespUrl = Lists.newArrayList();
		for (int i = 0, j = 0; i < (respUrlSize >= threadNum ? (threadNum - currExecRespUrlSize)
				: respUrlSize); i++) {
			currExecRespUrl.add(respUrls.remove(j));
		}

		Collection<UrlHandler> tempUrlHandler = Collections2.transform(
				currExecRespUrl, new Function<F, UrlHandler>() {
					@Override
					public UrlHandler apply(F input) {
						return new UrlHandler(input, 0);
					}
				});
		urlHandlerColl.addAll(tempUrlHandler);

		for (UrlHandler urlHandler : urlHandlerColl) {
			this.registerCompletionService(ipDynDraw, httpGet,
					completionService, urlHandler);
		}

		int execUrlSize = urlHandlerColl.size();
		Set<UrlHandler> passUrl = Sets.newHashSet();
		for (int i = 0; i < execUrlSize; i++) {
			Future<UrlHandler> future = completionService.take();
			if (future.isDone()) {
				UrlHandler urlHandler = future.get();
				Object result = urlHandler.result;
				F movieBasic = urlHandler.movieBasic;

				if (result != null) {
					if (result instanceof String) {
						T tar = movieParse.parseByResult((String) result);
						urlAndResult.put(movieBasic, tar);
					} else if (result instanceof IOException) {// 此url无法正常执行，跳过此url
						IOException exception = (IOException) result;
						String errMsg = exception.getMessage();
						UrlExecuteStatBean urlExecuteStat = new UrlExecuteStatBean();

						urlExecuteStat.setDbId(movieBasic.getId());
						urlExecuteStat.setFialMsg(errMsg);
						urlExecuteStat.setLastExecTm(new Date());
						urlExecuteStat.setUrl(MovieDoMain.MOIVE_MAIN_URL
								+ movieBasic.getUrl());
						urlExecuteStat.setUrlType(respUrlType.toString());

						urlAndExecStats.put(movieBasic, urlExecuteStat);
					}
					passUrl.add(urlHandler);
				}
			}
		}
		int passUrlSize = passUrl.size();

		// 移除成功的url
		urlHandlerColl.removeAll(passUrl);

		logger.info("第 " + atomicInt.incrementAndGet() + " 次，执行完毕，共执行链接 --> "
				+ execUrlSize + " 条，成功 --> " + passUrlSize);

	}

	private void registerCompletionService(final IPDyncDraw ipDynDraw,
			final HttpGet httpGet,
			ExecutorCompletionService<UrlHandler> completionService,
			final UrlHandler urlRes) {
		completionService.submit(new Callable<UrlHandler>() {
			@Override
			public UrlHandler call() throws Exception {
				HttpProxyInfo httpProxy = ipDynDraw.getProxy();

				// 检测如果未设置代理，则设置代理
				if (httpClient.getParams().getParameter(
						ConnRoutePNames.DEFAULT_PROXY) == null) {
					httpClient.getParams()
							.setParameter(
									ConnRoutePNames.DEFAULT_PROXY,
									new HttpHost(httpProxy.getIp(), httpProxy
											.getPort()));
				}

				F movieBasic = urlRes.movieBasic;
				String url = movieBasic.getUrl();
				int execNum = urlRes.executeNum;

				HttpHost useProxy = (HttpHost) httpClient.getParams()
						.getParameter(ConnRoutePNames.DEFAULT_PROXY);
				String useIP = useProxy.getHostName();
				int usePort = useProxy.getPort();

				HttpHost myHost = host;
				logger.info("url --> " + url + "，第 " + (execNum + 1)
						+ " 次请求，使用代理 ip --> " + useIP + "，端口 --> " + usePort);

				Object result = null;
				httpGet.setURI(URI.create(url));

				try {
					result = httpClient.execute(myHost, httpGet,
							getResponseHandler());

					// 每成功执行一次，计数器加一
					httpProxy.addExecTotal();
				} catch (ResourceNotFountException e) {
					logger.info("资源未找到，url --> " + url);
					result = e;
				} catch (Exception e) {
					logger.error("url --> " + url, e);

					synchronized (httpClient) {

						HttpHost updateProxy = (HttpHost) httpClient
								.getParams().getParameter(
										ConnRoutePNames.DEFAULT_PROXY);
						String updateIP = updateProxy.getHostName();
						int updatePort = updateProxy.getPort();

						if (useIP.equals(updateIP) && usePort == updatePort) {
							// 显示移到下一个代理
							ipDynDraw.addProxyIndex();

							httpProxy = ipDynDraw.getProxy();

							httpClient.getParams().setParameter(
									ConnRoutePNames.DEFAULT_PROXY,
									new HttpHost(httpProxy.getIp(), httpProxy
											.getPort()));
						}
					}
					urlRes.executeNum = execNum + 1;
				}

				return new UrlHandler(movieBasic, result);
			}
		});
	}

	/**
	 * url辅助类
	 * 
	 * @author XX
	 * 
	 */
	class UrlHandler {
		F movieBasic;
		Object result;
		int executeNum;

		UrlHandler(F movieBasic, Object result) {
			this.movieBasic = movieBasic;
			this.result = result;
		}

		UrlHandler(F movieBasic, int executeNum) {
			this.movieBasic = movieBasic;
			this.executeNum = executeNum;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((movieBasic.getUrl() == null) ? 0 : movieBasic.getUrl()
							.hashCode());
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UrlHandler other = (UrlHandler) obj;
			if (movieBasic == null) {
				if (other.movieBasic != null)
					return false;
			} else if (!movieBasic.getUrl().equals(other.movieBasic.getUrl()))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "UrlHandler [url=" + movieBasic.getUrl() + ", executeNum="
					+ executeNum + "]";
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
	 * @throws ResourceNotFountException
	 */
	private static void checkRespHaveAlertError(String html) throws IOException {
		Document doc = Jsoup.parse(html);

		Elements htmlEle = doc.select("html");
		String key = null;
		if (!htmlEle.isEmpty()) {
			key = doc.select("html").first().attr("xmlns:wb");
		}
		Elements eles = doc.select("div.alert-error");
		String errMsg = eles.text();

		if (StringUtils.indexOf(errMsg, "影片暂时不可以访问") != -1) { // 链接资源错误
			throw new ResourceNotFountException("链接资源未找到");
		} else {
			if (!eles.isEmpty()) {
				throw new RespUrlException("请求页面结果错误");
			} else if (!"http://open.weibo.com/wb".equals(key)) {
				throw new RespUrlException("代理返回结果错误");
			}
		}
	}
}
