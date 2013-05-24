package test.service.httpclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.moviegat.dyfm.core.MyHttpClient;
import com.moviegat.dyfm.service.htmlparse.MovieParse;

public class MovieParseTest {

	@Test
	public void test() throws Exception {
		String[] testUrl = {
				"http://dianying.fm/movie/thundering-dawn/",
				"http://dianying.fm/movie/a-song-of-ice-and-fire-game-of-thrones-season-3/",
				"http://dianying.fm/movie/no-time-for-nuts/",
				"http://dianying.fm/movie/dexter-2006/",
				"http://dianying.fm/movie/hei-jing-di-er-ji/",
				"http://dianying.fm/movie/castle-in-the-sky/" };
		
//		System.setProperty("http.proxyHost", "127.0.0.1");
//		System.setProperty("http.proxyPort", "8086");
//		
		
		HttpClient httpClient = MyHttpClient.getCleanHttpClient();
		
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("222.220.1.91",6675));
		
		for (String url : testUrl) {
			HttpResponse response = httpClient.execute(new HttpGet(url));
			HttpEntity entity = response.getEntity();
			
			MovieParse movieParse = new MovieParse();

			System.out.println(movieParse.parseByResult(EntityUtils.toString(entity), null));
		}
	}
}