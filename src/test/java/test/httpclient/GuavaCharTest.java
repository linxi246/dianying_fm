package test.httpclient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.google.common.base.CharMatcher;

public class GuavaCharTest {
	Double d ;
	double dd ;
	
	@Test
	public void test() throws DecoderException, UnsupportedEncodingException{
		
		String str = StringUtils.trimToNull(CharMatcher
				.inRange('\u4e00', '\u9fa5').or(CharMatcher.WHITESPACE)
				.retainFrom("30 for 30 30 for 30 30 For 30 (2009)"));
		
		System.out.println(StringUtils.trim(str));
		
		DecimalFormat df = new DecimalFormat("#.00");
		
		System.out.println(df.format(3242.1));
		
		String url = "http://www.dailiaaa.com/?ddh=263386390575109&dq=%C8%AB%B9%FA&sl=1&xl=2&cf=4&tj=%CC%E1+%C8%A1";
		
		System.out.println(URLDecoder.decode(url, "UTF-8"));
		
		
		
		
		System.out.println(d +","+dd);
	}
}
