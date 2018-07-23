package Component.ThreadPool;

import Component.Graph.Pair;

import java.util.*;

/**
 * 线程池。在创建后预先创建指定数量的线程等待处理。
 *
 * @author soulike
 */
public class ThreadPool
{
    private int minThreadNum;
    private int maxThreadNum;

    // 当池中线程数达到这个数值时，执行清理程序
    private final int threadCleanLevel;

    // 任务等待队列的极限大小
    private final int waitingWorkQueueSize;

    // 任务等待队列
    private final List<Pair<Object, Processor>> waitingWorkQueue;

    //等待线程队列，此队列中线程都处于阻塞状态等待分配。
    private final List<ThreadService> waitingThreadList;

    //运行线程队列，此队列中线程都在处理用户连接。
    private final List<ThreadService> runningThreadList;

    private final byte[] waitingThreadListLock = new byte[0];
    private final byte[] runningThreadListLock = new byte[0];
    private final byte[] waitingWorkQueueLock = new byte[0];


    public ThreadPool()
    {
        this(50, 200);
    }


    public ThreadPool(int minThreadNum, int maxThreadNum)
    {
        this(minThreadNum, maxThreadNum, 0.75, 0.5);
    }

    /**
     * 完全版本构造函数。
     * 限定线程池的线程数目限制。如果传入负值，将会忽略并使用默认值。
     * 如果最小值大于最大值，将会对调两个值。
     *
     * @param minThreadNum         最小线程数。当可用线程数小于这个数时将向线程池中追加新线程。
     * @param maxThreadNum         最大线程数。当可用线程数大于这个数时将任务放入等待队列。
     * @param autoCleanRate        自动清理比例。当线程池中线程数量大于 (maxThreadNum-minThreadNum)*autoCleanRate 时，将会尝试关闭空闲线程。
     * @param waitingQueueSizeRate 等待队列大小比例。当等待的任务大于 (maxThreadNum-minThreadNum)*waitingQueueSizeRate 时，将拒绝服务。
     */
    public ThreadPool(int minThreadNum, int maxThreadNum, double autoCleanRate, double waitingQueueSizeRate)
    {
        if (minThreadNum < 0)
        {
            minThreadNum = 50;
        }
        if (maxThreadNum < 0)
        {
            maxThreadNum = 200;
        }

        if (maxThreadNum < minThreadNum)
        {
            int temp = maxThreadNum;
            maxThreadNum = minThreadNum;
            minThreadNum = temp;
        }

        this.minThreadNum = minThreadNum;
        this.maxThreadNum = maxThreadNum;
        this.threadCleanLevel = (int) Math.round((maxThreadNum - minThreadNum) * autoCleanRate) + minThreadNum;
        this.waitingWorkQueueSize = (int) Math.round((maxThreadNum - minThreadNum) * waitingQueueSizeRate) + minThreadNum;
        waitingThreadList = new LinkedList<>();
        runningThreadList = new LinkedList<>();
        waitingWorkQueue = new LinkedList<>();

        // 启动进程池管理线程
        ThreadPoolManager poolManager = new ThreadPoolManager();
        new Thread(poolManager).start();
    }

    /**
     * 当线程池中还有线程活着时，就阻塞主线程。
     * 在执行这个方法之后，线程池将不再接受新任务直到所有线程退出。
     * 用于不会使主线程阻塞的监听任务。
     */
    public synchronized void join()
    {
        try
        {
            while (getCurrentThreadNum() > 0)
            {
                wait();
            }
        }
        catch (InterruptedException e)
        {
            System.err.println("等待子线程结束时发生错误");
            System.err.println(e.getLocalizedMessage());
        }
    }

    /**
     * 从等待线程队列中找一个可运行的线程。如果都在运行，就创建一个新线程并返回。
     * 此函数无需处理线程在两个队列中切换的问题。
     */
    private ThreadService getRunnableThread()
    {
        // 遍历等待线程列表，找到一个还活着的线程返回
        synchronized (waitingThreadListLock)
        {
            for (ThreadService threadService : waitingThreadList)
            {
                if (threadService.isAlive())
                {
                    return threadService;
                }
            }

            // 如果都死了或者是等待线程列表为空
            // 如果线程池还没有到极限大小，就申请一个新线程返回
            if (getCurrentThreadNum() < maxThreadNum)
            {
                ThreadService newThread = new ThreadService();
                newThread.start();
                waitingThreadList.add(newThread);
                return newThread;
            }
            // 如果到了极限大小，就只能返回 null
            else
            {
                return null;
            }
        }
    }

    /**
     * 从等待线程队列中找一个空闲线程创建一个服务线程，并将其从等待线程队列转移到运行线程队列。
     *
     * @param objNeedsProcess 被处理对象。
     * @param processor       对这个对象进行处理的服务程序，实现 Processor 接口。
     */
    public void createThread(Object objNeedsProcess, Processor processor)
    {
        synchronized (waitingThreadListLock)
        {
            synchronized (runningThreadListLock)
            {
                ThreadService threadService = getRunnableThread();

                // 如果没有可用线程了
                if (threadService == null)
                {
                    synchronized (waitingWorkQueueLock)
                    {
                        // 如果等待任务队列都满了，就只能丢弃任务
                        if (isWaitingWorkQueueFull())
                        {
                            System.out.println("警告：线程池已满，拒绝创建新线程");
                        }
                        // 没有满，就放进队列尾部
                        else
                        {
                            addWaitingWork(new Pair<>(objNeedsProcess, processor));
                        }
                    }
                }
                // 还有可用线程，就直接运行
                else
                {
                    waitingThreadList.remove(threadService);
                    runningThreadList.add(threadService);
                    threadService.runThreadService(objNeedsProcess, processor);
                }
            }
        }
    }

    private Pair<Object, Processor> getWaitingWork()
    {
        synchronized (waitingWorkQueueLock)
        {
            if (!waitingWorkQueue.isEmpty())
            {
                Pair<Object, Processor> work = waitingWorkQueue.get(0);
                waitingWorkQueue.remove(work);
                return work;
            }
            else
            {
                return null;
            }
        }
    }

    private boolean hasWaitingWork()
    {
        return !waitingWorkQueue.isEmpty();
    }

    private boolean isWaitingWorkQueueFull()
    {
        return waitingWorkQueue.size() == waitingWorkQueueSize;
    }

    private void addWaitingWork(Pair<Object, Processor> work)
    {
        synchronized (waitingWorkQueueLock)
        {
            waitingWorkQueue.add(work);
        }
    }


    /**
     * 得到现在所有线程的数量。
     */
    public int getCurrentThreadNum()
    {
        return waitingThreadList.size() + runningThreadList.size();
    }

    /**
     * 得到现在正在运行线程的数量。
     */
    public int getCurrentRunningThreadNum()
    {
        return runningThreadList.size();
    }

    /**
     * 得到现在正在等待线程的数量。
     */
    public int getCurrentWaitingThreadNum()
    {
        return waitingThreadList.size();
    }

    public int getWaitingWorkQueueSize()
    {
        return waitingWorkQueue.size();
    }

    /**
     * 线程池管理类，开启另一个线程定时管理线程池。
     */
    class ThreadPoolManager implements Runnable
    {
        private final Timer timer = new Timer(true);

        /**
         * 当进程池进程数超出范围时进行干预。
         * 如果线程数量不够，就往等待队列里面添加新的。
         * 如果线程数量过多，从等待队列中关闭指定个线程。
         */
        private void checkPoolRange()
        {
            synchronized (waitingThreadListLock)
            {
                synchronized (runningThreadListLock)
                {
                    // 如果线程数量不够，就往等待队列里面添加新的
                    if (getCurrentThreadNum() < minThreadNum)
                    {
                        addThreadToMinNum();
                    }

                    // 如果线程数量过多，从等待队列中关闭指定个线程
                    if (getCurrentThreadNum() > threadCleanLevel)
                    {
                        closeThreads();
                    }
                }
            }
        }

        /**
         * 整理进程池。
         * 清理要求退出和死亡的线程。
         * 移动线程到正确队列。
         */
        private void rearrangePool()
        {
            synchronized (waitingWorkQueueLock)
            {
                synchronized (runningThreadListLock)
                {
                    // 清理要求退出和死亡的线程
                    cleanPool();
                    // 移动线程到正确队列
                    moveThread();
                }
            }
        }


        /**
         * 把运行队列中等待的线程移动到等待队列。
         */
        private void moveThread()
        {
            List<ThreadService> threadToMoveList = new ArrayList<>();
            // 找到所有正在等待任务的线程
            synchronized (waitingThreadListLock)
            {
                synchronized (runningThreadListLock)
                {
                    for (ThreadService threadService : runningThreadList)
                    {
                        if (threadService.isWaiting())
                        {
                            threadToMoveList.add(threadService);
                        }
                    }

                    // 从运行队列中删掉它们
                    runningThreadList.removeAll(threadToMoveList);
                }
                // 添加到等待队列里
                waitingThreadList.addAll(threadToMoveList);
            }
        }

        /**
         * 清理池中被要求退出或已经死亡的线程。
         */
        private void cleanPool()
        {
            synchronized (waitingThreadListLock)
            {
                cleanList(waitingThreadList);
            }
            synchronized (runningThreadListLock)
            {
                cleanList(runningThreadList);

            }
        }

        /**
         * 清理一个队列中被要求退出或已经死亡的线程。
         *
         * @param threadServiceList 一个线程队列。
         */
        private synchronized void cleanList(List<ThreadService> threadServiceList)
        {
            List<ThreadService> exitThreads = new ArrayList<>();
            for (ThreadService threadService : threadServiceList)
            {
                if (threadService.isExit())
                {
                    exitThreads.add(threadService);
                }
            }

            for (ThreadService threadService : exitThreads)
            {
                threadServiceList.remove(threadService);
            }

            notify();
        }

        /**
         * 在等待队列中添加线程，直到达到最小限制。
         */
        private void addThreadToMinNum()
        {
            synchronized (waitingThreadListLock)
            {
                ThreadService threadServiceTemp;
                while (getCurrentThreadNum() < minThreadNum)
                {
                    threadServiceTemp = new ThreadService();
                    threadServiceTemp.start();
                    waitingThreadList.add(threadServiceTemp);
                }
            }
        }

        /**
         * 在等待队列中关闭线程，直到达到建议值。
         */
        private void closeThreads()
        {
            synchronized (waitingThreadListLock)
            {
                int exitThreadNum = 0;
                int threadToExitNum = getCurrentThreadNum() - threadCleanLevel;
                List<ThreadService> exitThread = new ArrayList<>();

                for (ThreadService threadService : waitingThreadList)
                {
                    exitThread.add(threadService);
                    exitThreadNum++;
                    if (exitThreadNum == threadToExitNum)
                    {
                        break;
                    }
                }

                for (ThreadService threadService : exitThread)
                {
                    threadService.setExit();
                }
            }
        }

        /**
         * 输出当前池的状态。
         */
        private void printPoolStatus()
        {
            System.out.println(String.format("目前线程池线程数量: %d\n目前线程池等待线程数量: %d\n目前线程池运行线程数量: %d\n目前等待队列大小: %d\n", getCurrentThreadNum(), getCurrentWaitingThreadNum(), getCurrentRunningThreadNum(), getWaitingWorkQueueSize()));
        }

        /**
         * 从运行线程队列中找到已经空闲的线程并直接分配等待任务队列里的任务。
         * 如果没有空闲线程，则尝试从等待线程队列中拿出线程进行运行。
         */
        private void runWaitingWork()
        {
            synchronized (waitingThreadListLock)
            {
                synchronized (runningThreadListLock)
                {
                    synchronized (waitingWorkQueueLock)
                    {
                        List<ThreadService> waitingThreadServices = new LinkedList<>();
                        Pair<Object, Processor> work;
                        // 先看看在运行线程队列里面有没有已经空闲下来的
                        for (ThreadService thread : runningThreadList)
                        {
                            if (thread.isWaiting())
                            {
                                waitingThreadServices.add(thread);
                            }
                        }
                        // 如果有直接分配任务
                        if (!waitingThreadServices.isEmpty())
                        {
                            for (ThreadService thread : waitingThreadServices)
                            {
                                work = getWaitingWork();
                                if (work == null)
                                {
                                    break;
                                }
                                else
                                {
                                    thread.runThreadService(work.getFirst(), work.getSecond());
                                }
                            }
                        }
                        // 如果没有，就尝试从等待线程队列获得可用线程
                        else
                        {
                            ThreadService thread = getRunnableThread();
                            while (thread != null && !waitingWorkQueue.isEmpty())
                            {
                                work = getWaitingWork();
                                thread.runThreadService(work.getFirst(), work.getSecond());
                                waitingThreadList.remove(thread);
                                runningThreadList.add(thread);
                                thread = getRunnableThread();
                            }
                        }
                    }
                }
            }
        }

        /**
         * 调用 Timer 运行定时整理任务。
         */

        public void run()
        {
            /*// 每 5 秒输出一次线程池状态
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    printPoolStatus();
                }
            }, 0, 5000);*/

            // 每 100 毫秒执行队列中的任务
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    synchronized (waitingThreadListLock)
                    {
                        synchronized (runningThreadListLock)
                        {
                            synchronized (waitingWorkQueueLock)
                            {
                                runWaitingWork();
                            }
                        }
                    }
                }
            }, 0, 100);

            // 每 250 毫秒执行一次线程池整理
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    synchronized (waitingThreadListLock)
                    {
                        synchronized (runningThreadListLock)
                        {
                            checkPoolRange();
                            rearrangePool();
                        }
                    }
                }
            }, 0, 250);
        }
    }
}



