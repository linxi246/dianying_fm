package test.service.httpclient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class ExecuteUrlRespTest {

	@Test
	public void test() throws InterruptedException, ExecutionException, JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writeValueAsString(null));
	}
	
}
