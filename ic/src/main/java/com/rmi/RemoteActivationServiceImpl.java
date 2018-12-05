package com.rmi;

import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.rmi.activation.Activatable;
import java.rmi.activation.ActivationException;
import java.rmi.activation.ActivationID;
import java.util.ArrayList;
import java.util.List;


/**
 * RMI Service的一个具体实现。注意这里我们继承的是Activatable父类
 *
 *
 */
public class RemoteActivationServiceImpl extends Activatable implements RemoteServiceInterface {

    /**
     * 这个构造函数式必须的，不然客户端会报：<br>
     * java.lang.NoSuchMethodException: testRMI.RemoteActivationServiceImpl.<init>(java.rmi.activation.ActivationID, java.rmi.MarshalledObject) 异常<br>
     * 在这个方法里面，您可以调用任何一个Activatable父级的构造函数。这里我们调用的是最简单的一个
     *
     * @throws RemoteException
     * @throws ActivationException
     */
    public RemoteActivationServiceImpl(ActivationID id, MarshalledObject<?> data) throws RemoteException, ActivationException {
        super("file://E:\\testworkspace\\testBSocket\\target\\classes", data, false, 0);
    }

    /**
     *
     */
    private static final long serialVersionUID = 6797720945876437472L;

    /* (non-Javadoc)
     * @see testRMI.RemoteServiceInterface#queryAllUserinfo()
     */
    @Override
    public List<UserInfo> queryAllUserinfo() throws RemoteException {
        List<UserInfo> users = new ArrayList<UserInfo>();

        UserInfo user1 = new UserInfo();
        user1.setUserAge(21);
        user1.setUserDesc("userDesc1");
        user1.setUserName("userName1");
        user1.setUserSex(true);
        users.add(user1);

        UserInfo user2 = new UserInfo();
        user2.setUserAge(21);
        user2.setUserDesc("userDesc2");
        user2.setUserName("userName2");
        user2.setUserSex(false);
        users.add(user2);
        return users;
    }
}