package com.moviegat.dyfm.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.google.common.base.CharMatcher;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.moviegat.dyfm.bean.HttpProxyInfo;
import com.moviegat.dyfm.bean.ProxyType;
import com.moviegat.dyfm.bean.db.ProxyBean;
import com.moviegat.dyfm.dao.ProxyDao;

/**
 * ip动态提取器
 * 
 * @author XX
 * 
 */
@Service
public class IPDyncDraw {
	@Autowired
	private ProxyDao proxyDao;

	private Logger logger = Logger.getLogger(IPDyncDraw.class);
	// 时间跨越范围,毫秒
	private final Integer skipTime = 1000 * 60 * 60 * 2;

	private final Integer groupProxyTotal = 30;

	// 代理url
	private String proxyUrl = "http://www.httpdaili.com/api.asp?ddbh=353004162165109&sl={0}";

	private String proxyUrl_daili = "http://www.dailiaaa.com/?ddh=263386390575109&dq=%C8%AB%B9%FA&sl={0}&xl=2&cf=4&tj=%CC%E1+%C8%A1";

	private List<HttpProxyInfo> proxys = Lists.newArrayList();

	private int loopNum = 1;

	private AtomicInteger nextIndex = new AtomicInteger(0);

	private List<ProxyBean> proxysDB;

	private String priorGet = "DB";

	public synchronized HttpProxyInfo getProxy() throws IOException,
			InterruptedException, ParseException {
		if (proxys.isEmpty()) {
			proxys = this.getNewProxy();
		} else if (nextIndex.get() == proxys.size()) {
			nextIndex = new AtomicInteger(0);
			loopNum++;
		}
		if (loopNum == 3) {
			this.saveProxy();
			for (;;) {
				proxys = this.getNewProxy();

				if (proxys.isEmpty()) {
					Date minLastDate = proxyDao.findMinLastusetm();
					Date newDate = DateUtils.addMilliseconds(minLastDate,
							skipTime);

					long sleepTime = newDate.getTime() - new Date().getTime();
					if (sleepTime > 0L) {
						Thread.sleep(sleepTime);
					} else {
						Thread.sleep(1000 * 60 * 5);
					}
				} else {
					break;
				}
			}
			loopNum = 1;

			nextIndex = new AtomicInteger(0);
		}
		return proxys.get(nextIndex.get());
	}

	public synchronized void addProxyIndex() {
		nextIndex.incrementAndGet();
	}

	/**
	 * 保存、更新代理
	 */
	private void saveProxy() {
		List<ProxyBean> proxyList = Lists.newArrayList();

		for (HttpProxyInfo proxyInfo : proxys) {
			if (proxyInfo.getProxyType() == ProxyType.DB) {
				final String dbId = proxyInfo.getDbId();
				ProxyBean proxyB = Iterables.find(proxysDB,
						new Predicate<ProxyBean>() {
							@Override
							public boolean apply(ProxyBean input) {
								return input.getId().equals(dbId);
							}
						});

				proxyB.setCallTotal(proxyB.getCallTotal() + 1);
				proxyB.setExecTotal(proxyB.getExecTotal()
						+ proxyInfo.getExecTotal());

				proxyList.add(proxyB);
			} else if (proxyInfo.getProxyType() == ProxyType.WEB) {
				ProxyBean proxyB = new ProxyBean();
				proxyB.setIp(proxyInfo.getIp());
				proxyB.setPort(proxyInfo.getPort());
				proxyB.setCallTotal(1);
				proxyB.setExecTotal(proxyInfo.getExecTotal());

				proxyList.add(proxyB);
			}
		}

		proxyDao.save(proxyList);
	}

	/**
	 * 得到新的代理
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<HttpProxyInfo> getNewProxy() throws IOException {
		List<HttpProxyInfo> allProxy = Lists.newArrayList();
		List<HttpProxyInfo> httpProxy = null;
		if (priorGet.equals("WEB")) { // web 优先获得
			httpProxy = this.getProxyByWeb(groupProxyTotal);
		} else { // db 优先获得
			httpProxy = this.getProxyByDB(groupProxyTotal);
		}

		allProxy.addAll(httpProxy);
		int proxySize = httpProxy.size();
		if (proxySize < groupProxyTotal) {
			int proxyWebSize = groupProxyTotal - proxySize;
			List<HttpProxyInfo> httpProxySecond = null;

			if (priorGet.equals("WEB")) { // 如果代理不够，则从数据库或者web中补充
				httpProxySecond = this.getProxyByDB(proxyWebSize);
			} else {
				httpProxySecond = this.getProxyByWeb(proxyWebSize);
			}

			allProxy.addAll(httpProxySecond);
		}

		return allProxy;
	}

	/**
	 * 从web页面中获得代理
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<HttpProxyInfo> getProxyByWeb(Integer backRow)
			throws IOException {
		logger.info("获取WEB代理～，row --> " + backRow);

		String url = MessageFormat.format(proxyUrl, backRow);
		Document doc = null;
		try {
			// 连接超时时间为 10分钟
			doc = Jsoup.connect(url).timeout(1000 * 60 * 3).get();
		} catch (Exception e) {
			logger.error("WEB代理请求错误 -- >", e);
			return Lists.newArrayList();
		}
		String bodyHtml = doc.body().html();
		Iterable<String> bodyIter = Splitter.on("<hr />").split(bodyHtml);
		List<HttpProxyInfo> httpProxyColl = Lists.newArrayList();

		if (Iterables.size(bodyIter) > 3) {
			String ipArea = Iterables.get(bodyIter, 2);
			Iterable<String> ipIter = Splitter.on("<br />").omitEmptyStrings()
					.trimResults().split(ipArea);

			if (ipIter != null) {
				for (String ipScheme : ipIter) {
					Iterable<String> ipSchemeIter = Splitter.on(':').split(
							StringUtils.trim(ipScheme));
					HttpProxyInfo httpProxy = new HttpProxyInfo(0);
					httpProxy.setIp(Iterables.get(ipSchemeIter, 0));
					httpProxy.setPort(Integer.parseInt(Iterables.get(
							ipSchemeIter, 1)));
					httpProxy.setProxyType(ProxyType.WEB);

					httpProxyColl.add(httpProxy);
				}
			}
		} else {
			logger.error("web代理页面结果错误");
		}

		return httpProxyColl;
	}

	private List<HttpProxyInfo> getProxyByWeb_Daili(Integer backRow)
			throws IOException {
		logger.info("获取WEB_Daili代理～，row --> " + backRow);

		String url = MessageFormat.format(proxyUrl_daili, backRow);
		Document doc = null;
		try {
			// 连接超时时间为 10分钟
			doc = Jsoup.connect(url).timeout(1000 * 60 * 3).get();
		} catch (Exception e) {
			logger.error("WEB代理请求错误 -- >", e);
			return Lists.newArrayList();
		}

		String startStr = "发放10万IP";
		String endStr = "剩余IP";

		String targetStr = doc.select(".mass").text();

		int start = targetStr.indexOf(startStr) + startStr.length();
		int last = targetStr.indexOf(endStr);

		String ipCon = StringUtils.trim(targetStr.substring(start, last));

		Iterable<String> bodyIter = Splitter.on(CharMatcher.WHITESPACE).split(
				ipCon);

		List<HttpProxyInfo> httpProxyColl = Lists.newArrayList();

		if (Iterables.size(bodyIter) != 0) {
			for (String ipAndPort : bodyIter) {
				Iterable<String> ipPortIter = Splitter.on(':').split(ipAndPort);

				HttpProxyInfo httpProxy = new HttpProxyInfo(0);
				httpProxy.setIp(Iterables.get(ipPortIter, 0));
				httpProxy
						.setPort(Integer.parseInt(Iterables.get(ipPortIter, 1)));
				httpProxy.setProxyType(ProxyType.WEB);
				httpProxyColl.add(httpProxy);
			}
		} else {
			logger.error("web代理页面结果错误");
		}

		return httpProxyColl;
	}

	/**
	 * 从DB中获取一组最旧的代理
	 * 
	 * @return
	 */
	private List<HttpProxyInfo> getProxyByDB(Integer backRow) {
		logger.info("获取DB代理～，row --> " + backRow);

		Order order = new Order(Direction.ASC, "lastusetm");
		Sort sort = new Sort(Lists.newArrayList(order));

		proxysDB = proxyDao.findAll(sort);

		int proxyDBSize = proxysDB.size();
		if (!proxysDB.isEmpty()) { // 获得前 30 条的代理
			proxysDB = proxysDB.subList(0, backRow < proxyDBSize ? backRow
					: proxyDBSize);
		}

		List<HttpProxyInfo> httpProxyColl = Lists.newArrayList();

		for (ProxyBean proxyDB : proxysDB) {
			HttpProxyInfo httpProxy = new HttpProxyInfo(proxyDB.getExecTotal());
			httpProxyColl.add(httpProxy);

			httpProxy.setIp(proxyDB.getIp());
			httpProxy.setDbId(proxyDB.getId());
			httpProxy.setPort(proxyDB.getPort());
			httpProxy.setProxyType(ProxyType.DB);
			httpProxy.setCallTotal(proxyDB.getCallTotal());
		}

		return httpProxyColl;
	}

}
