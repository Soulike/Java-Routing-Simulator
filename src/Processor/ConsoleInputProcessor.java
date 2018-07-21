package Processor;

import Component.Graph;
import Component.ThreadPool.Processor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConsoleInputProcessor implements Processor
{
    private final Graph graph;
    private final String nodeId;

    public ConsoleInputProcessor(Graph graph, String nodeId)
    {
        this.graph = graph;
        this.nodeId = nodeId;
    }

    public void process(Object object)
    {
        InputStream in = (InputStream) object;
        Scanner scanner = new Scanner(in);
        String command;
        while (scanner.hasNextLine())
        {
            command = scanner.nextLine();
            if (command.equals("show"))
            {
                graph.printShortestPaths(nodeId);
            }
            else
            {
                System.out.println("未知命令，可输入 show 显示路由信息");
            }
        }
    }
}
