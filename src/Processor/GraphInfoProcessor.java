package Processor;

import Interface.MessageProcessor;
import Message.GraphInfo;
import Objects.Graph;
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
        GraphInfo info = (GraphInfo) object;
        Graph neighborGraph = info.getGraph();
        List<Path> neighborPathList = neighborGraph.getPathList();
        List<String> graphNodeIds = graph.getNodeIds();

        final ArrayList<Path> pathsToUpdate = new ArrayList<>();

        for (Path path : neighborPathList)
        {
            // 这条路径有一端图里面没有，就添加这个结点以及路径
            if (!graphNodeIds.contains(path.getStartNodeId()) || !graphNodeIds.contains(path.getEndNodeId()))
            {
                pathsToUpdate.add(path);
            }

            // 如果两端都在图里，且不是与本结点直接相连的路径
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
