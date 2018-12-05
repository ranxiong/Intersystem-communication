package client;

import java.nio.ByteBuffer;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import thrift.iface.DIYFrameworkService.Client;
import thrift.iface.Reponse;
import thrift.iface.Request;
import utils.JSONUtils;

public class ThriftClient {
    /**
     * 日志
     */
    private static final Log LOGGER = LogFactory.getLog(ThriftClient.class);

    private static final String SERVCENAME = "queryUserDetailService";

    static {
        BasicConfigurator.configure();
    }

    public static final void main(String[] main) throws Exception {
        /*
		 * 服务治理框架的客户端示例，要做以下事情：
		 * 
		 * 1、连接到zk，查询当前zk下提供的服务列表中是否有自己需要的服务名称（queryUserDetailService）
		 * 2、如果没有找到需要的服务名称，则客户端终止工作
		 * 3、如果找到了服务，则通过服务给出的ip，port，基于Thrift进行正式请求
		 * （这时，和zookeeper是否断开，关系就不大了）
		 * */
        // 1、===========================
        // 默认的监听器
        ClientDefaultWatcher defaultWatcher = new ClientDefaultWatcher();
        // 连接到zk服务器集群，添加默认的watcher监听
        ZooKeeper zk = new ZooKeeper("localhost:2181", 120000, defaultWatcher);
		
		/*
		 * 为什么客户端连接上来以后，也可能创建一个Service根目录呢？
		 * 因为正式的环境下，不能保证客户端一点就在服务器端全部准备好的情况下，再来做调用请求
		 * */
        Stat pathStat = null;
        try {
            pathStat = zk.exists("/Service", defaultWatcher);
            //如果条件成立，说明节点不存在（只需要判断一个节点的存在性即可）
            //创建的这个节点是一个“永久状态”的节点
            if (pathStat == null) {
                zk.create("/Service", "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            System.exit(-1);
        }

        // 2、===========================
        //获取服务列表(不需要做任何的事件监听，所以第二个参数可以为false)
        List<String> serviceList = zk.getChildren("/Service", false);
        if (serviceList == null || serviceList.isEmpty()) {
            ThriftClient.LOGGER.info("未发现相关服务，客户端退出");
            return;
        }

        //然后查看要找寻的服务是否在存在
        boolean isFound = false;
        byte[] data;
        for (String serviceName : serviceList) {
            if (StringUtils.equals(serviceName, ThriftClient.SERVCENAME)) {
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            ThriftClient.LOGGER.info("未发现相关服务，客户端退出");
            return;
        } else {
            data = zk.getData("/Service/" + ThriftClient.SERVCENAME, false, null);
        }
		
		/*
		 * 执行到这里，zk的工作就完成了，接下来zk是否断开，就不重要了
		 * */
        zk.close();
        if (data == null || data.length == 0) {
            ThriftClient.LOGGER.info("未发现有效的服务端地址，客户端退出");
            return;
        }
        // 得到服务器地值说明
        JSONObject serverTargetJSON = null;
        String serverIp;
        String serverPort;
        try {
            serverTargetJSON = JSONObject.fromObject(new String(data));
            serverIp = serverTargetJSON.getString("ip");
            serverPort = serverTargetJSON.getString("port");
        } catch (Exception e) {
            ThriftClient.LOGGER.error(e);
            return;
        }

        //3、===========================
        TSocket transport = new TSocket(serverIp, Integer.parseInt(serverPort));
        TProtocol protocol = new TBinaryProtocol(transport);
        // 准备调用参数
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("username", "yinwenjie");
        byte[] params = jsonParam.toString().getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(params.length);
        buffer.put(params);
        Request request = new Request(buffer, ThriftClient.SERVCENAME);

        // 开始调用
        Client client = new Client(protocol);
        // 准备传输
        transport.open();
        // 正式调用接口
        Reponse reponse = client.send(request);
        // 一定要记住关闭
        transport.close();

        // 将返回信息显示出来
        ThriftClient.LOGGER.info(JSONUtils.toString(reponse));
    }
}

/**
 * 这是默认的watcher，什么也没有，也不需要有什么<br>
 * 因为按照功能需求，客户端并不需要监控zk上的任何目录变化事件
 */
class ClientDefaultWatcher implements Watcher {
    public void process(WatchedEvent event) {

    }
}