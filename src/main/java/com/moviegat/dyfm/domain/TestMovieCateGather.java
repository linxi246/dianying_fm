package com.moviegat.dyfm.domain;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.moviegat.dyfm.service.MovieService;

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

		MovieService movieUrlService = ctx.getBean(MovieService.class);
		movieUrlService.getMovie();
	}

}
