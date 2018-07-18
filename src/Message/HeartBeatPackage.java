package Message;

import java.io.Serializable;

public class HeartBeatPackage implements Serializable
{
    private final String senderNodeId;
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
