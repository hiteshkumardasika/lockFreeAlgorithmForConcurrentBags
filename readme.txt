How To Execute:

The lock_Free_Code folder is where the algorithm is implemented.
 -MyThread.java Contains the main method which is the starting point for execution
 -The code for algorithm is in TestThreadLocal.java file.
 - There are two classes producer and consumer which play their respective roles

The lock_based_Code folder is where the lock based algorithm is written to compare it with the lock free algorithm. I have taken part of the code from (https://crunchify.com/java-producer-consumer-example-handle-concurrent-read-write/) and modified it to my requirements.

For Benchmarking, i made use of the YourKit java thread profiler tool and all the graphs i have generated are with the help of this tool.
