package test.httpclient;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class MuiltThreadTest {
	@Test
	public void testThreadPool1() throws InterruptedException,
			ExecutionException {
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(
				threadPool);

		final Counting counting = new Counting();
		for (int i = 0; i < 1000; i++) {
			completionService.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return counting.add();
				}
			});
		}
		
		for (int i = 0; i < 1000; i++) {
			Future<Integer> tuture = completionService.take();
			
			if(tuture.isDone()){
				Integer result = tuture.get();
				System.out.println(result);
			}
		}

	}

}

class Counting {
	private int i;
	
	public  Integer add() {
		return ++i;
	}
}
