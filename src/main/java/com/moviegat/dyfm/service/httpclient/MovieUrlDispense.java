package com.moviegat.dyfm.service.httpclient;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.moviegat.dyfm.bean.db.MovieCateBean;
import com.moviegat.dyfm.bean.db.MovieUrlBean;
import com.moviegat.dyfm.bean.db.UrlExecuteStatBean;
import com.moviegat.dyfm.core.ExecuteUrlResp;
import com.moviegat.dyfm.core.IPDyncDraw;
import com.moviegat.dyfm.dao.MovieCateDao;
import com.moviegat.dyfm.dao.MovieUrlDao;
import com.moviegat.dyfm.service.htmlparse.IMovieParse;
import com.moviegat.dyfm.service.htmlparse.MovieUrlParse;
import com.moviegat.dyfm.util.MovieDoMain;

public class MovieUrlDispense {

	Logger logger = Logger.getLogger(MovieUrlDispense.class);

	public void getMoiveUrlByMovieCate(MovieCateDao movieCateDao,
			MovieUrlDao movieUrlDao) throws Exception {

		List<MovieCateBean> movieCateList = movieCateDao.findByIsRead(false);
		String movieCateUrl = MovieDoMain.MOIVE_CATE_URL;

		IPDyncDraw ipDynDraw = new IPDyncDraw();
		Integer loop = 0;

		for (MovieCateBean movieCate : movieCateList) {
			loop++;

			int pageTotal = movieCate.getTotalPageNum();
			String cateUrl = movieCate.getCateUrl();

			// 构造请求url
			List<String> respUrls = Lists.newArrayList();
			for (int page = 1; page <= pageTotal; page++) {
				String url = movieCateUrl + cateUrl + "?p="
						+ String.valueOf(page);
				respUrls.add(url);
			}
			List<List<MovieUrlBean>> urlResults = Lists.newArrayList();
			IMovieParse<List<MovieUrlBean>> movieParse = new MovieUrlParse();
			List<UrlExecuteStatBean> urlExecBads = Lists.newArrayList();

			ExecuteUrlResp.doUrlResultByGetMethod(ipDynDraw, respUrls,
					urlResults, movieParse, urlExecBads, 10);

			if (urlResults.size() == respUrls.size()) {
				movieCate.setIsRead(true);
				movieCate.setReadTm(new Date());
				movieCateDao.save(movieCate);

				List<MovieUrlBean> movieUrlList = Lists.newArrayList();

				for (List<MovieUrlBean> movieUrl : urlResults) {
					movieUrlList.addAll(movieUrl);
				}
				List<MovieUrlBean> allMovieUrl = (List<MovieUrlBean>) movieUrlDao
						.findAll();
				// 去掉已经在数据库中存在的url
				Iterables.removeAll(movieUrlList, allMovieUrl);
				movieUrlDao.save(movieUrlList);
			}

			logger.info("第 " + loop + " 次执行完毕，剩余 --> "
					+ (movieCateList.size() - loop) + " 次");
		}

	}
}
