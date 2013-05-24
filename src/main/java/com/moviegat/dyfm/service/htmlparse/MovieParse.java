package com.moviegat.dyfm.service.htmlparse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.moviegat.dyfm.bean.db.MovieBean;

public class MovieParse implements IMovieParse<MovieBean> {
	@Override
	public MovieBean parseByResult(String paseHtml, String url)
			throws Exception {
		Preconditions.checkNotNull(paseHtml);

		Document doc = Jsoup.parse(paseHtml);
		Elements movieMainEle = doc.select("#main_content>div.row");

		if (movieMainEle.isEmpty())
			return null;

		// ////////////////定位要获取的资源位置////////////////////
		// 图片，src
		Element posterImgEle = movieMainEle.select("div.x-m-poster>img")
				.first();
		// 电影id 'data-id'
		Element dyMovieIdAEle = movieMainEle.select("div.x-m-area>div>a")
				.first();

		// 电影内容
		Elements movieConEle = movieMainEle.select("div.x-m-desc>*");

		// 中文名、英文名、年份
		Elements titleEle = movieConEle.select("div.x-m-title");
		// 电影信息，导演、主演、类型、地区。。。。
		Elements movieInfoEle = movieConEle.select("table.table-bordered");

		// 推荐资源
		Elements movieRecommendEle = null;
		if (movieConEle.size() >= 3) {
			movieRecommendEle = movieConEle.get(2).select("*");
		}

		// 剧情介绍
		Element summaryEle = movieConEle.select("div.x-m-summary").first();

		// 电影资源
		Elements movieResoEle = movieConEle.select("#resource_tab");
		Elements movieResoNavEle = null;
		Elements movieResoLinksEle = null;
		if (movieResoEle != null) {
			// 电影导航资源
			movieResoNavEle = movieResoEle.select("ul>li>a>*");
			// 电影链接资源
			movieResoLinksEle = movieResoEle.select("#links_table table");
		}

		// //////////////获取 海报url，dy id，中文，英文，年份//////////////
		String dyPosterUrl = posterImgEle.attr("src");
		String dyMovieId = dyMovieIdAEle.attr("data-id");
		String chName, enName = null;
		Integer year = null;
		String titleStr = titleEle.text();
		// 提取中文电影名 \u4e00-\u9fa5 正则汉字范围,如果没有匹配，则返回 null
		chName = StringUtils.trimToNull(CharMatcher.inRange('\u4e00', '\u9fa5')
				.or(CharMatcher.WHITESPACE).retainFrom(titleStr));
		// 英文电影名+年份
		String enNameAndYear = StringUtils.trimToEmpty(CharMatcher.inRange(
				'\u4e00', '\u9fa5').removeFrom(titleStr));
		// 通过 '(' 分割
		Iterable<String> enNameYearIter = Splitter.on(CharMatcher.anyOf("()"))
				.omitEmptyStrings().trimResults().split(enNameAndYear);
		if (Iterables.size(enNameYearIter) == 1) {
			// 英文名 或者 年份
			String tempStr = Iterables.get(enNameYearIter, 0);
			if (StringUtils.isNumeric(tempStr))
				year = Integer.parseInt(tempStr);
			else
				enName = tempStr;
		} else if (Iterables.size(enNameYearIter) == 2) {
			enName = Iterables.get(enNameYearIter, 0);

			String yearStr = Iterables.get(enNameYearIter, 1);
			if (StringUtils.isNumeric(yearStr))
				year = Integer.parseInt(yearStr);
		}

		// 导演，演员，类型，地区，上映时间，片长，别名，豆瓣url，imdb url，豆瓣url，imdb url
		String directors = null, starrings = null, genres = null, regions = null, showTms = null, fileLens = null, alias = null, doubanUrl = null, imdbUrl = null;
		// 豆瓣评分，IMDB 评分
		Double gradeDouban = null, gradeImdb = null;
		for (Element trEle : movieInfoEle.select("tr")) {
			Elements tdEles = trEle.select("td");
			String labName = tdEles.get(0).text();
			String labVal = tdEles.get(1).text();

			if ("导演".equals(labName)) {
				directors = labVal;
			} else if ("主演".equals(labName)) {
				starrings = labVal;
			} else if ("类型".equals(labName)) {
				genres = labVal;
			} else if ("地区".equals(labName)) {
				regions = labVal;
			} else if ("上映时间".equals(labName)) {
				showTms = labVal;
			} else if ("片长".equals(labName)) {
				fileLens = labVal;
			} else if ("别名".equals(labName)) {
				alias = labVal;
			} else if ("评分".equals(labName)) {
				Elements gradeEle = tdEles.get(1).select("a");

				for (Element grade : gradeEle) {
					Element span = grade.select("span").first();
					Element aEle = grade.select("a").first();

					if (span != null && aEle != null) {
						String styleAttr = span.attr("style");
						if (styleAttr.indexOf("green") != -1) {// 豆瓣
							doubanUrl = aEle.attr("href");
							gradeDouban = Double.parseDouble(span.text());
						} else if (styleAttr.indexOf("orange") != -1) {// imdb
							imdbUrl = aEle.attr("href");
							gradeImdb = Double.parseDouble(span.text());
						}
					}
				}
			}
		}

		String playUrlList = null, prevueResoUrl = null, bestResoUrl = null;
		// /////推荐资源////////
		if (movieRecommendEle != null) {
			String subTitle = StringUtils.trimToEmpty(movieRecommendEle
					.select("p.x-m-subtitle").first().text());
			if (subTitle.startsWith("播放")) {
				playUrlList = this.getRecommendReso(movieRecommendEle);
			} else if (subTitle.indexOf("预告片") != -1) {
				prevueResoUrl = this.getRecommendReso(movieRecommendEle);
			} else { // 最佳资源
				bestResoUrl = this.getRecommendReso(movieRecommendEle);
			}
		}

		// 剧情介绍
		String plotSummary = null;
		if (summaryEle != null)
			plotSummary = summaryEle.text();

		String movieReso = null;
		Integer resoNum = null;
		
		// 资源
		if (movieResoNavEle != null && movieResoLinksEle != null
				& movieResoNavEle.size() == movieResoLinksEle.size()) {
			List<Links> linkResos = Lists.newArrayList();
			
			for (int eleIndex = 0; eleIndex < movieResoNavEle.size(); eleIndex++) {
				Element movieResoNav = movieResoNavEle.get(eleIndex);
				Element movieResoLinks = movieResoLinksEle.get(eleIndex);
				String movieNavName = movieResoNav.text();

				List<Resource> resources = Lists.newArrayList();
				Links link = new Links(movieNavName, resources);
				linkResos.add(link);
				if (movieResoNav.hasClass("icon-magnet")) {// 磁力链接
					for (Element resoLinkEle : movieResoLinks.select("tr")) {
						Elements tdEle = resoLinkEle.select("td");
						Element nameEle = tdEle.first();

						// 为 显示全部链接
						if (nameEle.attr("colspan").equals("2"))
							continue;

						// 最后一个td标签中第一个子标签
						Element linkEle = tdEle.last().select("a").first();
						String format = nameEle.select("b").text();
						String size = nameEle.select(".muted").text();

						String name = nameEle.text();
						name = StringUtils.trimToNull(name.replace(format, "")
								.replace(size, ""));

						String linkVal = linkEle.attr("href");
						// 通过关键字分割，取得磁力链接左侧的字符
						linkVal = this.urlDraw(linkVal, "&dn=", 'l');

						Resource reso = new Resource(linkVal, name, format,
								size);
						resources.add(reso);
					}
					
					resoNum = resources.size();
				} else {
					for (Element resoLinkEle : movieResoLinks.select("tr")) {
						Elements tdEle = resoLinkEle.select("td");
						Element nameEle = tdEle.first();

						// 为 显示全部链接
						if (nameEle.attr("colspan").equals("2"))
							continue;

						Element linkEle = tdEle.last().select("a").first();

						String nameAndClickNum = nameEle.text();
						Iterable<String> nameClickNumIter = Splitter
								.on(CharMatcher.anyOf("()")).omitEmptyStrings()
								.trimResults().split(nameAndClickNum);
						String name = Iterables.get(nameClickNumIter, 0);

						Integer clickNum = 0;
						if (Iterables.size(nameClickNumIter) == 2) {
							String clickNumStr = Iterables.get(
									nameClickNumIter, 1);

							if (StringUtils.isNumeric(clickNumStr)) {
								clickNum = Integer.parseInt(clickNumStr);
							}
						}

						String linkVal = this.urlDraw(linkEle.attr("href"),
								"url=", 'r');

						Resource reso = new Resource(linkVal, name, clickNum);
						resources.add(reso);
					}
				}
			}
			
			ObjectMapper mapper = new ObjectMapper();
			movieReso = mapper.writeValueAsString(linkResos);
		}
		

		// 缺失 dyMovieId、type、posterUrl；dyMovieId、type由调用者填充，posterUrl
		// 为本地图片的url，需要另外一个程序获取下载填充
		MovieBean movie = new MovieBean();

		movie.setDyMovieId(dyMovieId);
		movie.setDyPosterUrl(dyPosterUrl);
		movie.setChName(chName);
		movie.setEnName(enName);
		movie.setYear(year);
		movie.setDirectors(directors);
		movie.setStarrings(starrings);
		movie.setGenres(genres);
		movie.setRegions(regions);
		movie.setShowTms(showTms);
		movie.setAlias(alias);
		movie.setFileLens(fileLens);
		movie.setDoubanUrl(doubanUrl);
		movie.setGradeDouban(gradeDouban);
		movie.setImdbUrl(imdbUrl);
		movie.setGradeImdb(gradeImdb);
		movie.setBestResoUrl(bestResoUrl);
		movie.setPrevueResoUrl(prevueResoUrl);
		movie.setPlayUrlList(playUrlList);
		movie.setPlotSummary(plotSummary);
		movie.setMovieReso(movieReso);

		movie.setHaveReso(movieReso == null ? false : true);
		movie.setGatherNum(1);
		movie.setResoNum(resoNum);

		return movie;
	}

	/**
	 * 解析推荐资源
	 * 
	 * @param movieRecommendEle
	 * @return
	 * @throws DecoderException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	private String getRecommendReso(Elements movieRecommendEle)
			throws DecoderException, JsonGenerationException,
			JsonMappingException, IOException {
		Elements playList = movieRecommendEle.select("div a");
		List<Resource> palyReso = Lists.newArrayList();

		for (Element play : playList) {
			String href = this.urlDraw(play.attr("href"), "url=", 'r');
			String name = play.text();
			palyReso.add(new Resource(href, name));
		}
		ObjectMapper mapper = new ObjectMapper();

		return mapper.writeValueAsString(palyReso);
	}

	/**
	 * 提取url
	 * 
	 * @param url
	 * @param splitChar
	 * @param side
	 *            l --> 提取左侧 r --> 提取右侧
	 * @return
	 * @throws DecoderException
	 * @throws UnsupportedEncodingException
	 */
	private String urlDraw(String url, String splitChar, Character side)
			throws UnsupportedEncodingException {
		String href = URLDecoder.decode(url, "UTF-8");
		if (href.indexOf(splitChar) != -1) {// 提取分割字符串右边的字符
			href = StringUtils.trimToEmpty(Iterables.get(Splitter.on(splitChar)
					.split(href), side == 'r' ? 1 : 0));
		}
		return href;
	}

	class Resource {
		@Override
		public String toString() {
			return "Resource [id=" + id + ", url=" + url + ", name=" + name
					+ "]";
		}

		public String id;
		public String url;
		public String name;
		public Integer clickNum;
		public String format;
		public String size;

		public Resource(String url, String name) {
			super();
			String id = CharMatcher.is('-').removeFrom(
					UUID.randomUUID().toString());
			this.id = id;
			this.url = url;
			this.name = name;
		}

		public Resource(String url, String name, String format, String size) {
			this(url, name);
			this.format = format;
			this.size = size;
		}

		public Resource(String url, String name, Integer clickNum) {
			this(url, name);
			this.clickNum = clickNum;
		}

	}

	class Links {
		public String linkName;
		public List<Resource> resos;
		public Integer count;

		public Links(String linkName, List<Resource> resos) {
			super();
			this.linkName = linkName;
			this.resos = resos;
			this.count = resos == null ? 0 : resos.size();
		}

		public Links() {
			super();
		}

		@Override
		public String toString() {
			return "Links [linkName=" + linkName + ", resos=" + resos
					+ ", count=" + count + "]";
		}
	}
}
