package Component.ThreadPool;

/**
 * 线程类，继承 Thread，以实现对每一个新连接创建一个新线程。
 *
 * @author soulike
 */
public class ThreadService extends Thread
{
    /**
     * 需要被处理的对象。
     */
    private Object objNeedsProcess;


    /**
     * 对这个连接进行处理的服务程序。
     */
    private Processor processor;

    /**
     * 是否被要求退出。true-是，false-否。
     */
    private boolean exit;


    /**
     * 构造函数，用于创建一个等待线程。
     */
    public ThreadService()
    {
        this(null, null);
    }


    /**
     * 用于在线程池内没有空余线程时直接创建新线程。
     *
     * @param objNeedsProcess 被处理对象。
     * @param processor       对这个对象进行处理的服务程序，实现 Processor 接口。
     */
    public ThreadService(Object objNeedsProcess, Processor processor)
    {
        this.objNeedsProcess = objNeedsProcess;
        this.processor = processor;
        exit = false;
    }

    /**
     * 判断这个线程是不是正在等待任务。
     */
    public boolean isWaiting()
    {
        return (objNeedsProcess == null && processor == null && !exit && Thread.currentThread().isAlive());
    }

    /**
     * 判断这个线程是不是已经有任务并正在运行。
     */
    public boolean isRunning()
    {
        return objNeedsProcess != null && processor != null && Thread.currentThread().isAlive();
    }

    /**
     * 把这个线程重新变为阻塞状态。
     */
    private synchronized void toWaiting()
    {
        objNeedsProcess = null;
        processor = null;
    }

    /**
     * 通知这个线程退出执行。
     */
    public synchronized void setExit()
    {
        exit = true;
        notify();
    }

    /**
     * 检查这个线程是不是已经被要求退出或已经退出了。
     */
    public boolean isExit()
    {
        return exit || !Thread.currentThread().isAlive();
    }


    /**
     * 分配对象并启用线程。
     *
     * @param objNeedsProcess 被处理对象。
     * @param processor       对这个对象进行处理的服务程序，实现 Processor 接口。
     */
    public synchronized void runThreadService(Object objNeedsProcess, Processor processor)
    {
        this.objNeedsProcess = objNeedsProcess;
        this.processor = processor;
        notify();// 内容分配好了，唤醒线程
    }

    /**
     * 在执行线程之后，把对象交给对应处理器处理。
     */
    public synchronized void run()
    {
        try
        {
            // 当不要求这个线程退出时再进行循环
            while (!exit)
            {
                // 如果状态为 false，就阻塞这个线程
                while (isWaiting() && !exit)
                {
                    wait();
                }

                // 当不要求这个线程退出时再进行操作
                if (!exit)
                {
                    processor.process(objNeedsProcess);
                    toWaiting();
                }
            }
        }
        catch (Exception e)
        {
            System.err.println(String.format("线程 %s 处理请求时发生错误", Thread.currentThread().getName()));
            e.printStackTrace();
            toWaiting();
        }
    }
}

