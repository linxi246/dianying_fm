package test.httpclient.db;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.moviegat.dyfm.bean.HttpProxyInfo;
import com.moviegat.dyfm.core.IPDyncDraw;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class IPDyncDrawTest {

	final Logger logger = Logger.getLogger(IPDyncDrawTest.class);

	@Autowired
	private IPDyncDraw ipDyncDraw;

	@Test
	public void testExec() throws IOException, InterruptedException,
			ExecutionException, ParseException {
		HttpProxyInfo proxyInfo = ipDyncDraw.getProxy();
		final HttpProxyInfo[] proxyInfos = new HttpProxyInfo[] { proxyInfo };

		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(
				threadPool);

		final int testLoopTotal = 5;

		for (int i = 0; i < 1000; i++) {
			final int[] ii = new int[] { i };
			completionService.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					HttpProxyInfo proxyInfo1 = proxyInfos[0];
					Integer execTotal = proxyInfo1.addExecTotal();

					if (execTotal % (testLoopTotal) == 0) {
						proxyInfo1 = proxyInfos[0] = ipDyncDraw.getProxy();
					}

//					logger.info(proxyInfo1);
					return ii[0];
				}
			});
		}

		for (int i = 0; i < 1000; i++) {
			Future<Integer> future = completionService.take();

			if (future.isDone()) {
				future.get();
			}
		}

	}
}
