package com.moviegat.dyfm.domain;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.moviegat.dyfm.task.datamoving.MovieBodyDise;

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

		MovieBodyDise movieService = ctx.getBean(MovieBodyDise.class);
		movieService.dise();
	}
}
