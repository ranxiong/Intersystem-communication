package com.rmi;

import java.rmi.Naming;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

/**
 * 客户端调用RMI测试
 *
 * @author yinwenjie
 */
public class RemoteClient {
    static {
        BasicConfigurator.configure();
    }

    private static Object WAITOBJECT = new Object();
    /**
     * 日志
     */
    private static final Log LOGGER = LogFactory.getLog(RemoteClient.class);

    public static void main(String[] args) throws Exception {
        // 您看，这里使用的是java名称服务技术进行的RMI接口查找。
        RemoteServiceInterface remoteServiceInterface = (RemoteServiceInterface) Naming.lookup("rmi://127.0.0.1/queryAllUserinfo");
      /*  List<UserInfo> users = remoteServiceInterface.queryAllUserinfo();

        RemoteClient.LOGGER.info("users.size() = " + users.size());*/

        // 保证连续请求，好观察SingleRegistry关闭后的请求情况
        for (; ; ) {
            List<UserInfo> users = remoteServiceInterface.queryAllUserinfo();
            System.out.println("users.size() = " + users.size());
            synchronized (RemoteClient.WAITOBJECT) {
                RemoteClient.WAITOBJECT.wait(1000);
            }
        }
    }
}