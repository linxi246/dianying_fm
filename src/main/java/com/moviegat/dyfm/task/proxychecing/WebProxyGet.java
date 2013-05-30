package com.moviegat.dyfm.task.proxychecing;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.moviegat.dyfm.core.MyHttpClient;

/**
 * 从web页面中获得代理，每天 13:00 定时采集
 * 
 * @author XX
 * 
 */

public class WebProxyGet implements IProxyGet {
	static Logger logger = Logger.getLogger(WebProxyGet.class);

	@Override
	public Collection<CheckingProxy> gather() throws Exception {
		DailiWEB daili = new DailiWEB();
		Map<String, Integer> dailiMap = daili.getProxy();

		Ads56 ads = new Ads56();
		Map<String, Integer> adsMap = ads.getProxy();

		Map<String, Integer> allMap = Maps.newHashMap(dailiMap);
		allMap.putAll(adsMap);

		Collection<CheckingProxy> checkingProxys = Collections2.transform(
				allMap.entrySet(),
				new Function<Entry<String, Integer>, CheckingProxy>() {
					@Override
					public CheckingProxy apply(Entry<String, Integer> input) {
						CheckingProxy cp = new CheckingProxy();
						cp.setIp(input.getKey());
						cp.setPort(input.getValue());
						cp.setForm(ProxyForm.WEB);
						return cp;
					}
				});
		
		return checkingProxys;
	}

	public static void main(String args[]) throws IOException,
			URISyntaxException {
		Ads56 ads = new Ads56();
		Map<String, Integer> map = ads.getProxy();

		System.out.println(map.size());

		DailiWEB daili = new DailiWEB();
		map = daili.getProxy();

		System.out.println(map.size());
	}
}

class DailiWEB {
	String httpProxyUrl = "http://www.youdaili.cn/Daili/http/";
	String guoneiProxyUrl = "http://www.youdaili.cn/Daili/guonei/";
	String guowaiProxyUrl = "http://www.youdaili.cn/Daili/guowai/";
	String qqProxyUrl = "http://www.youdaili.cn/Daili/QQ/";
	ImmutableList<String> urls = ImmutableList.of(httpProxyUrl, guoneiProxyUrl,
			guowaiProxyUrl, qqProxyUrl);

	HttpClient httpclient = MyHttpClient.getCleanHttpClient();
	HttpGet get = new HttpGet();

	public Map<String, Integer> getProxy() throws ClientProtocolException,
			IOException, URISyntaxException {
		List<String> topLinks = getTopLink();
		List<String> allHtml = Lists.newArrayList();
		try {
			for (String top : topLinks) {
				boolean isFirst = true;
				String url = null;
				int pageSize = 0;
				int curr = 1;
				while (true) {
					if (isFirst)
						url = top;
					get.setURI(new URI(url));
					try {
						HttpResponse response = httpclient.execute(get);
						StatusLine statLine = response.getStatusLine();
						if (statLine.getStatusCode() == HttpStatus.SC_OK) {
							String html = MyHttpClient.getHtml(response
									.getEntity());
							allHtml.add(html);
							if (isFirst) {
								isFirst = false;
								Document doc = Jsoup.parse(html);
								pageSize = doc.select("ul.pagelist li").size();
								pageSize -= 3; // 获得总页数

							} else {
								pageSize--;
							}
							if (pageSize != 0) {
								curr++;
								int lastIndex = CharMatcher.is('.')
										.lastIndexIn(top);
								String leftStr = StringUtils.substring(top, 0,
										lastIndex);
								// 构造下一页url
								url = leftStr + "_" + curr + ".html";
							} else {
								break;
							}

							if (pageSize - 1 == 0)
								break;
						}
					} finally {
						get.releaseConnection();
					}
				}
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

		// 解析 ip
		Map<String, Integer> ipMaps = Maps.newHashMap();
		for (String html : allHtml) {
			Document doc = Jsoup.parse(html);
			// 获得所有代理
			String ipsHtml = doc.select("div.cont_font span").html();
			// 分割每行代理
			Iterable<String> ipLine = Splitter.on("<br />").omitEmptyStrings()
					.trimResults().split(ipsHtml);
			for (String line : ipLine) {
				Iterable<String> ipIter = Splitter.on(CharMatcher.anyOf(":@"))
						.limit(3).trimResults().split(line);
				ipMaps.put(Iterables.get(ipIter, 0),
						Integer.parseInt(Iterables.get(ipIter, 1)));
			}
		}
		return ipMaps;
	}

	/**
	 * 获得每个页面的 top url
	 * 
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private List<String> getTopLink() throws ClientProtocolException,
			IOException, URISyntaxException {
		List<String> topLink = Lists.newArrayList();
		for (String url : urls) {
			get.setURI(new URI(url));
			try {
				HttpResponse response = httpclient.execute(get);
				StatusLine statLine = response.getStatusLine();
				if (statLine.getStatusCode() == HttpStatus.SC_OK) {
					String html = MyHttpClient.getHtml(response.getEntity());
					Document doc = Jsoup.parse(html);
					Element hrefEle = doc.select("ul.newslist_line li").first()
							.select("a").first();
					topLink.add(hrefEle.attr("href"));
				}
			} finally {
				get.releaseConnection();
			}
		}
		return topLink;
	}
}

class Ads56 {
	String mainUrl = "http://www.56ads.com";

	public Map<String, Integer> getProxy() throws IOException {
		Document doc = Jsoup.connect(mainUrl).timeout(1000 * 5).get();
		List<String> findUrl = Lists.newArrayList();
		Date d = new Date();
		int day = 0;
		while (true) {
			String searchKey = DateFormatUtils.format(
					DateUtils.addDays(d, -(day)), "yyyy.M.d");
			Elements hrefEle = doc.select("a:contains(" + searchKey + ")");
			if (!hrefEle.isEmpty()) {
				for (Element href : hrefEle) {
					findUrl.add(mainUrl + href.attr("href"));
				}
			}

			if (day == 3) {
				break;
			}
			day++;
		}

		Map<String, Integer> map = Maps.newHashMap();
		for (String url : findUrl) {
			doc = Jsoup.connect(url).timeout(1000 * 5).get();

			String ipContent = doc.select("div.content p").html();

			Iterable<String> ipLines = Splitter.on("<br />").omitEmptyStrings()
					.trimResults().split(ipContent);

			for (String ipLine : ipLines) {
				Iterable<String> ipIter = Splitter.on(CharMatcher.anyOf(":@"))
						.limit(3).omitEmptyStrings().trimResults()
						.split(ipLine);
				map.put(Iterables.get(ipIter, 0),
						Integer.parseInt(Iterables.get(ipIter, 1)));
			}
		}
		return map;
	}
}
