package Object;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Graph
{
    /**
     * 所有结点的名称
     */
    private List<String> nodeIds;
    /**
     * 结点之间的路径长度。
     * 例如 paths[1][2] 即为 1 号结点到 2 号结点的路径长度，他们的名字分别为 nodeIds[1] 和 nodeIds[2]。
     * paths[1][2] 与 paths[2][1] 一定是相等的。
     */
    private double[][] paths;
    /**
     * 正无穷距离
     */
    private static int INF = -1;//正无穷距离

    public Graph(String nodeId)
    {
        nodeIds = new LinkedList<>();
        nodeIds.add(nodeId);
        paths = new double[nodeIds.size()][nodeIds.size()];
    }

    public Graph(List<String> nodeIds, double[][] paths)
    {
        this.nodeIds = new LinkedList<>(nodeIds);
        this.paths = paths.clone();
    }

    /**
     * 获取两个结点之间的路径长度
     */
    public double getPathLength(String startNodeId, String endNodeId)
    {
        int startNodeIndex = getNodeIndex(startNodeId);
        int endNodeIndex = getNodeIndex(endNodeId);
        return paths[startNodeIndex][endNodeIndex];
    }

    /**
     * 给结点的名称，返回Pair(上一结点编号数组, 最短长度数组)。
     * 上一结点编号数组：下标 i 存储的数据 j 代表想要到达 i 号结点，需要先到达 j 号结点。nodeIndex 号结点的上一个结点编号等于 nodeIndex。
     * 最短长度数组：下标 i 存储的数据 j 代表从 nodeIndex 号结点到 i 号结点的最短长度是 j。
     *
     * @param nodeIndex 想要查找到各结点最短路径的起始结点编号
     */
    private synchronized Pair<int[], double[]> Dijkstra(int nodeIndex)
    {
        HashSet<Integer> processedNodes = new HashSet<>();//已经找到最短路径的结点号集合
        int[] prevNode = new int[nodeIds.size()];//每个结点最短路径的前一个结点号
        double[] currentShortestPathLength = new double[nodeIds.size()];//从nodeName到各个结点的目前最短长度
        int currentShortestPathIndex = 0;//每一轮查找后最短路径的编号
        int lastProcessNodeIndex = nodeIndex;//最后一个找到的距离最短的结点

        //给距离数组赋初值
        for (int i = 0; i < currentShortestPathLength.length; i++)
        {
            // 到自己距离是0
            if (i == nodeIndex)
            {
                currentShortestPathLength[i] = 0;
            }
            // 到其他的都设为INF
            else
            {
                currentShortestPathLength[i] = INF;
            }
        }
        // 起始结点算作处理过的
        processedNodes.add(nodeIndex);
        // 当已找到最短路径集合还没有包含所有结点的时候，继续循环
        while (processedNodes.size() != nodeIds.size())
        {
            // 从lastProcessNodeIndex出发比较新路径是否比老路径更短
            for (int i = 0; i < nodeIds.size(); i++)
            {
                // 新路径比老路径更短，且这个距离不是INF，且这个结点不包含在已处理结点集合中，则更新路径长度与上一结点编号
                if ((paths[lastProcessNodeIndex][i] + currentShortestPathLength[lastProcessNodeIndex] < currentShortestPathLength[i] || currentShortestPathLength[i] == INF) && paths[lastProcessNodeIndex][i] != INF && !processedNodes.contains(i))
                {
                    currentShortestPathLength[i] = paths[lastProcessNodeIndex][i] + currentShortestPathLength[lastProcessNodeIndex];
                    prevNode[i] = lastProcessNodeIndex;
                }
            }
            // 在这一轮循环结束之后查找当前最短路径
            currentShortestPathIndex = findShortestPath(currentShortestPathLength, processedNodes);
            // 找到后，将其连接的结点添加到已处理集合中
            processedNodes.add(currentShortestPathIndex);
            // 修改lastProcessNodeIndex为这个结点
            lastProcessNodeIndex = currentShortestPathIndex;
        }
        return new Pair<>(prevNode, currentShortestPathLength);
    }

    /**
     * 增加结点及其所有的连接路径。
     */
    public synchronized void addNode(String nodeId, List<Path> paths)
    {
        addNode(nodeId);
        updatePaths(paths);
    }

    /**
     * 仅增加一个新结点。
     */
    private synchronized void addNode(String nodeId)
    {
        if (!nodeIds.contains(nodeId))
        {
            nodeIds.add(nodeId);
            expandPaths();
        }
    }

    /**
     * 删除结点及其路径。
     */
    private synchronized void removeNode(String nodeId)
    {
        int nodeIndex = getNodeIndex(nodeId);
        nodeIds.remove(nodeIndex);
        shrinkPaths(nodeIndex);
    }

    /**
     * 根据传入的 Path 对象信息更新 paths 数组
     */
    public synchronized void updatePaths(List<Path> paths)
    {
        int startNodeIndex = 0;
        int endNodeIndex = 0;
        for (Path path : paths)
        {
            if (!nodeIds.contains(path.getStartNodeId()))
            {
                addNode(path.getStartNodeId());
            }

            if (!nodeIds.contains(path.getEndNodeId()))
            {
                addNode(path.getEndNodeId());
            }

            startNodeIndex = getNodeIndex(path.getStartNodeId());
            endNodeIndex = getNodeIndex(path.getEndNodeId());
            this.paths[startNodeIndex][endNodeIndex] = path.getPathLength();
            this.paths[endNodeIndex][startNodeIndex] = path.getPathLength();
        }
    }

    /**
     * 将 paths 数组扩大一行一列，并把原来的数据复制出来。
     */
    private synchronized void expandPaths()
    {
        final int lastLength = paths.length;
        final double[][] newPaths = new double[lastLength + 1][lastLength + 1];
        for (int row = 0; row < lastLength; row++)
        {
            for (int col = 0; col < lastLength; col++)
            {
                newPaths[row][col] = paths[row][col];
            }
        }
        this.paths = newPaths;
    }

    /**
     * 将 paths 数组缩小一行一列，并把剩余数据复制出来。
     *
     * @param nodeIndex 要被删除的行列号。
     */
    private synchronized void shrinkPaths(int nodeIndex)
    {
        final int lastLength = paths.length;
        final double[][] newPaths = new double[lastLength][lastLength];
        int newRow = 0;
        int newCol = 0;
        for (int row = 0; row < lastLength; row++)
        {
            for (int col = 0; col < lastLength && row != nodeIndex; col++)
            {
                if (col != nodeIndex)
                {
                    if (row < nodeIndex)
                    {
                        newRow = row;
                    }
                    else
                    {
                        newRow = row - 1;
                    }

                    if (col < nodeIndex)
                    {
                        newCol = col;
                    }
                    else
                    {
                        newCol = col - 1;
                    }
                    newPaths[newRow][newCol] = paths[row][col];
                }
            }
        }
        paths = newPaths;
    }

    /**
     * 输出最短路径信息。
     */
    public void printShortestPaths(int nodeIndex)
    {
        final Pair<int[], double[]> info = Dijkstra(nodeIndex);

        final int[] prevNodeArray = info.getFirst();
        final double[] currentShortestPathLengthArray = info.getSecond();
        StringBuilder[] pathStr = new StringBuilder[nodeIds.size()];

        for (int i = 0; i < pathStr.length; i++)
        {
            pathStr[i] = new StringBuilder();
        }

        int currentNodeIndex = -1;

        for (int i = 0; i < nodeIds.size(); i++)
        {
            currentNodeIndex = i;
            while (currentNodeIndex != nodeIndex)
            {
                pathStr[i].append(nodeIds.get(currentNodeIndex));
                currentNodeIndex = prevNodeArray[currentNodeIndex];
            }
            pathStr[i].reverse();

            char targetNode = pathStr[i].charAt(pathStr[i].length() - 1);
            System.out.printf("least-cost path to node %c: %s and the cost is %f\n", targetNode, pathStr[i].toString(), currentShortestPathLengthArray[i]);
        }
    }

    /**
     * 找到这个结点 Id 在 nodeIds 中的下标。
     */
    private synchronized int getNodeIndex(String nodeId)
    {
        int index = -1;
        for (int i = 0; i < nodeIds.size(); i++)
        {
            if (nodeIds.get(i).equals(nodeId))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 找到数组中最小路径的下标。
     *
     * @param currentShortestPathLength 当前到所有结点最短长度。
     * @param processedNodes            已经确定最短长度的结点集合。
     */
    private int findShortestPath(double[] currentShortestPathLength, HashSet<Integer> processedNodes)
    {
        int minIndex = -1;
        double min = Integer.MAX_VALUE;
        for (int i = 0; i < currentShortestPathLength.length; i++)
        {
            if (currentShortestPathLength[i] < min && currentShortestPathLength[i] != INF && !processedNodes.contains(i))
            {
                min = currentShortestPathLength[i];
                minIndex = i;
            }
        }
        return minIndex;
    }
}
