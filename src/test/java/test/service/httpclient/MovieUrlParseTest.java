package test.service.httpclient;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.moviegat.dyfm.bean.db.MovieUrlBean;
import com.moviegat.dyfm.core.MyHttpClient;
import com.moviegat.dyfm.service.htmlparse.IMovieParse;
import com.moviegat.dyfm.service.htmlparse.MovieUrlParse;

public class MovieUrlParseTest {
	
	@Test
	public void test1() throws Exception{
		HttpClient httpClient = MyHttpClient.getCleanHttpClient();
		HttpGet httpGet = new HttpGet("http://dianying.fm/category/region_美国-year_70s-genre_传记");
		
		HttpResponse response = httpClient.execute(httpGet);
		
		HttpEntity entity = response.getEntity();
		String html = EntityUtils.toString(entity);
		
		IMovieParse<List<MovieUrlBean>> movieParse = new MovieUrlParse();
		movieParse.parseByResult(html,null);
	}
}
