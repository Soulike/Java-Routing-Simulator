package Processor;

import Interface.MessageProcessor;
import Message.GraphInfo;
import Component.Graph;
import Objects.Path;

import java.util.ArrayList;
import java.util.List;

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

    public void process(Object object)
    {
        synchronized (graph)
        {
            GraphInfo info = (GraphInfo) object;
            List<Path> neighborPathList = info.getPathList();
            List<String> graphNodeIds = graph.getNodeIds();

            // 如果自己在自己图的结点里面存在邻居连接不到的结点A，而且自己也不是直接连接结点A，就选择删除结点A。
            // 因为如果邻居通过自己来连接结点A，那么邻居必然不会连接不到结点A（因为一定可以通过自己来连接到）。结点A必然是自己通过邻居连接到的。邻居对结点A的状态更可信。
            for (String nodeId : graphNodeIds)
            {
                if (!info.hasNode(nodeId) && graph.getPathLength(this.nodeId, nodeId) == Graph.INF)
                {
                    graph.removeNode(nodeId);
                }
            }

            final ArrayList<Path> pathsToUpdate = new ArrayList<>();
            for (Path path : neighborPathList)
            {

                // 这条路径有一端自己图里面没有，就添加这个结点以及路径
                if (!graphNodeIds.contains(path.getStartNodeId()) || !graphNodeIds.contains(path.getEndNodeId()))
                {
                    pathsToUpdate.add(path);
                }

                // 如果两端都在自己图里，且不是与本结点直接相连的路径
                // 与本结点直接相连的路径，取本地的数据，丢弃外来数据
                else if (!path.getStartNodeId().equals(nodeId) && !path.getEndNodeId().equals(nodeId))
                {
                    // 如果图的数据与接收到的图的数据不一致，就修改为接收到图的数据
                    if (graph.getPathLength(path.getStartNodeId(), path.getEndNodeId()) != path.getPathLength())
                    {
                        pathsToUpdate.add(path);
                    }
                }
            }
            graph.updatePaths(pathsToUpdate);
        }
    }
}
