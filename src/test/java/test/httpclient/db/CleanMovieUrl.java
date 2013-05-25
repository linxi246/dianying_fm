package test.httpclient.db;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.moviegat.dyfm.bean.db.MovieUrlBean;
import com.moviegat.dyfm.dao.MovieUrlDao;

/**
 * 去除 movie_url 中重复的数据
 * @author XX
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class CleanMovieUrl {

	@Autowired
	private MovieUrlDao movieUrlDao;

	@Test
	public void test() {
		List<String> list = movieUrlDao.query1();
		for (String l : list) {
			List<MovieUrlBean> movieUrlList = movieUrlDao.findByUrl(l);
			
			
			
			for (int i = 0; i < movieUrlList.size() - 1; i++) {
				movieUrlDao.delete(movieUrlList.get(i));
			}
			
		}
	}
}
