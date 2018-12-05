package business;

import java.util.HashMap;
import java.util.Map;

public class BusinessServicesMapping {
    /**
     * 直接将服务名称和关联的类，放置在这个常量中
     */
    public static final Map<String, String> SERVICES_MAPPING = new HashMap<String, String>();

    static {
        //business.impl.QueryUserDetailServiceImpl
        SERVICES_MAPPING.put("queryUserDetailService", "business.impl.QueryUserDetailServiceImpl");
    }
}