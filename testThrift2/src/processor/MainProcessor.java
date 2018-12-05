package processor;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Executors;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.ServerContext;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import business.BusinessServicesMapping;
import thrift.iface.DIYFrameworkService;
import thrift.iface.DIYFrameworkService.Iface;

public class MainProcessor {
	static {
		BasicConfigurator.configure();
	}

	/**
	 * 日志
	 */
	private static final Log LOGGER = LogFactory.getLog(MainProcessor.class);

	private static final Integer SERVER_PORT = 8090;

	/**
	 * 专门用于锁定以保证这个主线程不退出的一个object对象
	 */
	private static final Object WAIT_OBJECT = new Object();

	/**
	 * 标记apache thrift是否启动成功了
	 * 只有apache thrift启动成功了，才需要连接到zk
	 */
	private boolean isthriftStart = false;

	public static void main(String[] args) {
		/*
		 * 主程序要做的事情：
		 * 
		 * 1、启动thrift服务。并且服务调用者的请求
		 * 2、连接到zk，并向zk注册自己提供的服务名称，告知zk真实的访问地址、访问端口
		 * （向zk注册的服务，存储在BusinessServicesMapping这个类的K-V常量中）
		 * */

		//1、========启动thrift服务
		MainProcessor mainProcessor = new MainProcessor();
		mainProcessor.startServer();

		// 一直等待，apache thrift启动完成
		synchronized (mainProcessor) {
			try {
				while(!mainProcessor.isthriftStart) {
					mainProcessor.wait();
				}
			} catch (InterruptedException e) {
				MainProcessor.LOGGER.error(e);
				System.exit(-1);
			}
		}

		//2、========连接到zk
		try {
			mainProcessor.connectZk();
		} catch (IOException | KeeperException | InterruptedException e) {
			MainProcessor.LOGGER.error(e);
			System.exit(-1);
		}

		// 这个wait在业务层面，没有任何意义。只是为了保证这个守护线程不会退出
		synchronized (MainProcessor.WAIT_OBJECT) {
			try {
				MainProcessor.WAIT_OBJECT.wait();
			} catch (InterruptedException e) {
				MainProcessor.LOGGER.error(e);
				System.exit(-1);
			}
		}
	}

	/**
	 * 这个私有方法用于连接到zk上，并且注册相关服务
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws KeeperException
	 */
	private void connectZk() throws IOException, KeeperException, InterruptedException {
		// 读取这个服务提供者，需要在zk上注册的服务
		Set<String> serviceNames = BusinessServicesMapping.SERVICES_MAPPING.keySet();
		// 如果没有任何服务需要注册到zk，那么这个服务提供者就没有继续注册的必要了
		if(serviceNames == null || serviceNames.isEmpty()) {
			return;
		}

		// 默认的监听器
		MyDefaultWatcher defaultWatcher = new MyDefaultWatcher();
		// 连接到zk服务器集群，添加默认的watcher监听
		ZooKeeper zk = new ZooKeeper("localhost:2181", 120000, defaultWatcher);

		//创建一个父级节点Service
		Stat pathStat = null;
		try {
			pathStat = zk.exists("/Service", defaultWatcher);
			//如果条件成立，说明节点不存在（只需要判断一个节点的存在性即可）
			//创建的这个节点是一个“永久状态”的节点
			if(pathStat == null) {
				zk.create("/Service", "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch(Exception e) {
			System.exit(-1);
		}

		// 开始添加子级节点，每一个子级节点都表示一个这个服务提供者提供的业务服务
		for (String serviceName : serviceNames) {
			JSONObject nodeData = new JSONObject();
			nodeData.put("ip", "127.0.0.1");
			nodeData.put("port", MainProcessor.SERVER_PORT);
			zk.create("/Service/" + serviceName, nodeData.toString().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		}

		//执行到这里，说明所有的service都启动完成了
		MainProcessor.LOGGER.info("===================所有service都启动完成了，主线程开始启动===================");
	}

	/**
	 * 这个私有方法用于开启Apache thrift服务端，并进行持续监听
	 * @throws TTransportException
	 */
	private void startServer() {
		Thread startServerThread = new Thread(new StartServerThread());
		startServerThread.start();
	}

	private class StartServerThread implements Runnable {
		@Override
		public void run() {
			MainProcessor.LOGGER.info("看到这句就说明thrift服务端准备工作 ....");

			// 服务执行控制器（只要是调度服务的具体实现该如何运行）
			TProcessor tprocessor = new DIYFrameworkService.Processor<Iface>(new DIYFrameworkServiceImpl());

			// 基于阻塞式同步IO模型的Thrift服务，正式生产环境不建议用这个
			TServerSocket serverTransport = null;
			try {
				serverTransport = new TServerSocket(MainProcessor.SERVER_PORT);
			} catch (TTransportException e) {
				MainProcessor.LOGGER.error(e);
				System.exit(-1);
			}

			// 为这个服务器设置对应的IO网络模型、设置使用的消息格式封装、设置线程池参数
			Args tArgs = new Args(serverTransport);
			tArgs.processor(tprocessor);
			tArgs.protocolFactory(new TBinaryProtocol.Factory());
			tArgs.executorService(Executors.newFixedThreadPool(100));

			// 启动这个thrift服务
			TThreadPoolServer server = new TThreadPoolServer(tArgs);
			server.setServerEventHandler(new StartServerEventHandler());
			server.serve();
		}
	}

	/**
	 * 为这个TThreadPoolServer对象，设置是一个事件处理器。
	 * 以便在TThreadPoolServer正式开始监听服务请求前，通知mainProcessor：
	 * “Apache Thrift已经成功启动了”
	 *
	 *
	 */
	private class StartServerEventHandler implements TServerEventHandler {
		@Override
		public void preServe() {
			/*
			 * 需要实现这个方法，以便在服务启动成功后，
			 * 通知mainProcessor： “Apache Thrift已经成功启动了”
			 * */
			MainProcessor.this.isthriftStart = true;
			synchronized (MainProcessor.this) {
				MainProcessor.this.notify();
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.thrift.server.TServerEventHandler#createContext(org.apache.thrift.protocol.TProtocol, org.apache.thrift.protocol.TProtocol)
		 */
		@Override
		public ServerContext createContext(TProtocol input, TProtocol output) {
			/*
			 * 无需实现
			 * */
			return null;
		}

		@Override
		public void deleteContext(ServerContext serverContext, TProtocol input, TProtocol output) {
			/*
			 * 无需实现
			 * */
		}

		@Override
		public void processContext(ServerContext serverContext, TTransport inputTransport, TTransport outputTransport) {
			/*
			 * 无需实现
			 * */
		}
	}

	/**
	 * 这是默认的watcher，什么也没有，也不需要有什么<br>
	 * 因为按照功能需求，服务器端并不需要监控zk上的任何目录变化事件
	 *
	 */
	private class MyDefaultWatcher implements Watcher {
		public void process(WatchedEvent event) {

		}
	}
}
