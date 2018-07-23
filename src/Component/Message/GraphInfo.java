package Component.Message;

import Component.Graph.Graph;
import Component.Graph.Path;

import java.io.Serializable;
import java.util.*;

/**
 * 路径信息。用于网络传输。
 */
public class GraphInfo implements Serializable
{
    // 所有路径的列表
    private final List<Path> pathList;
    private final String senderId;

    public GraphInfo(Graph graph, String nodeId)
    {
        this.pathList = graph.getPathList();
        this.senderId = nodeId;
    }

    public List<Path> getPathList()
    {
        return new ArrayList<>(pathList);
    }

    public String getSenderId()
    {
        return senderId;
    }
}
