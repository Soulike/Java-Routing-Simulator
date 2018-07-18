package Processor;

import Message.HeartBeatPackage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static util.Broadcaster.*;

public class HeartBeatPackageSender
{
    private final Timer timer;

    private final String senderNodeId;

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
                    e.printStackTrace();
                }
            }
        }, 0, sendInterval);
    }

    public void stop()
    {
        timer.cancel();
    }
}
