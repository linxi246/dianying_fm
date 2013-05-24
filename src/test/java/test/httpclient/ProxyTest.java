package test.httpclient;

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ProxyTest {
	
	
	
	public void getProxyByWeb() throws IOException, InterruptedException, ParseException {
		Document doc = Jsoup.connect("http://www.dailiaaa.com/?ddh=263386390575109&dq=%C8%AB%B9%FA&sl=2&xl=2&cf=4&tj=%CC%E1+%C8%A1").get();
		Elements tt = doc.select(".mass");
		
		Elements a = tt.select("*");
		System.out.println(a.text());
		
		
//		System.out.println(tt.html());
	}
	
	public void test1(){
		String tt = "最新HTTP代理IP 24小时自助提取系统 目前有效代理数: 1596 端口6666,6675 本店回馈所有客户,5月15号前所有订单号赠送10万IP. 请广大客户查收 网赚论坛 论坛发贴数量超过50发放10万IP 113.137.192.66:6675 118.117.253.55:6675 剩余IP：2989 最新HTTP代理IP 24小时自助提取系统 目前有效代理数: 1596 端口6666,6675 本店回馈所有客户,5月15号前所有订单号赠送10万IP. 请广大客户查收 网赚论坛 论坛发贴数量超过50发放10万IP 网赚论坛  ";
	
		int start = tt.indexOf("发放10万IP") + "发放10万IP".length();
		
		int last = tt.indexOf("剩余IP");
		
		System.out.println(StringUtils.trim(tt.substring(start,last)));
	}

}
