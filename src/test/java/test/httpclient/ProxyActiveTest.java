package test.httpclient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.google.common.base.CharMatcher;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.moviegat.dyfm.core.MyHttpClient;
import com.moviegat.dyfm.dao.MovieUrlDao;
import com.moviegat.dyfm.util.MovieDoMain;

public class ProxyActiveTest {
	Logger logger = Logger.getLogger(ProxyActiveTest.class);

	@Autowired
	private MovieUrlDao movieUrl;

	public void test1() throws ClientProtocolException, IOException {
		Sort sort = new Sort(Direction.DESC, "douban", "imdb", "year");
		HttpHost host = new HttpHost(MovieDoMain.MOIVE_MAIN_URL);

		// Page<MovieUrlBean> list = movieUrl.findByIsGather(false,
		// new PageRequest(0, 100, sort));

		int i = 0;
		// for (MovieUrlBean url : list) {
		// String key = url.getUrl();

		HttpClient httpClient = MyHttpClient.getCleanHttpClient();
		HttpGet httpGet = new HttpGet("http://dianying.fm/movie/stand-up-guys/");

		HttpHost proxy = new HttpHost("80.65.90.148", 80);
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
				proxy);

		HttpResponse response = httpClient.execute(httpGet);
		String html = EntityUtils.toString(response.getEntity());
		System.out.println(html);
		StatusLine statusLine = response.getStatusLine();

		if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			System.out.println(++i);
		}
		// }
	}

	public void test2() throws IOException, InterruptedException,
			ExecutionException {
		Document doc = Jsoup
				.connect("http://www.youdaili.cn/Daili/http/380.html")
				.timeout(1000 * 5).get();
		Elements ipContnt = doc.select("div.cont_font span");
		String ipText = ipContnt.html();
		Iterable<String> ipLineIter = Splitter.on("<br />").omitEmptyStrings()
				.trimResults().split(ipText);
		List<IP> ipList = Lists.newArrayList();
		for (String line : ipLineIter) {
			Iterable<String> splter = Splitter.on(CharMatcher.anyOf(":@"))
					.limit(3).trimResults().split(line);

			IP ip = new IP(Iterables.get(splter, 0), Integer.parseInt(Iterables
					.get(splter, 1)));

			ipList.add(ip);
		}
		ExecutorService threadPool = Executors.newFixedThreadPool(100);
		ExecutorCompletionService<IP> completionService = new ExecutorCompletionService<IP>(
				threadPool);
		final HttpClient httpClient = MyHttpClient.getCoonPoolHttpClient();
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);
		final HttpGet httpGet = new HttpGet("http://www.baidu.com/");

		for (final IP ip : ipList) {
			completionService.submit(new Callable<ProxyActiveTest.IP>() {
				@Override
				public IP call() throws Exception {
					synchronized (this) {
						HttpHost proxy = new HttpHost(ip.ip, ip.port);
						httpClient.getParams().setParameter(
								ConnRoutePNames.DEFAULT_PROXY, proxy);
						HttpResponse response = null;
						try {
							response = httpClient.execute(httpGet);
							StatusLine statusLine = response.getStatusLine();

							if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
								ip.active = true;
							} else {
								ip.active = false;
							}
						} catch (Exception e) {
							ip.active = false;
						}
						return ip;
					}

				}
			});
		}

		for (int i = 0; i < ipList.size(); i++) {
			Future<IP> futer = completionService.take();

			if (futer.isDone()) {
				IP ip = futer.get();
				if (ip != null && ip.active) {
					System.out.println(ip);
				}
			}
		}

		httpGet.releaseConnection();
		httpClient.getConnectionManager().shutdown();
	}

	@Test
	public void test3() throws IOException, InterruptedException {
		Document doc = Jsoup
				.connect("http://www.youdaili.cn/Daili/http/380.html")
				.timeout(1000 * 5).get();
		Elements ipContnt = doc.select("div.cont_font span");
		String ipText = ipContnt.html();
		Iterable<String> ipLineIter = Splitter.on("<br />").omitEmptyStrings()
				.trimResults().split(ipText);
		List<IP> ipList = Lists.newArrayList();
		for (String line : ipLineIter) {
			Iterable<String> splter = Splitter.on(CharMatcher.anyOf(":@"))
					.limit(3).trimResults().split(line);

			IP ip = new IP(Iterables.get(splter, 0), Integer.parseInt(Iterables
					.get(splter, 1)));
			ipList.add(ip);
		}

		ListeningExecutorService pool = MoreExecutors
				.listeningDecorator(Executors.newFixedThreadPool(50));

		final HttpClient httpClient = MyHttpClient.getCoonPoolHttpClient();
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);

		final long start = System.currentTimeMillis();

		List<ListenableFuture<IP>> ll = Lists.newArrayList();
		for (final IP ip : ipList) {
			final ListenableFuture<IP> futer = pool.submit(new Callable<IP>() {
				@Override
				public IP call() throws Exception {
					synchronized (ip) {
						final HttpGet httpGet = new HttpGet(
								"http://dianying.fm/");
						HttpHost proxy = new HttpHost(ip.ip, ip.port);
						httpClient.getParams().setParameter(
								ConnRoutePNames.DEFAULT_PROXY, proxy);
						HttpResponse response = null;
						try {
							response = httpClient.execute(httpGet);
							StatusLine statusLine = response.getStatusLine();

							if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
								ip.active = true;
								return ip;
							} else {
								ip.active = false;
								httpGet.abort();
								// logger.info("ip --> "+ip +" 状态错误 ");
								return null;
							}
						} catch (Exception e) {
							// logger.info("ip --> " +
							// ip+" ---> "+e.getMessage());
							httpGet.abort();
							return null;
						}
					}
				}
			});
			ll.add(futer);
		}

		ListenableFuture<List<IP>> fll = Futures.allAsList(ll);
		Futures.addCallback(fll, new FutureCallback<List<IP>>() {
			@Override
			public void onSuccess(List<IP> result) {
				// logger.info(result);
				Iterable<IP> ss = Iterables.filter(result, new Predicate<IP>() {
					@Override
					public boolean apply(IP input) {
						return input != null;
					}
				});

				System.out.println(ss);

				System.out.println(System.currentTimeMillis() - start);
			}

			@Override
			public void onFailure(Throwable t) {
				logger.info(t);
			}
		});

		// System.out.println("---");
		Thread.sleep(1000 * 10 * 20);

	}

	class IP {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((ip == null) ? 0 : ip.hashCode());
			result = prime * result + ((port == null) ? 0 : port.hashCode());
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
			IP other = (IP) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (ip == null) {
				if (other.ip != null)
					return false;
			} else if (!ip.equals(other.ip))
				return false;
			if (port == null) {
				if (other.port != null)
					return false;
			} else if (!port.equals(other.port))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "IP [ip=" + ip + ", port=" + port + "]";
		}

		String ip;
		Integer port;
		Boolean active;

		IP(String ip, Integer port, Boolean active) {
			this.ip = ip;
			this.port = port;
			this.active = active;
		}

		IP(String ip, Integer port) {
			this.ip = ip;
			this.port = port;
		}

		private ProxyActiveTest getOuterType() {
			return ProxyActiveTest.this;
		}

	}
}
