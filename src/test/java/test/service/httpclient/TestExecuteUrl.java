package test.service.httpclient;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;
import com.moviegat.dyfm.bean.db.MovieBean;
import com.moviegat.dyfm.bean.db.UrlExecuteStatBean;
import com.moviegat.dyfm.core.ExecuteUrlResp;
import com.moviegat.dyfm.core.IPDyncDraw;
import com.moviegat.dyfm.service.htmlparse.IMovieParse;
import com.moviegat.dyfm.service.htmlparse.MovieParse;
import com.moviegat.dyfm.util.RespUrlType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class TestExecuteUrl {

	@Autowired
	private IPDyncDraw ipDyncDraw;

	@Test
	public void test() throws Exception {

		List<MovieBean> pageTotalList = Lists.newArrayList();
		List<UrlExecuteStatBean> urlExecBads = Lists.newArrayList();
		IMovieParse<MovieBean> movieParse = new MovieParse();
		List<String> respReadUrlList = Lists
				.newArrayList("/movie/dekalog-dekalog-szesc/");

		// 执行Url请求集合
		ExecuteUrlResp.doUrlResultByGetMethod(ipDyncDraw, respReadUrlList,
				pageTotalList, movieParse, urlExecBads, 1, RespUrlType.MOVIE);
	}
}
