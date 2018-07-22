package Component.Processor;

import Component.ThreadPool.Processor;
import Component.Message.NeighborNodeInfo;
import Component.Graph.Graph;

import static util.Broadcaster.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.*;

public class NeighborNodeInfoProcessor implements Processor
{
    // 这个结点的图
    private final Graph graph;

    //这个结点向外发送数据的 socket
    private final DatagramSocket datagramSocket;

    // 所有邻居结点的端口
    private final List<Integer> neighborPorts;

    // 已经处理过的包的时间戳列表
    private final List<Long> processedInfoTimestamps;

    private final byte[] processedInfoTimestampsLock = new byte[0];

    private final Timer timer;

    public NeighborNodeInfoProcessor(Graph graph, DatagramSocket datagramSocket)
    {
        this.graph = graph;
        this.datagramSocket = datagramSocket;
        this.neighborPorts = new LinkedList<>();
        this.processedInfoTimestamps = new LinkedList<>();

        // 创建一个计时任务，每一分钟清理 processedInfoTimestamps 当中大于当前十分钟的时间戳。不大可能收到十分钟以前的重复广播包。
        this.timer = new Timer(true);
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                final long timestampNow = System.currentTimeMillis();
                final List<Long> timestampToRemove = new ArrayList<>();

                synchronized (processedInfoTimestampsLock)
                {
                    for (long timestamp : processedInfoTimestamps)
                    {
                        if (longerThanTenMins(timestamp, timestampNow))
                        {
                            timestampToRemove.add(timestamp);
                        }
                    }

                    processedInfoTimestamps.removeAll(timestampToRemove);
                }
            }
        }, 0, 60 * 1000);
    }

    /**
     * 判断两个时间戳间隔是否大于十分钟。
     */
    private static boolean longerThanTenMins(long timestamp1, long timestamp2)
    {
        return timestamp2 - timestamp1 > 10 * 60 * 1000;
    }

    /**
     * 检测包是否是重复收到的。
     */
    private boolean hasProcessed(NeighborNodeInfo info)
    {
        return processedInfoTimestamps.contains(info.getSendTime());
    }

    public void process(Object object) throws IOException
    {
        NeighborNodeInfo info = (NeighborNodeInfo) object;
        // 如果这个广播包是第一次收到，就进行相应处理
        if (!hasProcessed(info))
        {
            synchronized (processedInfoTimestampsLock)
            {
                processedInfoTimestamps.add(info.getSendTime());
            }
            graph.updatePaths(info.getPathList());
            broadcast(info, datagramSocket, neighborPorts);
        }
    }
}
