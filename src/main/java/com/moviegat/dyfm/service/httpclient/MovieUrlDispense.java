package com.moviegat.dyfm.service.httpclient;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.moviegat.dyfm.bean.UrlExecuteStatBean;
import com.moviegat.dyfm.bean.db.MovieCateBean;
import com.moviegat.dyfm.bean.db.MovieUrlBean;
import com.moviegat.dyfm.core.ExecuteUrlResp;
import com.moviegat.dyfm.core.IPDyncDraw;
import com.moviegat.dyfm.dao.MovieCateDao;
import com.moviegat.dyfm.dao.MovieUrlDao;
import com.moviegat.dyfm.service.htmlparse.IMovieParse;
import com.moviegat.dyfm.service.htmlparse.MovieUrlParse;
import com.moviegat.dyfm.util.MovieDoMain;

public class MovieUrlDispense {

	Logger logger = Logger.getLogger(MovieUrlDispense.class);

	/**
	 * 根据电影类型，采集url
	 * 
	 * @param movieCateDao
	 * @param movieUrlDao
	 * @param ipDynDraw
	 * @throws Exception
	 */
	public void getMoiveUrlByMovieCate(MovieCateDao movieCateDao,
			MovieUrlDao movieUrlDao, IPDyncDraw ipDynDraw) throws Exception {

		List<MovieCateBean> movieCateList = movieCateDao.findByIsRead(false);
		String movieCateUrl = MovieDoMain.MOIVE_CATE_URL;

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
			Set<List<MovieUrlBean>> urlResults = Sets.newHashSet();
			IMovieParse<List<MovieUrlBean>> movieParse = new MovieUrlParse();
			List<UrlExecuteStatBean> urlExecBads = Lists.newArrayList();

			ExecuteUrlResp.doUrlResultByGetMethod(ipDynDraw, respUrls,
					urlResults, movieParse, urlExecBads, 10);

			Integer repeatSize = 0;
			Integer urlSize = 0;
			if (urlResults.size() == respUrls.size()) {
				movieCate.setIsRead(true);
				movieCate.setReadTm(new Date());

				List<MovieUrlBean> movieUrlList = Lists.newArrayList();

				for (List<MovieUrlBean> movieUrl : urlResults) {
					movieUrlList.addAll(movieUrl);
				}
				List<MovieUrlBean> allMovieUrl = (List<MovieUrlBean>) movieUrlDao
						.findAll();

				Integer clearBefore = movieUrlList.size();
				urlSize = clearBefore;
				// 去掉已经在数据库中存在的url
				Iterables.removeAll(movieUrlList, allMovieUrl);
				Integer clearAfter = movieUrlList.size();

				repeatSize = clearBefore - clearAfter;
				// 必须所有url正常入库，才能修改 movieCate 状态
				movieUrlDao.save(movieUrlList);

				movieCateDao.save(movieCate);
			}

			logger.info("第 " + loop + " 次执行完毕，共取得url --> " + urlSize
					+ "，重复数 --> " + repeatSize + "；剩余次数 --> "
					+ (movieCateList.size() - loop) + " 次");
		}
	}
}
