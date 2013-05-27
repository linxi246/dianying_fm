package com.moviegat.dyfm.service.httpclient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.moviegat.dyfm.bean.db.MovieBean;
import com.moviegat.dyfm.bean.db.MovieUrlBean;
import com.moviegat.dyfm.bean.db.UrlExecuteStatBean;
import com.moviegat.dyfm.core.ExecuteUrlResp2;
import com.moviegat.dyfm.core.IPDyncDraw;
import com.moviegat.dyfm.core.MyHttpClient;
import com.moviegat.dyfm.dao.MovieDao;
import com.moviegat.dyfm.dao.MovieUrlDao;
import com.moviegat.dyfm.dao.UrlExecuteStatDao;
import com.moviegat.dyfm.service.htmlparse.IMovieParse;
import com.moviegat.dyfm.service.htmlparse.MovieParse;
import com.moviegat.dyfm.util.MovieDoMain;
import com.moviegat.dyfm.util.RespUrlType;

public class MovieDispense2 {
	Logger logger = Logger.getLogger(MovieUrlDispense.class);

	// 每次获得总个数
	private int signLoopSize = 10000;

	public void getMovie(MovieDao movieDao, MovieUrlDao movieUrlDao,
			IPDyncDraw ipDynDraw, UrlExecuteStatDao urlExecuteStatDao)
			throws Exception {
		Sort sort = new Sort(Direction.DESC, "douban", "imdb", "year");
		long count = 0L;

		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		HttpClient httpClient = MyHttpClient.getCoonPoolHttpClient();
		HttpHost host = new HttpHost(MovieDoMain.MOIVE_MAIN_URL);
		HttpGet httpGet = new HttpGet();
		IMovieParse<MovieBean> movieParse = new MovieParse();

		try {
			while ((count = movieUrlDao.findIsGatherSize()) != 0) {

				long loopSizeNum = (count == 0 ? 0 : (count / signLoopSize) + 1);

				for (int loopNum = 0; loopNum < loopSizeNum; loopNum++) {
					logger.info("第 " + (loopNum + 1) + " 次，剩余 --> "
							+ (loopSizeNum - (loopNum + 1)) + " 次");

					Pageable pageable = new PageRequest(loopNum, signLoopSize,
							sort);

					Page<MovieUrlBean> movieUrlPage = movieUrlDao
							.findByIsGather(false, pageable);
					List<MovieUrlBean> movieUrlList = movieUrlPage.getContent();

					ExecuteUrlResp2<MovieUrlBean, MovieBean> execute2 = new ExecuteUrlResp2<MovieUrlBean, MovieBean>(
							movieUrlList, httpClient, httpGet, host);

					while (execute2.next()) {
						Map<MovieUrlBean, UrlExecuteStatBean> urlExecStats = Maps
								.newHashMap();
						Map<MovieUrlBean, MovieBean> urlAndResult = Maps
								.newHashMap();

						execute2.doUrlResultByGetMethod(threadPool, ipDynDraw,
								urlAndResult, movieParse, urlExecStats, 10,
								RespUrlType.MOVIE_URL);

						for (MovieUrlBean movieUrl : urlAndResult.keySet()) {
							MovieBean movie = urlAndResult.get(movieUrl);

							Iterable<String> temp = Splitter.on('/')
									.omitEmptyStrings()
									.split(movieUrl.getUrl());

							movie.setDyMovieUrl(Iterables.get(temp, 1));
							movie.setType(StringUtils.trimToEmpty(movieUrl
									.getType()));

							movieUrl.setIsGather(true);
						}

						// 存放执行完成的对象
						List<MovieUrlBean> movieUrlDB = Lists.newArrayList();
						for (MovieUrlBean movieUrl : urlExecStats.keySet()) {
							// 标记对象已读取
							movieUrl.setIsGather(true);
						}

						movieUrlDB.addAll(urlExecStats.keySet());
						movieUrlDB.addAll(urlAndResult.keySet());

						// 更新已经使用过的url
						movieUrlDao.save(movieUrlDB);
						// 保存失败的url
						urlExecuteStatDao.save(urlExecStats.values());
						// 保存成功的movie
						movieDao.save(urlAndResult.values());
					}
				}
			}
		} finally {
			// 释放资源，关闭连接～
			httpGet.releaseConnection();
			httpClient.getConnectionManager().shutdown();
			threadPool.shutdown();
		}
	}
}