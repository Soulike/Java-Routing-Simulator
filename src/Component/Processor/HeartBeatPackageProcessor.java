package Component.Processor;

import Component.ThreadPool.Processor;
import Component.Message.HeartBeatPackage;
import Component.Graph.Graph;
import Component.Graph.Path;

import java.util.*;

public class HeartBeatPackageProcessor implements Processor
{
    // 心跳包的的发送间隔，单位为毫秒。
    private final long sendInterval;

    // 最后收到 key 的心跳包的时间戳。
    private final HashMap<String, Long> lastHeartBeatReceiveTime;

    private final Object lastHeartBeatReceiveTimeLock = new Object();

    private final Timer sendTimer;

    public HeartBeatPackageProcessor(String nodeId, Graph graph, List<Path> neighborPaths, long sendInterval)
    {
        this.sendInterval = sendInterval;
        lastHeartBeatReceiveTime = new HashMap<>();

        synchronized (lastHeartBeatReceiveTimeLock)
        {
            for (Path path : neighborPaths)
            {
                lastHeartBeatReceiveTime.put(path.getEndNodeId(), System.currentTimeMillis());
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
                    Set<String> keys = lastHeartBeatReceiveTime.keySet();
                    for (String key : keys)
                    {
                        // 如果超时，删除路径
                        if (isTimeOut(lastHeartBeatReceiveTime.get(key), System.currentTimeMillis()))
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
        synchronized (lastHeartBeatReceiveTimeLock)
        {
            lastHeartBeatReceiveTime.put(senderNodeId, System.currentTimeMillis());
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
