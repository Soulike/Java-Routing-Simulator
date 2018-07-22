package Component.Message;

import java.io.Serializable;

/**
 * 心跳包。用于网络传输。
 */
public class HeartBeatPackage implements Serializable
{
    /**
     * 发送者的 NodeId。
     */
    private final String senderNodeId;
    /**
     * 被发送时的时间戳。用于检测该包是否被重复接收。
     */
    private final long sendTime;

    public HeartBeatPackage(String senderNodeId, long sendTime)
    {
        this.senderNodeId = senderNodeId;
        this.sendTime = sendTime;
    }

    public String getSenderNodeId()
    {
        return senderNodeId;
    }

    public long getSendTime()
    {
        return sendTime;
    }
}
