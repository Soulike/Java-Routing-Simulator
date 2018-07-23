import Component.Node;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("参数数量错误");
            System.exit(0);
        }
        final String nodeId = args[0];
        final int port = Integer.parseInt(args[1]);
        final Path neighborConfigFilePath = Paths.get(args[2]);

        final Properties properties = new Properties();

        try (InputStream in = new FileInputStream("config.conf"))
        {
            properties.load(in);
            final long heartBeatSendInterval = Long.parseLong(properties.getProperty("heartBeatSendInterval"));
            final long graphInfoSendInterval = Long.parseLong(properties.getProperty("graphInfoSendInterval")) * 1000;
            final long printInterval = Long.parseLong(properties.getProperty("printInterval")) * 1000;
            try
            {
                Node node = new Node(nodeId, port, neighborConfigFilePath, heartBeatSendInterval, graphInfoSendInterval, printInterval);
                node.listen();
            }
            catch (IOException e)
            {
                System.out.println("结点邻居结点配置文件不存在或网络连接错误");
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("配置文件不存在");
        }
        catch (IOException e)
        {
            System.out.println("配置文件读取错误");
        }
    }
}
