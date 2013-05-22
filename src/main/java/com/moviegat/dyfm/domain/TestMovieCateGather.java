package com.moviegat.dyfm.domain;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.moviegat.dyfm.service.MovieUrlService;

/**
 * 测试电影类型采集
 * 
 * @author XX
 * 
 */
public class TestMovieCateGather {

	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = new FileSystemXmlApplicationContext(
				"classpath:applicationContext.xml");

		// MovieCateService movieCate = ctx.getBean(MovieCateService.class);
		//
		// movieCate.doMovieCate();

		MovieUrlService movieUrlService = ctx.getBean(MovieUrlService.class);
		movieUrlService.doMovieUrls();
	}

}
