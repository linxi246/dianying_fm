package test.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;

public class DianyingFm {
	
	public void testColl() {
		List<String> list = Lists.newArrayList("a", "b", "c", "d", "f", "e");

		while (list.size() != 0) {
			List<String> otherList = Lists.newArrayList(list.subList(0, 2));
			System.out.println(list.removeAll(otherList));
		}
	}

	public void testLogin() throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://dianying.fm/category/");

		HttpResponse resRult = httpclient.execute(httpget);
		InputStream content = resRult.getEntity().getContent();

		String resHtml = CharStreams.toString(new InputStreamReader(content,
				Charsets.UTF_8));

		Map<String, List<MovieCateInfo>> allMovieCateInfo = parseHtmlBackCate(resHtml);

		httpget.releaseConnection();
		httpclient.getConnectionManager().shutdown();
	}

	/**
	 * 解析html，获取类型信息
	 * 
	 * class --> 电影、电视剧... <br/>
	 * year --> 年份 <br/>
	 * region --> 地区 <br/>
	 * genre --> 类型
	 * 
	 * @param html
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private Map<String, List<MovieCateInfo>> parseHtmlBackCate(String html)
			throws UnsupportedEncodingException {
		ImmutableList<String> movieTopCate = ImmutableList.of("class", "year",
				"region", "genre");

		Document jsoup = Jsoup.parse(html);
		// 获得所有
		Elements allMovieSelCateMenu = jsoup
				.select("div.subnav-fixed ul.dropdown-menu");
		Map<String, List<MovieCateInfo>> allMovieCateInfo = Maps.newHashMap();

		int cateIndex = 0;
		for (Element movieSelCateMenu : allMovieSelCateMenu) {
			if (cateIndex + 1 > movieTopCate.size()) {
				break;
			}

			String topCate = movieTopCate.get(cateIndex);

			List<MovieCateInfo> movieCateList = Lists.newArrayList();
			allMovieCateInfo.put(topCate, movieCateList);

			boolean isFirst = true;
			for (Element movieCate : movieSelCateMenu.select("li")) {
				if (isFirst) {
					isFirst = false;
					continue;
				}

				String movieName = movieCate.text();
				Elements cateHref = movieCate.select("a");
				Preconditions.checkArgument(!cateHref.isEmpty());

				String cateHrefAttr = movieCate.select("a").get(0).attr("href");
				// 获取以 '/' 分割的字符串的最后一位字符
				cateHrefAttr = Iterables.getLast(Splitter.on('/')
						.omitEmptyStrings().split(cateHrefAttr));

				MovieCateInfo movieCateInfo = new MovieCateInfo();
				movieCateInfo.name = movieName;
				// 将需要转码的汉字转码～
				movieCateInfo.id = cateHrefAttr.indexOf('%') == -1 ? cateHrefAttr
						: URLDecoder.decode(cateHrefAttr,
								Charsets.UTF_8.toString());

				movieCateList.add(movieCateInfo);
			}
			cateIndex++;
		}

		return allMovieCateInfo;
	}

	class MovieCateInfo {
		String name;
		String id;
	}

}