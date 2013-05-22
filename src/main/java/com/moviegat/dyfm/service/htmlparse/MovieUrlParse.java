package com.moviegat.dyfm.service.htmlparse;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.moviegat.dyfm.bean.db.MovieUrlBean;

public class MovieUrlParse implements IMovieParse<List<MovieUrlBean>> {

	@Override
	public List<MovieUrlBean> parseByResult(String html) throws Exception {
		Preconditions.checkNotNull(html);

		List<MovieUrlBean> movieUrlList = Lists.newArrayList();

		Document doc = Jsoup.parse(html);
		Elements movieArea = doc.select("div.x-category-result>ul");

		if (!movieArea.isEmpty()) {

			Elements movieDesics = movieArea.select("li div.x-movie-desc");

			for (Element movieDesic : movieDesics) {
				Elements movieHrefEle = movieDesic.select("p>a");
				String url = null;

				Preconditions.checkArgument(!movieHrefEle.isEmpty());

				if (!movieHrefEle.isEmpty()) {
					url = movieHrefEle.first().attr("href");
				}
				Elements movieMuted = movieDesic.select("p>.muted");
				String type = null;
				Integer year = null;

				// 获得年份、类型
				if (!movieMuted.isEmpty()) {
					String mutedCont = StringUtils.trim(movieMuted.text());
					Iterable<String> typeAndYear = Splitter
							.on(CharMatcher.anyOf("()")).omitEmptyStrings()
							.split(mutedCont);

					if (!Iterables.isEmpty(typeAndYear)) {
						String yearStr = null;

						if (Iterables.size(typeAndYear) == 1) {
							yearStr = Iterables.get(typeAndYear, 0);

						} else if (Iterables.size(typeAndYear) == 2) {
							type = Iterables.get(typeAndYear, 0);
							yearStr = Iterables.get(typeAndYear, 1);
						}

						if (StringUtils.isNumeric(yearStr)) {
							year = Integer.parseInt(yearStr);
						}
					}
				}

				Elements badgesEle = movieDesic.select("span.badge");
				Double douban = null;
				Double imdb = null;

				if (!badgesEle.isEmpty()) {
					String doubanColorSign = "green";
					String imdbColorSign = "orange";

					for (Element badge : badgesEle) {
						String styleStr = badge.attr("style");
						String val = badge.text();
						if (styleStr.indexOf(doubanColorSign) != -1) {
							douban = Double.parseDouble(val);
						} else if (styleStr.indexOf(imdbColorSign) != -1) {
							imdb = Double.parseDouble(val);
						}
					}
				}
				MovieUrlBean movieUrl = new MovieUrlBean();

				movieUrl.setDouban(douban);
				movieUrl.setImdb(imdb);
				movieUrl.setType(type == null ? "movie" : type);
				movieUrl.setUrl(url);
				movieUrl.setYear(year);

				movieUrlList.add(movieUrl);
			}
		}
		return movieUrlList;
	}

}