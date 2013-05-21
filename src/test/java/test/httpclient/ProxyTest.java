package test.httpclient;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Splitter;
import com.moviegat.dyfm.bean.HttpProxyInfo;
import com.moviegat.dyfm.core.IPDyncDraw;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class ProxyTest {
	@Autowired
	private IPDyncDraw ipDynamicDraw;
	
	public void test1(){
		String ss = "114.80.136.181:7780 <br />123.54.71.34:8001 <br />";
		Iterable<String> ipIter = Splitter.on("<br />").omitEmptyStrings().trimResults().split(ss);
		System.out.println(ipIter);
	}
	
	
	@Test
	public void getProxyByWeb() throws IOException, InterruptedException, ParseException {
		HttpProxyInfo proxys = ipDynamicDraw.getProxy();
		
		System.out.println(proxys);
		
		proxys = ipDynamicDraw.getProxy();
		
		System.out.println(proxys);
	}
}
