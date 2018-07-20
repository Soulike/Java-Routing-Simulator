package Component;

import Component.ThreadPool.Processor;
import Component.ThreadPool.ThreadPool;
import Interface.MessageProcessor;
import Interface.TimingSender;
import Message.GraphInfo;
import Message.HeartBeatPackage;
import Message.NeighborNodeInfo;
import util.Broadcaster;
import Processor.*;
import Objects.*;
import util.Converter;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class Node
{
    private final String nodeId;
    private final DatagramSocket socket;
    private final Graph graph;

    private final ThreadPool pool;

    private final MessageProcessor graphInfoProcessor;
    private final MessageProcessor heartBeatPackageProcessor;
    private final MessageProcessor neighborNodeInfoProcessor;
    private final Processor consoleInputProcessor;
    private final TimingSender graphInfoSender;
    private final TimingSender heartBeatPackageSender;


    /**
     * 所有邻居结点的端口。
     */
    private List<Integer> neighborPorts;


    public Node(String nodeId, int port, java.nio.file.Path neighborConfigFilePath, long heartBeatSendInterval, long graphInfoSendInterval, long printInterval) throws IOException
    {
        this.nodeId = nodeId;
        this.socket = new DatagramSocket(port);
        this.graph = new Graph(nodeId);

        this.pool = new ThreadPool(150, 200);
        this.neighborPorts = new ArrayList<>();

        List<Path> neighborPaths = readConfigFile(neighborConfigFilePath);

        this.graphInfoProcessor = new GraphInfoProcessor(graph, nodeId);
        this.heartBeatPackageProcessor = new HeartBeatPackageProcessor(graph, neighborPaths, heartBeatSendInterval);
        this.neighborNodeInfoProcessor = new NeighborNodeInfoProcessor(graph, socket);
        this.consoleInputProcessor = new ConsoleInputProcessor(graph, nodeId);


        Broadcaster.broadcast(new NeighborNodeInfo(nodeId, neighborPaths), socket, neighborPorts);

        this.graphInfoSender = new GraphInfoSender(graph, socket, neighborPorts, graphInfoSendInterval);
        this.heartBeatPackageSender = new HeartBeatPackageSender(nodeId, socket, neighborPorts, heartBeatSendInterval);

        graphInfoSender.start();
        heartBeatPackageSender.start();

        pool.createThread(System.in, consoleInputProcessor);


        new Timer(true).schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                synchronized (graph)
                {
                    graph.printShortestPaths(nodeId);
                }
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

        Scanner scanner = new Scanner(neighborConfigFilePath, StandardCharsets.UTF_8);
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
                else if (objectReceived instanceof NeighborNodeInfo)
                {
                    pool.createThread(objectReceived, neighborNodeInfoProcessor);
                }
            }
            catch (ClassNotFoundException | EOFException e)
            {
                System.out.println("端口收到无效数据");
                e.printStackTrace();
            }
        }
    }
}
