package test.httpclient;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class GuavaThread {
	final Logger logger = Logger.getLogger(GuavaThread.class);

	class TestBean {
		TestBean(long sleep, int id) {
			this.sleep = sleep;
			this.id = id;
		}

		long sleep;
		int id;
	}

	class TestResult {
		TestResult(String tm, int id) {
			this.tm = tm;
			this.id = id;
		}

		String tm;
		int id;
	}

	class Test1 {
		String i;

		Test1(String i) {
			this.i = i;
		}
	}
	

	@Test
	public void test1() {

	}

	public void testThreadPool1() throws InterruptedException,
			ExecutionException {
		ExecutorService pool = Executors.newFixedThreadPool(3);
		final ExecutorCompletionService<TestResult> completionService = new ExecutorCompletionService<TestResult>(
				pool);

		List<TestBean> testList = Lists.newArrayList();
		testList.add(new TestBean(1000 * 10, 1));
		testList.add(new TestBean(1000 * 1, 2));
		testList.add(new TestBean(1000 * 1, 3));
		testList.add(new TestBean(1000 * 1, 4));
		testList.add(new TestBean(1000 * 1, 5));
		testList.add(new TestBean(1000 * 3, 6));
		testList.add(new TestBean(1000 * 3, 7));
		testList.add(new TestBean(1000 * 4, 8));
		testList.add(new TestBean(1000 * 5, 9));
		testList.add(new TestBean(1000 * 5, 10));
		testList.add(new TestBean(1000 * 5, 11));

		for (final TestBean testB : testList) {
			completionService.submit(new Callable<TestResult>() {
				@Override
				public TestResult call() throws Exception {
					logger.info("id --> " + testB.id);

					long sleep = testB.sleep;

					if (sleep == 1000 * 4) {
						throw new Exception();
					}

					Thread.sleep(sleep);

					String tm = DateFormatUtils.format(new Date(), "HH:mm:ss");

					return new TestResult(tm, testB.id);
				}
			});

		}

		for (TestBean testB : testList) {
			Future<TestResult> future = completionService.take();

			try {
				if (future.isDone()) {
					TestResult testResult = future.get();
					logger.info("tm --> " + testResult.tm + "|id --> "
							+ testResult.id);
				}
			} catch (Exception e) {
				logger.info(e);
			}
		}

		logger.info("end");

		// topSites.add("a");
		// topSites.add("a");
		// topSites.add("a");
		// topSites.add("a");
		// topSites.add("a");
		// topSites.add("a");
		// topSites.add("a");
		// topSites.add("a");
		// topSites.add("a");
		//
		//
		// for (final String site : topSites) {
		// completionService.submit(new Callable<String>() {
		// @Override
		// public String call() throws Exception {
		// String cont = site + "....";
		// logger.info(cont);
		// Thread.sleep(1000);
		// return cont;
		// }
		// });
		// }
		//
		// for (final String site : topSites) {
		// Future<String> future = completionService.take();
		// String cont = future.get();
		// }

	}

	public void testThreadPool() throws InterruptedException,
			ExecutionException {
		ListeningExecutorService pool = MoreExecutors
				.listeningDecorator(Executors.newFixedThreadPool(10));
		List<String> topSites = Lists.newArrayList();
		topSites.add("a");
		topSites.add("a");
		topSites.add("a");
		topSites.add("a");
		topSites.add("a");
		topSites.add("a");

		int i = 10;
		while (i != 0) {
			for (final String siteUrl : topSites) {
				final ListenableFuture<String> future = pool
						.submit(new Callable<String>() {
							@Override
							public String call() throws Exception {
								// Thread.sleep(1000);
								return siteUrl + "....";
							}
						});

				future.addListener(new Runnable() {
					@Override
					public void run() {
						try {
							final String contents = future.get();
							logger.info(contents);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					}
				}, MoreExecutors.sameThreadExecutor());

				Thread.sleep(10);
			}

			logger.info("##############################");
			i--;
		}

	}

}
