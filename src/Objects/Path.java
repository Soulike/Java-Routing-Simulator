package Objects;

import java.io.Serializable;

/**
 * 路径类，保存一条路径的信息。
 */
public class Path implements Serializable
{
    private final String startNodeId;
    private final String endNodeId;
    private final double pathLength;

    public Path(String startNodeId, String endNodeId, double pathLength)
    {
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.pathLength = pathLength;
    }

    public String getStartNodeId()
    {
        return startNodeId;
    }

    public String getEndNodeId()
    {
        return endNodeId;
    }

    public double getPathLength()
    {
        return pathLength;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj.getClass() != Path.class)
        {
            return false;
        }
        else
        {
            Path path = (Path) obj;
            return path.startNodeId.equals(startNodeId) && path.endNodeId.equals(endNodeId);
        }
    }
}
