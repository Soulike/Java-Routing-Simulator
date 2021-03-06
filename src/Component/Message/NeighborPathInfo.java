package Component.Message;

import java.io.Serializable;
import java.util.*;

import Component.Graph.Path;

/**
 * 广播使用相邻结点路径信息对象。
 */
public class NeighborPathInfo implements Serializable
{
    // 该包发出时的时间戳
    private final long sendTime;

    // 发送结点的标识
    private final String senderNodeId;

    //与该结点所有相邻的结点的路径信息。
    private final List<Path> pathList;

    public NeighborPathInfo(String senderNodeId, List<Path> pathList)
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
