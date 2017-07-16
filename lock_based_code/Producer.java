package pcthreads;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

/**
 * Created by root on 4/25/17.
 */
class Producer extends Thread {

    ProducerConsumerImpl pc;
    CyclicBarrier cyclicBarrier;

    public Producer(ProducerConsumerImpl sharedObject, CyclicBarrier cyclicBarrier) {
        super("PRODUCER");
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
            pc.put();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

