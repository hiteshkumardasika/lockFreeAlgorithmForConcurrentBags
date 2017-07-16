package pcthreads;

/**
 * Created by root on 4/25/17.
 */

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


public class ThreadMain {
    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1001);
        ProducerConsumerImpl sharedObject = new ProducerConsumerImpl();


        ArrayList<Thread> producerList = new ArrayList<>();
        for (int i = 0; i < 800; i++) {
            producerList.add(new Producer(sharedObject, cyclicBarrier));
        }
        ArrayList<Thread> consumerList = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            consumerList.add(new Consumer(sharedObject, cyclicBarrier));
        }

        for (Thread thread : producerList) {
            try {
                thread.join();
                thread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (Thread thread : consumerList) {
            try {
                thread.join();
                thread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}

