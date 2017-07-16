package atomicClasses;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by root on 4/22/17.
 */
public class Producer implements Runnable {

    Block_T threadBlock, stealBlock, stealPrev;
    boolean foundAdd;
    int threadHead, stealHead, stealIndex;
    int id;
    TestThreadLocal testThreadLocal;
    CyclicBarrier cyclicBarrier;


    Producer(TestThreadLocal testThreadLocal, int id, CyclicBarrier cyclicBarrier) {
        this.cyclicBarrier = cyclicBarrier;
        this.id = id;
        this.testThreadLocal = testThreadLocal;
        threadBlock = testThreadLocal.getThreadBlock(id);
        this.threadHead = 0;
        stealIndex = 0;
        stealBlock = null;
        stealPrev = null;
        stealHead = TestThreadLocal.BLOCK_SIZE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Block_T getThreadBlock() {
        return threadBlock;
    }

    public void setThreadBlock(Block_T threadBlock) {
        this.threadBlock = threadBlock;
    }

    public Block_T getStealBlock() {
        return stealBlock;
    }

    public void setStealBlock(Block_T stealBlock) {
        this.stealBlock = stealBlock;
    }

    public Block_T getStealPrev() {
        return stealPrev;
    }

    public void setStealPrev(Block_T stealPrev) {
        this.stealPrev = stealPrev;
    }

    public boolean isFoundAdd() {
        return foundAdd;
    }

    public void setFoundAdd(boolean foundAdd) {
        this.foundAdd = foundAdd;
    }

    public int getThreadHead() {
        return threadHead;
    }

    public void setThreadHead(int threadHead) {
        this.threadHead = threadHead;
    }

    public int getStealHead() {
        return stealHead;
    }

    public void setStealHead(int stealHead) {
        this.stealHead = stealHead;
    }

    public int getStealIndex() {
        return stealIndex;
    }

    public void setStealIndex(int stealIndex) {
        this.stealIndex = stealIndex;
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
        for (int i = 0; i < 12; i++) {
            try {
                testThreadLocal.add(i, this);
                System.out.println("Added item " + i + " for the thread " + id+"\n");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
/*
        for (int i = 0; i < 36; i++) {
            System.out.println("****Removed value is" + testThreadLocal.tryRemoveAny(this) + "****\n");
        }
*/
    }
}
