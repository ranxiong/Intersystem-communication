package com.bio;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


/**
 * 通过加入线程的概念，让socket server能够在应用层面，
 * 通过非阻塞的方式同时处理多个socket套接字
 *
 *
 */
public class SocketServer6 {

    static {
        BasicConfigurator.configure();
    }

    private static Object xWait = new Object();

    private static final Log LOGGER = LogFactory.getLog(SocketServer6.class);

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(83);
        serverSocket.setSoTimeout(100);
        try {
            while (true) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch (SocketTimeoutException e1) {
                    //===========================================================
                    //      执行到这里，说明本次accept没有接收到任何TCP连接
                    //      主线程在这里就可以做一些事情，记为X
                    //===========================================================
                    synchronized (SocketServer6.xWait) {
                        SocketServer6.LOGGER.info("这次没有从底层接收到任何TCP连接，等待10毫秒，模拟事件X的处理时间");
                        SocketServer6.xWait.wait(10);
                    }
                    continue;
                }
                //当然业务处理过程可以交给一个线程（这里可以使用线程池）,并且线程的创建是很耗资源的。
                //最终改变不了.accept()只能一个一个接受socket连接的情况
                SocketServerThread6 socketServerThread6 = new SocketServerThread6(socket);
                new Thread(socketServerThread6).start();
            }
        } catch (Exception e) {
            SocketServer6.LOGGER.error(e.getMessage(), e);
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}

/**
 * 当然，接收到客户端的socket后，业务的处理过程可以交给一个线程来做。
 * 但还是改变不了socket被一个一个的做accept()的情况。
 *
 *
 */
class SocketServerThread6 implements Runnable {

    /**
     * 日志
     */
    private static final Log LOGGER = LogFactory.getLog(SocketServerThread.class);

    private Socket socket;

    public SocketServerThread6(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            Integer sourcePort = socket.getPort();
            int maxLen = 2048;
            byte[] contextBytes = new byte[maxLen];
            int realLen;
            StringBuffer message = new StringBuffer();
            //下面我们收取信息（设置成非阻塞方式，这样read信息的时候，又可以做一些其他事情）
            this.socket.setSoTimeout(10);
            BIORead:
            while (true) {
                try {
                    while ((realLen = in.read(contextBytes, 0, maxLen)) != -1) {
                        message.append(new String(contextBytes, 0, realLen));
                        /*
                         * 我们假设读取到“over”关键字，
                         * 表示客户端的所有信息在经过若干次传送后，完成
                         * */
                        if (message.indexOf("over") != -1) {
                            break BIORead;
                        }
                    }
                } catch (SocketTimeoutException e2) {
                    //===========================================================
                    //      执行到这里，说明本次read没有接收到任何数据流
                    //      主线程在这里又可以做一些事情，记为Y
                    //===========================================================
                    SocketServerThread6.LOGGER.info("这次没有从底层接收到任务数据报文，等待10毫秒，模拟事件Y的处理时间");
                    continue;
                }
            }
            //下面打印信息
            Long threadId = Thread.currentThread().getId();
            SocketServerThread6.LOGGER.info("服务器(线程：" + threadId + ")收到来自于端口：" + sourcePort + "的信息：" + message);

            //下面开始发送信息
            out.write("回发响应信息！".getBytes());

            //关闭
            out.close();
            in.close();
            this.socket.close();
        } catch (Exception e) {
            SocketServerThread6.LOGGER.error(e.getMessage(), e);
        }
    }
}