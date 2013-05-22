package com.moviegat.dyfm.service.htmlparse;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Preconditions;
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
			Elements movieListEle = movieArea.select("li div.x-movie-desc>p>a");
			for (Element movieEle : movieListEle) {
				String url = movieEle.attr("href");
				MovieUrlBean movieUrl = new MovieUrlBean();
				movieUrl.setUrl(url);

				movieUrlList.add(movieUrl);
			}
		}
		return movieUrlList;
	}

}
