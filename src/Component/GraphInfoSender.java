package Component;

import Interface.TimingSender;
import Message.GraphInfo;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.*;

import static util.Broadcaster.*;

/**
 * 路径信息发送器，定时发送本结点的所有路径信息（一个 Path 对象 List）。
 */
public class GraphInfoSender implements TimingSender
{
    /**
     * 定时器。用来为发送路径信息设定定时任务。
     */
    private final Timer sendTimer;

    /**
     * 结点的图对象。
     */
    private final Graph graph;

    /**
     * 这个 sender 的发送间隔，单位为毫秒。
     */
    private final long sendInterval;

    /**
     * 这个结点向外发送数据的 socket。
     */
    private final DatagramSocket datagramSocket;

    /**
     * 所有邻居结点的端口。
     */
    private List<Integer> neighborPorts;


    /**
     * @param graph          要发送的图，也就是本进程的图。
     * @param datagramSocket 发送图使用的 socket。
     * @param neighborPorts  所有邻居结点的端口号。
     * @param sendInterval   发送路径信息的间隔。
     */
    public GraphInfoSender(Graph graph, DatagramSocket datagramSocket, List<Integer> neighborPorts, long sendInterval)
    {
        this.graph = graph;
        this.datagramSocket = datagramSocket;
        this.neighborPorts = neighborPorts;

        sendTimer = new Timer(true);
        this.sendInterval = sendInterval;
    }

    public void start()
    {
        // 定时把路径信息通过 socket 发送到所有邻居结点端口
        sendTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    synchronized (graph)
                    {
                        broadcast(new GraphInfo(graph), datagramSocket, neighborPorts);
                    }
                }
                catch (IOException e)
                {
                    System.err.println("图发送出现错误");
                    e.printStackTrace();
                }
            }
        }, 0, sendInterval);
    }

    public void stop()
    {
        sendTimer.cancel();
    }
}
