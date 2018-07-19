package Processor;

import Interface.MessageProcessor;
import Message.HeartBeatPackage;
import Component.Graph;
import Objects.Path;

import java.util.*;

public class HeartBeatPackageProcessor implements MessageProcessor
{
    /**
     * 心跳包的的发送间隔，单位为毫秒。
     */
    private final long sendInterval;

    /**
     * 最后收到 key 的心跳包的时间戳。
     */
    private final HashMap<String, Long> lastHeartBeatReceiveTime;

    private final Object lastHeartBeatReceiveTimeLock = new Object();


    private final Timer timer;

    public HeartBeatPackageProcessor(Graph graph, List<Path> neighborPaths, long sendInterval)
    {
        this.sendInterval = sendInterval;
        lastHeartBeatReceiveTime = new HashMap<>();

        long timestampNow = System.currentTimeMillis();
        synchronized (lastHeartBeatReceiveTimeLock)
        {
            for (Path path : neighborPaths)
            {
                lastHeartBeatReceiveTime.put(path.getEndNodeId(), timestampNow);
            }
        }

        timer = new Timer(true);
        // 定时每个 sendInterval 检查是否有结点超过三个间隔没有收到心跳包。有的话从图中删掉结点。
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                long timestampNow = System.currentTimeMillis();
                synchronized (lastHeartBeatReceiveTimeLock)
                {
                    synchronized (graph)
                    {
                        Set<String> keys = lastHeartBeatReceiveTime.keySet();
                        for (String key : keys)
                        {
                            if (isTimeOut(lastHeartBeatReceiveTime.get(key), timestampNow))
                            {
                                if (graph.hasNode(key))
                                {
                                    graph.removeNode(key);
                                }
                            }
                        }
                    }
                }
            }
        }, 0, sendInterval);
    }

    public void process(Object object)
    {
        HeartBeatPackage heartBeatPackage = (HeartBeatPackage) object;
        String senderNodeId = heartBeatPackage.getSenderNodeId();
        long sendTime = heartBeatPackage.getSendTime();
        synchronized (lastHeartBeatReceiveTimeLock)
        {
            lastHeartBeatReceiveTime.put(senderNodeId, sendTime);
        }
    }

    /**
     * 检查两个时间戳之间的间隔是否超过了三个发送间隔
     */
    private boolean isTimeOut(long timestamp1, long timestamp2)
    {
        return timestamp2 - timestamp1 > 3 * sendInterval;
    }

}
