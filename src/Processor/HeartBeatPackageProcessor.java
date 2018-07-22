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


    private final Timer sendTimer;

    public HeartBeatPackageProcessor(String nodeId, Graph graph, List<Path> neighborPaths, long sendInterval)
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

        sendTimer = new Timer(true);
        // 定时每个 sendInterval 检查是否有结点超过三个间隔没有收到心跳包。有的话设置到对应边的长度为无穷。
        sendTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                synchronized (lastHeartBeatReceiveTimeLock)
                {
                    long timestampNow = System.currentTimeMillis();
                    Set<String> keys = lastHeartBeatReceiveTime.keySet();
                    for (String key : keys)
                    {
                        // 如果超时，删除路径
                        if (isTimeOut(lastHeartBeatReceiveTime.get(key), timestampNow))
                        {
                            graph.updatePath(new Path(nodeId, key, Graph.INF));
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
