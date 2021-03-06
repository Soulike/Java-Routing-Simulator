# Java Routing Simulator

## 项目介绍
Java 小学期大作业。路由模拟器。

## 部署方法
1. 在 IDEA 点击上步菜单 Build-Build Project 下编译整个项目；
2. 把 graph1 或者 graph2 下面的 config.conf 复制一份到项目目录下的 out/production/Java-Routing-Simulator 下，与 Main.class 同目录；
3. 在 Ubuntu 下，在 graph1 或 graph2 文件夹下开 N 个命令行窗口（在文件夹里右键-在终端打开），同时运行 graph1 或 graph2 bash 文件夹下的 N 个 .sh 文件；
    * 如果是 Windows 就直接双击打开所有的 .bat 文件，我分好文件夹了。
4. debug，emmmmm……。

## 原始需求

本次作业要求实现路由协议，编写的程序运行每个路由节点上，通过进程来模拟路由节点。进程运行的时候读入路径信息数据。在一台计算机上通过选择不同的端口来模拟不同的网络节点。每个节点通过UDP协议将路径信息发送给其他所有节点。程序必须能够处理死节点（例如某个节点意外宕机）。

程序运行的时候，需要通过命令行参数指定NODE_ID, 例如，用一个大写字符A,B,C,D…来表示节点ID标识信息。同时，也要通过命令行参数指定UDP端口。

程序运行的命令行参数如下：
```Java Assignment A 2000 configA.txt
Java Assignment B 2001 configB.txt
Java Assignment C 2002 configC.txt
……
……
```
在配置文件configA.txt中，数据如下：
```
2
B 5 2001
C 7 2002
```

解释如下，A节点的相连节点有2个。 分别是B节点和C节点，到B节点路径长度为5，B节点的端口为2001， 到C节点路径长度为7，C节点的端口为2002

初始的时候，每个节点通过配置文件知道到它所连接的节点的信息，其余节点的路径信息并不知道。

需要完成如下两个要求：
1. 节点启动后，将它自己的标识信息，以及它直接连接的节点的路径信息，通过UDP协议发送给它直接连接的节点。
2. 它的相邻节点接受到路径信息后，再发送给这个接收节点的相邻节点，经过这样的“广播”，每个节点会获得整个网络的路径信息。
3. 同时，要求每个节点经过一个更新周期时间，定时的发送它的路径信息到它的直接连接节点。更新周期时间单位为秒，其值存储在一个配置文件中。
    * 例如：assignment.properties文件，例如其更新时间为1秒。尽管UDP协议会丢失数据包，但是，定时更新机制会保证路径信息会传输到所有节点。
4. 最终，每个节点都会拥有整个网络的路径信息，可以利用Dijkstra算法来计算最短路径。
5. 要求每个节点能够打印输出其到网络中其他节点的最短路径，输出到控制台即可。要求经过“一段时间广播”后，每个节点可以自动输出最短路径信息，也可以通过控制台用户输入某个命令，输出该节点到其他节点的最短路径信息。
    * 最短路径信息包括路由和最短路径的长度信息。最短路径信息也需要设置定时输出功能，例如每个节点每经过20秒后就输出它到其他节点的最短路径信息。
6. 这部分代码实现要求提供网络中某个节点发生故障的处理功能。
    * 当某个节点发生故障，其直接连接的节点应该尽快知道这种情况，并把该节点从网络中删除。网络中其他节点将通过定时更新也将删除该节点，也就是整个网络中所有节点都应该删除该节点，整个网络的路径信息都应该得到更新。
    * 故障节点的检查和发现通常是通过心跳包来实现的，每个节点每个一定是时间向其直接连接的节点发送心跳包，表示它工作正常，心跳包发送的时间比定时更新路径信息的时间要短，例如4分之一秒。
        * 路径信息更新数据包和心跳包的数据内容及格式当然不同。一个节点如果连续接收不到3个心跳包，那么可以判断其某个直接连接的节点发生故障，
        * 然后，再通过路径定时更新功能，网络上其他节点都会知道发生故障的节点。最短路径信息也将会被更新。假设发生故障的节点再也不会重新恢复工作了。作业检查的时候，先停掉一个节点，然后路径信息更新并打印输出后，再尝试停掉其他节点。没有必要要求同时停掉几个节点。

 ![示例图](https://images.gitee.com/uploads/images/2018/0718/151855_6c570330_1118822.png "屏幕截图.png")


假设有6个节点，例如节点A占用2000端口，节点B占用2001端口，以此类推，节点的配置文件为configA.txt, configB.txt，以此类推，需要定义6个节点配置文件。

节点A运行后，路径信息更新后，经过一段时间后，它的输出应该是：
```
least-cost path to node B: AB and the cost is 2.0
least-cost path to node C: ADEC and the cost is 3.0
least-cost path to node D: AD and the cost is 1.0
least-cost path to node E: ADE and the cost is 2.0
least-cost path to node F: ADEF and the cost is 4.0
```

测试节点故障，可以假设节点B发生故障，然后检查网络路径信息更新及最短路径的输出。
然后，再假设节点C发生故障，继续检查输出。

网络的拓扑结构除了大作业提供的测试数据之外，也要求提供另外一组测试数据，请自行提供另外一组测试数据。

作业要求：必须使用UDP协议，系统的心跳包间隔时间，路径更新时间，最短路径更新输出时间等配置信息必须写在配置文件中。必须使用多线程编程，必须写同步代码。

每个学生必须提交纸面文档，每组同学同时检查作业，但是分别演示，分别回答问题。要有先后顺序。提交大作业纸面文档的封面格式稍后公布。检查大作业的同时必须上交纸面文档，如果没有纸面文档，大作业成绩为0分。纸面文档要求提供部分核心代码，包括udp发送和接收处理程序，多线程同步处理程序，最短路径计算程序等。要求有注释，有程序设计的简单说明。文档在10页以内，5页以上（包含封面）。在检查大作业的同时，每名同学需要回答两个语法问题。大作业检查不仅仅要演示程序输出结果，也要讲解算法实现。大作业检查按照分组来进行检查，检查大作业的时候，其余组的同学不允许在场。大作业检查时间为25号，26号。课程成绩公布在27号上午，如果对成绩有异议，可以联系授课教师。成绩登录到大连理工大学教务系统之后，一律不接受大作业复查，一律不接受更改成绩的申请。


