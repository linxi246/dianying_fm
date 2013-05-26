package com.moviegat.dyfm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moviegat.dyfm.core.IPDyncDraw;
import com.moviegat.dyfm.dao.MovieDao;
import com.moviegat.dyfm.dao.MovieUrlDao;
import com.moviegat.dyfm.dao.UrlExecuteStatDao;
import com.moviegat.dyfm.service.httpclient.MovieDispense2;


@Service
public class MovieService {
	@Autowired
	private MovieUrlDao movieUrlDao;
	@Autowired
	private MovieDao movieDao;
	@Autowired
	private IPDyncDraw ipDynDraw;
	@Autowired
	private UrlExecuteStatDao urlExecuteStatDao;
	
	public void getMovie() throws Exception{
		MovieDispense2 movieD = new MovieDispense2();
		movieD.getMovie(movieDao, movieUrlDao, ipDynDraw,urlExecuteStatDao);
	}
	
}
