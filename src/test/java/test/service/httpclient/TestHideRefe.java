package test.service.httpclient;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.moviegat.dyfm.core.MyHttpClient;

public class TestHideRefe {
	
	@Test
	public void test() throws IOException{
		Document doc = Jsoup.connect("http://hiderefer.me/?http://dianying.fm/movie/finding-mr-right/").get();
		HttpClient httpClient = MyHttpClient.getCleanHttpClient();
		HttpGet httpget = new HttpGet("http://hiderefer.me/?http://dianying.fm/movie/finding-mr-right/");
		
		HttpResponse response = httpClient.execute(httpget);
		
		System.out.println(response.getStatusLine());
		HttpEntity entity = response.getEntity();
		
		
		System.out.println(EntityUtils.toString(entity));
	}
}
