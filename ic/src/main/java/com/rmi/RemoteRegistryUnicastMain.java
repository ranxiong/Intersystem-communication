package com.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RemoteRegistryUnicastMain {
    public static void main(String[] args) throws Exception {
        /*
         * 我们通过LocateRegistry的get方法，寻找一个存在于远程JVM上的RMI注册表
         * */
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);

        // 以下是向远程RMI注册表（绑定/重绑定）RMI Server的Stub。
        // 同样的远程RMI注册表的JVM-classpath下，一定要有这个RMI Server的Stub
        RemoteUnicastServiceImpl remoteService = new RemoteUnicastServiceImpl();

        /*
         * 在不写LocateRegistry.createRegistry(1099);的情况下。
         * 下面这句代码就是注册 远程RMI注册表 （运行在另外一个JVM上的RMI注册表，
         * 可能是同一台物理机也可能不是同一台物理机）
         *
         * 注册的RMI注册表存在于192.168.61.1这个IP上
         *
         * 使用注册表registry进行绑定或者重绑定时，不需要写完整的RMI URL
         * */
        registry.rebind("queryAllUserinfo" , remoteService);
    }
}