package com.moviegat.dyfm.service.httpclient;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.moviegat.dyfm.bean.UrlExecuteStatBean;
import com.moviegat.dyfm.bean.db.MovieBean;
import com.moviegat.dyfm.bean.db.MovieUrlBean;
import com.moviegat.dyfm.core.ExecuteUrlResp;
import com.moviegat.dyfm.core.IPDyncDraw;
import com.moviegat.dyfm.dao.MovieDao;
import com.moviegat.dyfm.dao.MovieUrlDao;
import com.moviegat.dyfm.service.htmlparse.IMovieParse;
import com.moviegat.dyfm.service.htmlparse.MovieParse;

public class MovieDispense {
	Logger logger = Logger.getLogger(MovieUrlDispense.class);

	// 每次获得总个数
	private int signLoopSize = 100;

	public void getMovie(MovieDao movieDao, MovieUrlDao movieUrlDao,
			IPDyncDraw ipDynDraw) throws Exception {
		long count = movieUrlDao.size();
		long loopSizeNum = (count / signLoopSize) + 1;

		for (int loopNum = 0; loopNum < loopSizeNum; loopNum++) {
			Pageable pageable = new PageRequest(loopNum, signLoopSize);

			Page<MovieUrlBean> movieUrlPage = movieUrlDao.findByIsGather(false,pageable);

			List<MovieUrlBean> movieUrlList = movieUrlPage.getContent();
			Collection<String> movieLinks = Collections2.transform(
					movieUrlList, new Function<MovieUrlBean, String>() {
						@Override
						public String apply(MovieUrlBean input) {
							return input.getUrl();
						}
					});
			IMovieParse<MovieBean> movieParse = new MovieParse();
			List<UrlExecuteStatBean> urlExecBads = Lists.newArrayList();
			List<MovieBean> urlResults = Lists.newArrayList();

			ExecuteUrlResp.doUrlResultByGetMethod(ipDynDraw, movieLinks,
					urlResults, movieParse, urlExecBads, 10);

			int i = 0;
			for (MovieBean movie : urlResults) {
				MovieUrlBean movieUrl = movieUrlList.get(i);
				String url = movieUrl.getUrl();
				Iterable<String> temp = Splitter.on('/').omitEmptyStrings()
						.split(url);

				movie.setDyMovieUrl(Iterables.get(temp, 1));
				movie.setType(movieUrl.getType());

				movieUrl.setIsGather(true);
				i++;
			}

			movieDao.save(urlResults);
			movieUrlDao.save(movieUrlList);

			logger.info("loopNum --> " + loopNum);
		}
	}
}
