package test.httpclient.datamoving;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.moviegat.dyfm.task.datamoving.MovieBodyDise;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class DianyingBodyMoving {

	@Autowired
	MovieBodyDise bodyDise;

	@Test
	public void dataMoving() throws Exception {
		bodyDise.dise();
	}
}
