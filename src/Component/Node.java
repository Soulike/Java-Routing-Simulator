package Component;

import Component.Graph.*;
import Component.Sender.*;
import Component.ThreadPool.*;
import Component.Message.*;
import util.*;
import Component.Processor.*;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

/**
 * 结点对象。
 */
public class Node
{
    private final String nodeId;
    private final DatagramSocket socket;
    private final Graph graph;

    // 线程池
    private final ThreadPool pool;

    // 各种处理器。
    private final Processor graphInfoProcessor;
    private final Processor heartBeatPackageProcessor;
    private final Processor neighborPathInfoProcessor;
    private final Processor consoleInputProcessor;

    // 各种定时发送器。
    private final TimingSender graphInfoSender;
    private final TimingSender heartBeatPackageSender;


    // 所有邻居结点的端口。
    private List<Integer> neighborPorts;


    public Node(String nodeId, int port, java.nio.file.Path neighborConfigFilePath, long heartBeatSendInterval, long graphInfoSendInterval, long printInterval) throws IOException
    {
        System.out.printf("结点 %s 开始运行\n", nodeId);
        this.nodeId = nodeId;
        this.socket = new DatagramSocket(port);
        this.graph = new Graph(nodeId);

        this.pool = new ThreadPool(15, 50);
        this.neighborPorts = new ArrayList<>();

        List<Path> neighborPaths = readConfigFile(neighborConfigFilePath);

        this.graphInfoProcessor = new GraphInfoProcessor(graph, nodeId);
        this.heartBeatPackageProcessor = new HeartBeatPackageProcessor(nodeId, graph, neighborPaths, heartBeatSendInterval);
        this.neighborPathInfoProcessor = new NeighborPathInfoProcessor(graph, socket);
        this.consoleInputProcessor = new ConsoleInputProcessor(graph, nodeId);

        // 把自己以及邻居结点路径信息广播到所有邻居结点
        Broadcaster.broadcast(new NeighborPathInfo(nodeId, neighborPaths), socket, neighborPorts);

        // 路径信息定时发送器。这里对设定的时间进行了 25% 上下的浮动以防止路由信息更新无法扩散
        this.graphInfoSender = new GraphInfoSender(nodeId, graph, socket, neighborPorts, graphInfoSendInterval + Math.round((Math.random() - 0.5) * 0.5 * graphInfoSendInterval));

        this.heartBeatPackageSender = new HeartBeatPackageSender(nodeId, socket, neighborPorts, heartBeatSendInterval);

        graphInfoSender.start();
        heartBeatPackageSender.start();

        pool.createThread(System.in, consoleInputProcessor);

        // 每隔一段时间输出一次最短路径信息
        new Timer(true).schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                graph.printShortestPaths(nodeId);
            }
        }, printInterval, printInterval);
    }


    /**
     * 读取配置文件，并在图中添加相邻结点信息。
     *
     * @return 文件所写的相邻结点信息。
     */
    private List<Path> readConfigFile(java.nio.file.Path neighborConfigFilePath) throws IOException
    {
        if (Files.notExists(neighborConfigFilePath))
        {
            throw new FileNotFoundException();
        }

        Scanner scanner = new Scanner(neighborConfigFilePath);
        try
        {
            int lineNum = Integer.parseInt(scanner.nextLine());
            String line;
            String[] lineParts;
            List<Path> neighborPaths = new ArrayList<>(lineNum);
            for (int i = 0; i < lineNum; i++)
            {
                line = scanner.nextLine();
                lineParts = line.split(" ");
                neighborPaths.add(new Path(nodeId, lineParts[0], Integer.parseInt(lineParts[1])));
                neighborPorts.add(Integer.parseInt(lineParts[2]));
            }
            graph.updatePaths(neighborPaths);
            return neighborPaths;
        }
        catch (Exception e)
        {
            throw new IOException("结点配置文件格式错误", e);
        }
    }

    /**
     * 开始监听 UDP 端口，并根据收到的包类型分配对应的处理器。
     */
    public void listen() throws IOException
    {
        byte[] packetBuffer = new byte[1024 * 1024];
        DatagramPacket packet = new DatagramPacket(packetBuffer, 0, packetBuffer.length);
        Object objectReceived;

        while (true)
        {
            socket.receive(packet);
            try
            {
                objectReceived = Converter.byteArrayToObject(Arrays.copyOfRange(packetBuffer, packet.getOffset(), packet.getOffset() + packet.getLength()));
                if (objectReceived instanceof GraphInfo)
                {
                    pool.createThread(objectReceived, graphInfoProcessor);
                }
                else if (objectReceived instanceof HeartBeatPackage)
                {
                    pool.createThread(objectReceived, heartBeatPackageProcessor);
                }
                else if (objectReceived instanceof NeighborPathInfo)
                {
                    pool.createThread(objectReceived, neighborPathInfoProcessor);
                }
            }
            catch (ClassNotFoundException | EOFException e)
            {
                System.err.println("端口收到无效数据");
                System.err.println(e.getLocalizedMessage());
            }
        }
    }
}
