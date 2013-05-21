package com.moviegat.dyfm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashMultimap;
import com.moviegat.dyfm.bean.db.MovieCateStatBean;
import com.moviegat.dyfm.core.IPDyncDraw;
import com.moviegat.dyfm.dao.MovieCateDao;
import com.moviegat.dyfm.dao.MovieCateStatDao;
import com.moviegat.dyfm.service.httpclient.MovieCateDispense;

/**
 * 电影类型服务
 * @author XX
 *
 */
@Service
public class MovieCateService {
	@Autowired
	private MovieCateStatDao movieCateStatDao;
	@Autowired
	private MovieCateDao movieCateDao;
	@Autowired
	private IPDyncDraw ipDynDraw;
	
	
	public void doMovieCate() throws Exception {
		// 从数据库中获取所有电影状态
		List<MovieCateStatBean> movieCateStatList = (List<MovieCateStatBean>) movieCateStatDao
				.findAll();
		HashMultimap<String, MovieCateStatBean> movieCateStatMap = HashMultimap
				.create();
		for (MovieCateStatBean movieCateStat : movieCateStatList) {
			movieCateStatMap.put(movieCateStat.getCateType(), movieCateStat);
		}

		MovieCateDispense dispenseUrls = new MovieCateDispense();
		dispenseUrls.buildMovieCateSearGetTotalPage(movieCateStatMap,
				movieCateStatDao, movieCateDao,ipDynDraw);
	}
}
