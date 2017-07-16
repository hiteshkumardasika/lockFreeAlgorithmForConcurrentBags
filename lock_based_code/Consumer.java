package pcthreads;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

/**
 * Created by root on 4/25/17.
 */
class Consumer extends Thread {

    ProducerConsumerImpl pc;
    CyclicBarrier cyclicBarrier;

    public Consumer(ProducerConsumerImpl sharedObject, CyclicBarrier cyclicBarrier) {
        super("CONSUMER");
        this.cyclicBarrier = cyclicBarrier;
        this.pc = sharedObject;
    }

    @Override
    public void run() {
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        try {
            pc.get();
        } catch (InterruptedException e) { // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
