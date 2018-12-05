package processor;

import java.nio.ByteBuffer;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;

import business.BusinessService;
import business.BusinessServicesMapping;
import business.exception.BizException;
import business.exception.ResponseCode;
import business.pojo.AbstractPojo;
import business.pojo.BusinessResponsePojo;
import business.pojo.DescPojo;
import thrift.iface.DIYFrameworkService.Iface;
import thrift.iface.EXCCODE;
import thrift.iface.RESCODE;
import thrift.iface.Reponse;
import thrift.iface.Request;
import thrift.iface.ServiceException;
import utils.JSONUtils;

/**
 * IDL文件中，我们定义的唯一服务接口DIYFrameworkService.Iface的唯一实现
 */
public class DIYFrameworkServiceImpl implements Iface {

    /**
     * 日志
     */
    public static final Log LOGGER = LogFactory.getLog(DIYFrameworkServiceImpl.class);

    /* (non-Javadoc)
     * @see thrift.iface.DIYFrameworkService.Iface#send(thrift.iface.Request)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Reponse send(Request request) throws ServiceException, TException {
        /*
		 * 由于MainProcessor中，在Apache Thrift 服务端启动时已经加入了线程池，所以这里就不需要再使用线程池了
		 * 这个服务方法的实现，需要做以下事情：
		 * 
		 * 1、根据request中，描述的具体服务名称，在配置信息中查找具体的服务类
		 * 2、使用java的反射机制，调用具体的服务类(BusinessService接口的实现类)。
		 * 3、根据具体的业务处理结构，构造Reponse对象，并进行返回
		 * */

        //1、===================
        String serviceName = request.getServiceName();
        String className = BusinessServicesMapping.SERVICES_MAPPING.get(serviceName);
        //未发现服务
        if (StringUtils.isEmpty(className)) {
            return this.buildErrorReponse("无效的服务", null);
        }

        //2、===================
        // 首先得到以json为描述格式的请求参数信息
        JSONObject paramJSON = null;
        try {
            byte[] paramJSON_bytes = request.getParamJSON();
            if (paramJSON_bytes != null && paramJSON_bytes.length > 0) {
                String paramJSON_string = new String(paramJSON_bytes);
                paramJSON = JSONObject.fromObject(paramJSON_string);
            }
        } catch (Exception e) {
            DIYFrameworkServiceImpl.LOGGER.error(e);
            // 向调用者抛出异常
            throw new ServiceException(EXCCODE.PARAMNOTFOUND, e.getMessage());
        }

        // 试图进行反射
        BusinessService<AbstractPojo> businessServiceInstance = null;
        try {
            businessServiceInstance = (BusinessService<AbstractPojo>) Class.forName(className).newInstance();
        } catch (Exception e) {
            DIYFrameworkServiceImpl.LOGGER.error(e);
            // 向调用者抛出异常
            throw new ServiceException(EXCCODE.SERVICENOTFOUND, e.getMessage());
        }

        // 进行调用
        AbstractPojo returnPojo = null;
        try {
            returnPojo = businessServiceInstance.handle(paramJSON);
        } catch (BizException e) {
            DIYFrameworkServiceImpl.LOGGER.error(e);
            return this.buildErrorReponse(e.getMessage(), e.getResponseCode());
        }

        // 构造处理成功情况下的返回信息
        BusinessResponsePojo responsePojo = new BusinessResponsePojo();
        responsePojo.setData(returnPojo);
        DescPojo descPojo = new DescPojo("", ResponseCode._200);
        responsePojo.setDesc(descPojo);

        // 生成json
        String returnString = JSONUtils.toString(responsePojo);
        byte[] returnBytes = returnString.getBytes();
        ByteBuffer returnByteBuffer = ByteBuffer.allocate(returnBytes.length);
        returnByteBuffer.put(returnBytes);
        returnByteBuffer.flip();

        // 构造response
        Reponse reponse = new Reponse(RESCODE._200, returnByteBuffer);
        return reponse;
    }

    /**
     * 这个私有方法，用于构造“Thrift中错误的返回信息”
     *
     * @param erroe_mess
     * @return
     */
    private Reponse buildErrorReponse(String erroe_mess, ResponseCode responseCode) {
        // 构造返回信息
        BusinessResponsePojo responsePojo = new BusinessResponsePojo();
        responsePojo.setData(null);
        DescPojo descPojo = new DescPojo(erroe_mess, responseCode == null ? ResponseCode._504 : responseCode);
        responsePojo.setDesc(descPojo);

        // 存储byteBuffer；
        String responseJSON = JSONUtils.toString(responsePojo);
        byte[] responseJSON_bytes = responseJSON.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(responseJSON_bytes.length);
        byteBuffer.put(byteBuffer);
        Reponse reponse = new Reponse(RESCODE._500, byteBuffer);
        return reponse;
    }
}
