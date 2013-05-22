package com.moviegat.dyfm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moviegat.dyfm.core.IPDyncDraw;
import com.moviegat.dyfm.dao.MovieCateDao;
import com.moviegat.dyfm.dao.MovieCateStatDao;
import com.moviegat.dyfm.service.httpclient.MovieCateDispense;

/**
 * 电影类型服务
 * 
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
		MovieCateDispense dispenseUrls = new MovieCateDispense();
		dispenseUrls.buildMovieCateSearGetTotalPage(movieCateStatDao,
				movieCateDao, ipDynDraw);
	}
}
