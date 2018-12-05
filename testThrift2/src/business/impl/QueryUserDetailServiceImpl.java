package business.impl;

import net.sf.json.JSONObject;
import business.BusinessService;
import business.exception.BizException;
import business.pojo.UserPojo;

/**
 * 查询用户详细信息的服务
 */
public class QueryUserDetailServiceImpl implements BusinessService<UserPojo> {

    /* (non-Javadoc)
     * @see business.BusinessService#handle(net.sf.json.JSONObject)
     */
    @Override
    public UserPojo handle(JSONObject params) throws BizException {
        UserPojo user = new UserPojo();
        user.setUserAddr("用户地址");
        user.setUserName("用户姓名");
        user.setUserSex(1);

        return user;
    }
}