package com.moviegat.dyfm.service.htmlparse;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * 电影总页数解析
 * 
 * @author XX
 * 
 */
public class MovieCountPageParse implements IMovieParse<Integer> {

	@Override
	public Integer parseByResult(String html) throws Exception {
		Preconditions.checkNotNull(html);

		Document doc = Jsoup.parse(html);
		Elements pageArea = doc.select("div.pagination-centered");
		if (pageArea.isEmpty())
			return 0;

		Integer lastPage = null;

		Elements allPageUrl = doc.select("div.pagination-centered li");
		Element lastPageUrl = allPageUrl.last().select("a").first();
		String href = lastPageUrl.attr("href");
		String requestAttrs = Iterables.getLast(Splitter.on('?').split(href));
		String lastPageStr = null;

		// 如果有多个请求参数，则需要进行分割解析
		if (requestAttrs.indexOf('&') != -1) {
			Iterable<String> keyAttrAndValAttr = Splitter.on(
					CharMatcher.anyOf("=&")).split(requestAttrs);
			int keyAttrAndValAttrSize = Iterables.size(keyAttrAndValAttr);
			for (int i = 0; i < keyAttrAndValAttrSize; i++) {
				if (i % 2 == 0) {
					String keyAttrName = Iterables.get(keyAttrAndValAttr, i);
					if (keyAttrName.equals("p")) {
						lastPageStr = Iterables.get(keyAttrAndValAttr, i + 1);
						break;
					}
				}
			}
		} else {
			lastPageStr = Iterables.getLast(Splitter.on("=")
					.split(requestAttrs));
		}

		if (StringUtils.isNumeric(lastPageStr)) {
			lastPage = Integer.parseInt(lastPageStr);
		}

		// 如果最后一页为50页，则判断此页的电影总数，如果总数为15则表示有下一页
		if (lastPage == 50) {
			Elements pageMovieArea = doc.select("ul.x-movie-list");
			Element pageMovie = pageMovieArea.first();

			Elements pageMovieEles = pageMovie.children();
			if (pageMovieEles.size() == 15)
				lastPage++;
		}

		return lastPage;
	}
}
