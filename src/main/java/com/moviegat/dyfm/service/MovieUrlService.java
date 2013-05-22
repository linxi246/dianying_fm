package com.moviegat.dyfm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moviegat.dyfm.dao.MovieCateDao;
import com.moviegat.dyfm.dao.MovieUrlDao;
import com.moviegat.dyfm.service.httpclient.MovieUrlDispense;

/**
 * 电影类型服务
 * 
 * @author XX
 * 
 */
@Service
public class MovieUrlService {
	@Autowired
	private MovieUrlDao movieUrlDao;
	@Autowired
	private MovieCateDao movieCateDao;

	public void doMovieCate() throws Exception {
		MovieUrlDispense movieUrlDis = new MovieUrlDispense();
		movieUrlDis.getMoiveUrlByMovieCate(movieCateDao, movieUrlDao);
	}
}
