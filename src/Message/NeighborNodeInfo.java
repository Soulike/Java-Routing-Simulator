package Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Object.Path;

/**
 * 广播使用邻居结点信息对象。
 */
public class NeighborNodeInfo implements Serializable
{
    /**
     * 该包发出时的时间戳。
     */
    private final long sendTime;
    /**
     * 发送结点的标识。
     */
    private final String senderNodeId;
    /**
     * 与该结点所有相邻的结点的路径信息。
     */
    private final List<Path> pathList;

    public NeighborNodeInfo(String senderNodeId, List<Path> pathList)
    {
        this.sendTime = System.currentTimeMillis();
        this.senderNodeId = senderNodeId;
        this.pathList = pathList;
    }

    public String getSenderNodeId()
    {
        return senderNodeId;
    }

    public List<Path> getPathList()
    {
        return new ArrayList<>(pathList);
    }

    public long getSendTime()
    {
        return sendTime;
    }
}
