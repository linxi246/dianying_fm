package test.httpclient.db;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;
import com.moviegat.dyfm.bean.db.MovieBean;
import com.moviegat.dyfm.bean.db.MovieUrlBean;
import com.moviegat.dyfm.dao.MovieDao;
import com.moviegat.dyfm.dao.MovieUrlDao;

/**
 * 去除 movie_url 中重复的数据
 * 
 * @author XX
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class CleanMovieUrl {

	@Autowired
	private MovieUrlDao movieUrlDao;
	@Autowired
	private MovieDao movieDao;

	@Test
	public void test2() {
		List<String> list = movieDao.findReportMovieId();
		List<String> movieUrls = Lists.newArrayList();

		List<MovieBean> delMovies = Lists.newArrayList();
		for (String l : list) {
			List<MovieBean> movieList = movieDao.findByDyMovieId(l);

			delMovies.addAll(movieList);
			for (int i = 0; i < movieList.size(); i++) {
				MovieBean movieBean = movieList.get(i);
				movieUrls.add("/movie/" + movieBean.getDyMovieUrl() + "/");
			}
		}

		List<MovieUrlBean> movieUrlBeanList = movieUrlDao.findByUrlIn(movieUrls);
		for (MovieUrlBean urlBean : movieUrlBeanList) {
			urlBean.setIsGather(false);
		}
		
		movieDao.delete(delMovies);
		movieUrlDao.save(movieUrlBeanList);

	}
	
	
	public void test3(){
		List<String> movieUrl = movieUrlDao.query2();
		
		List<MovieBean> errMovie = movieDao.findByDyMovieUrlNotIn(movieUrl);
		
		System.out.println(errMovie);
		
	}

}
