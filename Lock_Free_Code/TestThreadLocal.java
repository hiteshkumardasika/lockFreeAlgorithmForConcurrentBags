package atomicClasses;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Created by root on 4/14/17.
 */
public class TestThreadLocal {

    public static int NR_THREADS = 1000;
    LinkedList<Block_T> globalThreadBlockList;
    public static int WORD_SIZE = 10;
    public static int BLOCK_SIZE = 10;

    public TestThreadLocal() {
        globalThreadBlockList = new LinkedList<>();
        //gloabalThreadBlock = new Block_T[NR_THREADS];
        //gloabalThreadBlock[0] = new Block_T();
        Block_T firstBlock = new Block_T();
        globalThreadBlockList.add(0, firstBlock);
        for (int i = 1; i < NR_THREADS; i++) {
            Block_T newBlock = new Block_T();
            Block_T nextBlock = globalThreadBlockList.get(i - 1);
            nextBlock.next = new BlockP_T(newBlock, true, true);
            globalThreadBlockList.add(i - 1, newBlock);
            globalThreadBlockList.add(i, newBlock);
        }
        globalThreadBlockList.get(NR_THREADS - 1).next = null;
    }

    Block_T getThreadBlock(int id) {
        return globalThreadBlockList.get(id);
    }

    public Block_T newBlock() {
        Block_T blockT = new Block_T();
        blockT.next = null;
        notifyAll(blockT);
        for (int i = 0; i < BLOCK_SIZE; i++) {
            blockT.nodes[i] = null;
        }
        return blockT;
    }

    public void mark1Block(Block_T block) {
        while (true) {
            BlockP_T oldBlock = block.next;
            AtomicReference<BlockP_T> blockP_tAtomicReference = new AtomicReference<>(oldBlock);
            if (oldBlock.p == null || oldBlock.mark1 ||
                    blockP_tAtomicReference.compareAndSet(block.next, new BlockP_T(oldBlock.p, oldBlock.mark2, true))) {
                break;
            }
        }
    }

    public void notifyAll(Block_T block_t) {
        for (int i = 0; i < NR_THREADS / WORD_SIZE; i++) {
            block_t.notifyAdd[i] = 0;
        }
    }

    public void notifyStart(Block_T block_t, int id) {
        AtomicLong old;
        long setValue;
        do {
            old = new AtomicLong(block_t.notifyAdd[id / WORD_SIZE]);
            long oldLong = old.get();
            setValue = oldLong | (1 << (id % WORD_SIZE));
        } while (!old.compareAndSet(block_t.notifyAdd[id / WORD_SIZE], setValue));
    }

    public boolean notifyCheck(Block_T block_t, int id) {
        return (block_t.notifyAdd[id / WORD_SIZE] & (1 << (id % WORD_SIZE))) == 0;
    }

    public Block_T nextStealBlock(Block_T block_t, Consumer consumer) {
        BlockP_T next = new BlockP_T();
        AtomicReference<Block_T> block_tAtomicReference = new AtomicReference<>(block_t);
        while (true) {
            if (block_t == null) {
                block_t = globalThreadBlockList.get(consumer.getStealIndex());
                break;
            }
            next = block_t.next;
            if (next.mark2)
                mark1Block(next.p);
            if (consumer.getStealPrev() == null || next.p == null) {
                if (next.mark1) {
                    if (next.p != null) {
                        notifyAll(next.p);
                    }
                    if (block_tAtomicReference.compareAndSet(globalThreadBlockList.get(consumer.getStealIndex()), next.p)) {
                        block_t.next = new BlockP_T(null, false, true);
                        block_t = null; //it is delete node here
                    } else {
                        consumer.setStealPrev(null);
                        block_t = globalThreadBlockList.get(consumer.getStealIndex());
                        continue;
                    }
                } else {
                    consumer.setStealPrev(block_t);
                }
            } else {
                if (next.mark1) {
                    BlockP_T prevNext = new BlockP_T(block_t, consumer.getStealPrev().next.mark2, false);
                    AtomicReference<BlockP_T> prevNextAtomic = new AtomicReference<>(prevNext);
                    if (prevNextAtomic.compareAndSet(consumer.getStealPrev().next, next)) {
                        block_t.next = new BlockP_T(null, false, true);
                        block_t = null;
                    } else {
                        consumer.setStealPrev(null);
                        block_t = globalThreadBlockList.get(consumer.getStealIndex());
                        continue;
                    }
                } else if (block_t == consumer.getStealBlock()) {
                    if (block_tAtomicReference.compareAndSet(consumer.getStealPrev().next, new BlockP_T(block_t, true, false))) {
                        mark1Block(block_t);
                        continue;
                    } else {
                        consumer.setStealPrev(null);
                        block_t = globalThreadBlockList.get(consumer.getStealIndex());
                        continue;
                    }
                } else {
                    consumer.setStealPrev(block_t);
                }
            }
            if (block_t == consumer.getStealBlock() || next.p == consumer.getStealBlock()) {
                block_t = next.p;
                break;
            }
            block_t = next.p;
        }
        return block_t;
    }

    public Integer tryStealBlock(int round, Consumer consumer) {
        int head = consumer.getStealHead();
        Block_T stealBlock = consumer.getStealBlock();
        consumer.setFoundAdd(false);
        if (stealBlock == null) {
            stealBlock = globalThreadBlockList.get(consumer.getStealIndex());
            consumer.setStealBlock(stealBlock);
            consumer.setStealHead(0);
            head = 0;
        }
        if (head == BLOCK_SIZE) {
            stealBlock = nextStealBlock(stealBlock, consumer);
            consumer.setStealBlock(nextStealBlock(stealBlock, consumer));
            head = 0;
        }
        if (stealBlock == null) {
            int stealIndex = consumer.getStealIndex();
            consumer.setStealIndex((stealIndex + 1) % NR_THREADS);
            consumer.setStealIndex(0);
            consumer.setStealBlock(null);
            consumer.setStealPrev(null);
        }
        if (round == 1) {
            notifyStart(stealBlock, (int) consumer.getId());
        } else if (round > 1 && notifyCheck(stealBlock, (int) consumer.getId())) {
            consumer.setFoundAdd(true);
        }
        while (true) {
            if (head == BLOCK_SIZE) {
                consumer.setStealHead(head);
                return null;
            } else {
                Node_t node = stealBlock.nodes[head];
                if (node == null) {
                    head++;
                    continue;
                }
                AtomicInteger atomicData = new AtomicInteger(node.data);
                if (atomicData.compareAndSet(stealBlock.nodes[head].data, Integer.MAX_VALUE)) {
                    return atomicData.get();
                }
            }
        }
    }


    public int tryRemoveAny(Consumer consumer) {
        int head = consumer.getThreadHead() - 1;
        Block_T block = consumer.getThreadBlock();
        int round = 0;
        while (true) {
            if (block == null || (head < 0 && block.next.p == null)) {
                do {
                    int i = 0;
                    do {
                        Integer stolenBlock = tryStealBlock(round, consumer);
                        if (stolenBlock != null) {
                            return stolenBlock;
                        } else if (stolenBlock == null) {
                            i++;
                        }
                        if (consumer.foundAdd) {
                            round = 0;
                            i = 0;
                        }
                    } while (i < NR_THREADS);
                } while (++round <= NR_THREADS);
            }
            if (head < 0) {
                //System.out.println("or here\n");
                //Time to remove this block
                mark1Block(block);
                AtomicReference<Block_T> block_tAtomicReference = new AtomicReference<>(block);
                while (true) {
                    BlockP_T delBlock = block.next;
                    if (delBlock.mark2)
                        mark1Block(delBlock.p);
                    if (delBlock.mark1) {
                        if (delBlock.p != null) {
                            notifyAll(delBlock.p);
                        }
                        if (block_tAtomicReference.compareAndSet(globalThreadBlockList.get(consumer.getId()), globalThreadBlockList.set(consumer.getId(), delBlock.p))) {
                            block = delBlock.p; //ideally this indicates this node is garbage collected
                            block.next = new BlockP_T(null, false, true);
                        } else {
                            block = globalThreadBlockList.get(consumer.getId());
                        }
                        break;
                    } else
                        break;
                }
                consumer.setThreadBlock(block);
                consumer.setThreadHead(BLOCK_SIZE);
                head = BLOCK_SIZE - 1;
            } else {
                Node_t node = block.nodes[head];
                if (node == null) {
                    head--;
                    continue;
                }
                AtomicInteger atomicData = new AtomicInteger(node.data);
                if (atomicData.compareAndSet(block.nodes[head].data, node.data)) {
                    consumer.setThreadHead(head);
                    return atomicData.get();
                }
            }
        }
    }

    void add(int item, Producer producer) {
        int head = producer.getThreadHead();
        Block_T block = globalThreadBlockList.get(producer.getId());
        while (true) {
            if (head == BLOCK_SIZE) {
                Block_T newBlock = new Block_T();
                newBlock.next = new BlockP_T(block, true, true);
                globalThreadBlockList.set(producer.getId(), newBlock);
                producer.setThreadBlock(newBlock);
                producer.setThreadHead(0);
                head = 0;
                block = newBlock;
            } else if (block.nodes[head] == null) {
                notifyAll(block);
                Node_t newNode = new Node_t(item);
                block.nodes[head] = newNode;
                producer.setThreadHead(++head);
                return;
            } else head++;
        }
    }
}

class Node_t {
    Integer data;

    Node_t() {
    }

    Node_t(int data) {
        this.data = new Integer(data);
    }
}

class Block_T extends Node_t {
    Node_t nodes[];
    BlockP_T next;
    long notifyAdd[];

    Block_T() {
        nodes = new Node_t[TestThreadLocal.BLOCK_SIZE];
        this.notifyAdd = new long[TestThreadLocal.NR_THREADS / TestThreadLocal.WORD_SIZE];
    }
}

class BlockP_T extends Block_T {
    Block_T p;
    boolean mark1 = true;
    boolean mark2 = true;

    public BlockP_T() {
    }

    BlockP_T(Block_T p) {
        this.p = p;
    }

    BlockP_T(Block_T p, boolean mark1, boolean mark2) {
        this.p = p;
        this.mark1 = mark1;
        this.mark2 = mark2;
    }
}