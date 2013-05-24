package com.moviegat.dyfm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moviegat.dyfm.core.IPDyncDraw;
import com.moviegat.dyfm.dao.MovieDao;
import com.moviegat.dyfm.dao.MovieUrlDao;
import com.moviegat.dyfm.service.httpclient.MovieDispense;


@Service
public class MovieService {
	@Autowired
	private MovieUrlDao movieUrlDao;
	@Autowired
	private MovieDao movieDao;
	@Autowired
	private IPDyncDraw ipDynDraw;
	
	
	public void getMovie() throws Exception{
		MovieDispense movieD = new MovieDispense();
		movieD.getMovie(movieDao, movieUrlDao, ipDynDraw);
	}
	
}
