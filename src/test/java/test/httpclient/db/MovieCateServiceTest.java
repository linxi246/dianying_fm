package test.httpclient.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.moviegat.dyfm.service.MovieCateService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class MovieCateServiceTest {
	@Autowired
	private MovieCateService movieCateService;
	@Test
	public void test() throws Exception{
		
		movieCateService.doMovieCate();
	}
}