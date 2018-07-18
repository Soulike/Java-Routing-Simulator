package Message;

import Objects.Graph;

import java.io.Serializable;

/**
 * 路径信息。用于网络传输。
 */
public class GraphInfo implements Serializable
{
    private final Graph graph;

    public GraphInfo(Graph graph)
    {
        this.graph = graph;
    }

    public Graph getGraph()
    {
        return graph;
    }
}
