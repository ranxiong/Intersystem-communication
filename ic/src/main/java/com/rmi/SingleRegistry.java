package com.rmi;

import java.rmi.registry.LocateRegistry;

/**
 * 就是一个RMI注册仓库，不负责其它事情。<br>
 * 创建仓库后，锁住WAITOBJECT对象，保证整个程序不会退出
 * @author yinwenjie
 */
public class SingleRegistry {

    private static final Object WAITOBJECT = new Object();

    public static void main(String[] args) throws Exception {
        LocateRegistry.createRegistry(1099);
        synchronized (WAITOBJECT) {
            WAITOBJECT.wait();
        }
    }
}