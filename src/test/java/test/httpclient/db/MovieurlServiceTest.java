package test.httpclient.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.moviegat.dyfm.service.MovieUrlService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class MovieurlServiceTest {
	@Autowired
	private MovieUrlService movieUrlService;
	
	@Test
	public void test1() throws Exception{
		movieUrlService.doMovieUrls();
	}
	
}
