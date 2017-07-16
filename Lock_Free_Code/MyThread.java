package atomicClasses;

import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by root on 4/21/17.
 */
public class MyThread {
    public static void main(String[] args) throws InterruptedException {
        long start = 0;
        TestThreadLocal testThreadLocal = new TestThreadLocal();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1001);
        ArrayList<Thread> producerList = new ArrayList<>();
        ArrayList<Thread> consumerList = new ArrayList<>();
        for (int i = 0; i < 900; i++) {
            producerList.add(new Thread(new Producer(testThreadLocal, i, cyclicBarrier)));
        }
        for (int i = 0; i < 100; i++) {
            consumerList.add(new Thread(new Consumer(testThreadLocal, i, cyclicBarrier)));
        }
        try {
            for (Thread thread : producerList) {
                thread.join();
                thread.start();
            }
            for (Thread thread : consumerList) {
                thread.join();
                thread.start();
            }
            try {
                start = System.currentTimeMillis();
                cyclicBarrier.await();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Time taken" + (System.currentTimeMillis() - start));
    }
}
