package Component.Message;

import java.io.Serializable;

/**
 * 心跳包。用于网络传输。
 */
public class HeartBeatPackage implements Serializable
{
    // 发送者的 NodeId
    private final String senderNodeId;


    public HeartBeatPackage(String senderNodeId)
    {
        this.senderNodeId = senderNodeId;
    }

    public String getSenderNodeId()
    {
        return senderNodeId;
    }
}
