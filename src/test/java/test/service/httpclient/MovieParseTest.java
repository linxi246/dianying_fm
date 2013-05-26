package test.service.httpclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.moviegat.dyfm.core.MyHttpClient;
import com.moviegat.dyfm.service.htmlparse.MovieParse;

public class MovieParseTest {

	@Test
	public void test() throws Exception {
		String[] testUrl = {
				"http://dianying.fm/movie/p-a-s-s-p-o-r-t-t-o-l-a-t-i-n-a-m-e-r-i-c-a/" };
		
//		System.setProperty("http.proxyHost", "127.0.0.1");
//		System.setProperty("http.proxyPort", "8086");
//		
		
		HttpClient httpClient = MyHttpClient.getCleanHttpClient();
		
//		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("183.25.19.107",6675));
		
		for (String url : testUrl) {
			HttpResponse response = httpClient.execute(new HttpGet(url));
			HttpEntity entity = response.getEntity();
			
			MovieParse movieParse = new MovieParse();

			System.out.println(movieParse.parseByResult(EntityUtils.toString(entity)));
		}
	}
}