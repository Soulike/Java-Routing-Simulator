package Message;

import Component.Graph;
import Objects.Path;

import java.io.Serializable;
import java.util.*;

/**
 * 路径信息。用于网络传输。
 */
public class GraphInfo implements Serializable
{
    private final List<Path> pathList;

    public GraphInfo(Graph graph)
    {
        this.pathList = graph.getPathList();
    }

    public List<Path> getPathList()
    {
        return new ArrayList<>(pathList);
    }
}
