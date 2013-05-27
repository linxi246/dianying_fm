package test.service.httpclient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.moviegat.dyfm.bean.db.MovieBasic;
import com.moviegat.dyfm.bean.db.MovieBean;
import com.moviegat.dyfm.bean.db.UrlExecuteStatBean;
import com.moviegat.dyfm.core.ExecuteUrlResp2;
import com.moviegat.dyfm.core.IPDyncDraw;
import com.moviegat.dyfm.core.MyHttpClient;
import com.moviegat.dyfm.service.htmlparse.IMovieParse;
import com.moviegat.dyfm.service.htmlparse.MovieParse;
import com.moviegat.dyfm.util.MovieDoMain;
import com.moviegat.dyfm.util.RespUrlType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class ExecuteUrlRespTest {
	@Autowired
	private IPDyncDraw ipDyncDraw;

	@Test
	public void test() throws Exception {
		HttpClient httpClient = MyHttpClient.getCoonPoolHttpClient();
		HttpHost host = new HttpHost(MovieDoMain.MOIVE_MAIN_URL);
		HttpGet httpGet = new HttpGet();
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		IMovieParse<MovieBean> movieParse = new MovieParse();

		List<MovieBasic> movieUrlList = Lists.newArrayList();
		movieUrlList.add(new MovieBasic("/movie/ultimate-survival-season-2/"));
		movieUrlList.add(new MovieBasic("/movie/dekalog-dekalog-szesc/"));

		ExecuteUrlResp2<MovieBasic, MovieBean> execute2 = new ExecuteUrlResp2<MovieBasic, MovieBean>(
				movieUrlList, httpClient, httpGet, host);

		while (execute2.next()) {
			Map<MovieBasic, MovieBean> urlAndResult = Maps.newHashMap();
			Map<MovieBasic, UrlExecuteStatBean> urlAndExecStats = Maps
					.newHashMap();

			execute2.doUrlResultByGetMethod(threadPool, ipDyncDraw,
					urlAndResult, movieParse, urlAndExecStats, 10,
					RespUrlType.MOVIE);

			List<MovieBasic> movieBDB = Lists.newArrayList();
			movieBDB.addAll(urlAndExecStats.keySet());
			movieBDB.addAll(urlAndResult.keySet());

			
			System.out.println("成功执行的url --> " + movieBDB);
			System.out.println("有效的 --> " + urlAndResult.values());
			System.out.println("失败的url --> " + urlAndExecStats.values());

		}

	}

}
