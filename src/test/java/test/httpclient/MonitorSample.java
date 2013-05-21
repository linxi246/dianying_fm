package test.httpclient;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.util.concurrent.Monitor;

public class MonitorSample {
    private List<String> list = new ArrayList<String>();
    private static final int MAX_SIZE = 10;

    private Monitor monitor = new Monitor();
    private Monitor.Guard listBelowCapacity = new Monitor.Guard(monitor) {
        @Override
        public boolean isSatisfied() {
            return (list.size() < MAX_SIZE);
        }
    };
    
    public void addToList(String item) throws InterruptedException {
        monitor.enterWhen(listBelowCapacity);
        try {
            list.add(item);
        } finally {
            monitor.leave();
        }
    }
    
    @Test
    public void test() throws InterruptedException{
    	MonitorSample monitorS = new MonitorSample();
    	
    	monitorS.addToList("a");
    	monitorS.addToList("a");
    	monitorS.addToList("a");
    	monitorS.addToList("a");
    	monitorS.addToList("a");
    	monitorS.addToList("a");
    	monitorS.addToList("a");
//    	monitorS.addToList("a");
//    	monitorS.addToList("a");
//    	monitorS.addToList("a");
//    	monitorS.addToList("a");
//    	monitorS.addToList("a");
//    	monitorS.addToList("a");
    	
    	System.out.println(list.size());
    }
}
