package com.example;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;

/** Task57
 *  Есть 3 производителя и 2 потребителя, все разные потоки и работают все одновременно. Есть очередь с 200 элементами.
 *  Производители добавляют случайное число от 1..100, а потребители берут эти числа.
 *  Если в очереди элементов >= 100 производители спят, если нет элементов в очереди – потребители спят.
 *  Если элементов стало <= 80, производители просыпаются. Все это работает до тех пор, пока обработанных элементов не станет 10000,
 *  только потом программа завершается.
 */

public class App {

    static final int maxHandledElements = 10000;

    /**
     * @brief Producer 
     */
    static class Producer implements Runnable
    {
        Producer(BlockingQueue<Integer> queue, AtomicInteger totalhandledElements)
        {
            this._queue = queue;
            this._totalhandledElements = totalhandledElements;
        }
        
        BlockingQueue<Integer> _queue;
        AtomicInteger _totalhandledElements;

        /* Producer thread */
        @Override
        public void run()
        {
            while(_totalhandledElements.get() < maxHandledElements)
            {
                try 
                {
                    if(_queue.size() > 100)
                    {
                        synchronized(_queue)
                        {
                            System.out.println(Thread.currentThread().getName() + " is sleeping... " + "(Handled: " + _totalhandledElements.get() + ")");
                            _queue.wait();
                        }        
                    }
                    else
                    {
                        _queue.put(getRandomInt());
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(Thread.currentThread().getName() + " finished.");
        }
            
        /* Get random integer */
        private int getRandomInt()
        {
            return (int)(Math.random() * 100);
        }
    }

    /**
     * @brief Consumer 
     */
    static class Consumer implements Runnable
    {
        BlockingQueue<Integer> _queue;
        AtomicInteger _totalhandledElements;

        Consumer(BlockingQueue<Integer> queue, AtomicInteger totalhandledElements)
        {
            this._queue = queue;
            this._totalhandledElements = totalhandledElements;
        }

        /* Consumer thread */
        @Override
        public void run()
        {
            while(_totalhandledElements.get() < maxHandledElements)
            {
                try 
                {
                    if(_queue.size() <= 80)
                    {
                        synchronized(_queue)
                        {
                            _queue.notify();
                        }
                    }
                    else
                    {
                        _queue.take();
                        if(_totalhandledElements.get() < maxHandledElements)
                        {
                            _totalhandledElements.incrementAndGet();
                        }
                    }
                }
                catch (InterruptedException e) 
                {
                    e.printStackTrace();
                }
            }

            System.out.println(Thread.currentThread().getName() + " finished.");
            
            synchronized(_queue)
            {
                _queue.notifyAll();
            }
        }
    }

    public static void main(String[] args) 
    {
        /* Definitions: */
        final int queueDepth = 200;

        /* Common variables for all threads: */
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(queueDepth, true);
        AtomicInteger totalhandledElements = new AtomicInteger();

        Thread prodThread1 = new Thread(new Producer(queue, totalhandledElements), "Producer 1");
        Thread prodThread2 = new Thread(new Producer(queue, totalhandledElements), "Producer 2");
        Thread prodThread3 = new Thread(new Producer(queue, totalhandledElements), "Producer 3");

        Thread consThread1 = new Thread(new Consumer(queue, totalhandledElements), "Consumer 1");
        Thread consThread2 = new Thread(new Consumer(queue, totalhandledElements), "Consumer 2");

        try
        {
            prodThread1.start();
            prodThread2.start();
            prodThread3.start();
    
            consThread1.start();
            consThread2.start();
        }
        catch (NullPointerException e)
        {
            System.out.println(e.getMessage());
        }

        /* Wait in the main() until each thread is finished */
        try {
            prodThread1.join();
            prodThread2.join();
            prodThread3.join();

            consThread1.join();
            consThread2.join();
        } 
        catch(InterruptedException | IllegalMonitorStateException e)
        {
            System.out.println(e.toString());
        }

        System.out.println(totalhandledElements + " of " + maxHandledElements + " handled");
        System.out.println("exit...");
    }
}