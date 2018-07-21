package Processor;

import Interface.MessageProcessor;
import Message.GraphInfo;
import Component.Graph;
import Objects.Path;

import java.util.*;

public class GraphInfoProcessor implements MessageProcessor
{
    private final Graph graph;
    private final String nodeId;

    /**
     * @param nodeId 本结点的 ID
     */
    public GraphInfoProcessor(Graph graph, String nodeId)
    {
        this.nodeId = nodeId;
        this.graph = graph;
    }

    /**
     * 这里的处理思想是：与自己直接连接的路径由自己负责，其他的所有路径都无条件相信邻居提供的路径信息。
     */
    public void process(Object object)
    {
        GraphInfo info = (GraphInfo) object;
        List<Path> neighborPathList = info.getPathList();
        List<Path> pathList = graph.getPathList();

        // 如果某条与自己不相连的路径在邻居结点处不存在，那么自己也删掉这条路径
        for (Path path : pathList)
        {
            if (!neighborPathList.contains(path) && !path.getStartNodeId().equals(nodeId) && !path.getEndNodeId().equals(nodeId))
            {
                graph.updatePath(new Path(path.getStartNodeId(), path.getEndNodeId(), Graph.INF));
            }
        }


        final ArrayList<Path> pathsToUpdate = new ArrayList<>();
        for (Path path : neighborPathList)
        {
            // 如果路径与自己完全不相连，添加这条路径
            if (!this.nodeId.equals(path.getStartNodeId()) && !this.nodeId.equals(path.getEndNodeId()))
            {
                pathsToUpdate.add(path);
            }
        }
        graph.updatePaths(pathsToUpdate);
    }
}
