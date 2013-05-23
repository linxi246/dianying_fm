package com.moviegat.dyfm.service.htmlparse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

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
import com.moviegat.dyfm.bean.MovieCateColl;
import com.moviegat.dyfm.bean.MovieCateInfo;

/**
 * 电影类型解析
 * 
 * @author Administrator
 * 
 */
public class MovieCateParse implements IMovieParse<List<MovieCateColl>> {

	/**
	 * 解析html，获取类型信息
	 * 
	 * class --> 电影、电视剧... <br/>
	 * year --> 年份 <br/>
	 * region --> 地区 <br/>
	 * genre --> 类型
	 * 
	 * @param <T>
	 * @param <T>
	 * @param <T>
	 * 
	 * @param html
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@Override
	public List<MovieCateColl> parseByResult(String html,String url) throws Exception {
		ImmutableList<String> movieTopCate = ImmutableList.of("class", "year",
				"region", "genre");
		
		ImmutableList<String> ratios = ImmutableList.of("region", "year",
				"genre", "class");

		Document jsoup = Jsoup.parse(html);
		// 获得所有
		Elements allMovieSelCateMenu = jsoup
				.select("div.subnav-fixed ul.dropdown-menu");
		List<MovieCateColl> movieCateCollList = Lists.newArrayList();

		int cateIndex = 0;
		for (Element movieSelCateMenu : allMovieSelCateMenu) {
			if (cateIndex + 1 > movieTopCate.size()) {
				break;
			}

			String topCate = movieTopCate.get(cateIndex);

			MovieCateColl movieCateColl = new MovieCateColl();
			List<MovieCateInfo> movieCateList = Lists.newArrayList();
			movieCateColl.setMovieCateList(movieCateList);
			movieCateColl.setCateName(topCate);

			movieCateCollList.add(movieCateColl);

			boolean isFirst = true;
			for (Element movieCate : movieSelCateMenu.select("li")) {
				if (isFirst) {// 排除第一个
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
				movieCateInfo.setName(movieName);
				// 将需要转码的汉字转码～
				movieCateInfo
						.setId(cateHrefAttr.indexOf('%') == -1 ? cateHrefAttr
								: URLDecoder.decode(cateHrefAttr,
										Charsets.UTF_8.toString()));

				movieCateList.add(movieCateInfo);
			}
			cateIndex++;
		}

		List<MovieCateColl> tempMovieCateColl = Lists.newArrayList();
		for (String ratio : ratios) {
			int topCateIndex = movieTopCate.indexOf(ratio);
			tempMovieCateColl.add(movieCateCollList.get(topCateIndex));
		}
		movieCateCollList = tempMovieCateColl;

		return movieCateCollList;
	}
}