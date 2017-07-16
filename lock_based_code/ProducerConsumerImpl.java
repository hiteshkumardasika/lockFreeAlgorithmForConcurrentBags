package pcthreads;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by root on 4/25/17.
 */
public class ProducerConsumerImpl {

    private static final int CAPACITY = 500;
    private final Queue queue = new LinkedList<>();
    private final Random theRandom = new Random();

    private final Lock aLock = new ReentrantLock();
    private final Condition bufferNotFull = aLock.newCondition();
    private final Condition bufferNotEmpty = aLock.newCondition();

    public void put() throws InterruptedException {
        aLock.lock();
        try {
            while (queue.size() == CAPACITY) {
                System.out.println(Thread.currentThread().getName() + " : Buffer is full, waiting");
                bufferNotEmpty.await();
            }
            int number = theRandom.nextInt();
            boolean isAdded = queue.offer(number);

            if (isAdded) {
                System.out.printf("%s added %d into queue %n", Thread.currentThread().getName(), number);
                System.out.println(Thread.currentThread().getName() + " : Signalling that buffer is no more empty now");
                bufferNotFull.signalAll();
            }
        } finally {
            aLock.unlock();
        }
    }

    public void get() throws InterruptedException {
        aLock.lock();
        try {
            while (queue.size() == 0) {
                System.out.println(Thread.currentThread().getName() + " : Buffer is empty, waiting");
                bufferNotFull.await();
            }
            Integer value = (Integer) queue.poll();
            if (value != null) {
                System.out.printf("%s consumed %d from queue %n", Thread.currentThread().getName(), value); // signal producer thread that, buffer may be empty now
                System.out.println(Thread.currentThread().getName() + " : Signalling that buffer may be empty now");
                bufferNotEmpty.signalAll();
            }
        } finally {
            aLock.unlock();
        }
    }

}
