package Component.Sender;

import Component.Message.HeartBeatPackage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.*;

import static util.Broadcaster.*;

/**
 * 心跳包发送器。每隔一段时间发送一个心跳包。
 */
public class HeartBeatPackageSender implements TimingSender
{
    private final Timer timer;

    // 发送者的 NodeId
    private final String senderNodeId;

    // 这个 sender 的发送间隔，单位为毫秒
    private final long sendInterval;

    // 这个结点向外发送数据的 socket。
    private final DatagramSocket datagramSocket;

    // 所有邻居结点的端口
    private List<Integer> neighborPorts;

    /**
     * @param senderNodeId   发送者的 NodeId。
     * @param datagramSocket 发送图使用的 socket。
     * @param neighborPorts  所有邻居结点的端口号。
     * @param sendInterval   发送路径信息的间隔。
     */
    public HeartBeatPackageSender(String senderNodeId, DatagramSocket datagramSocket, List<Integer> neighborPorts, long sendInterval)
    {
        this.senderNodeId = senderNodeId;
        this.datagramSocket = datagramSocket;
        this.neighborPorts = neighborPorts;
        timer = new Timer(true);
        this.sendInterval = sendInterval;
    }

    public void start()
    {
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    broadcast(new HeartBeatPackage(senderNodeId, System.currentTimeMillis()), datagramSocket, neighborPorts);
                }
                catch (IOException e)
                {
                    System.err.println("心跳包发送出现错误");
                    System.err.println(e.getLocalizedMessage());
                }
            }
        }, 0, sendInterval);
    }

    public void stop()
    {
        timer.cancel();
    }
}
