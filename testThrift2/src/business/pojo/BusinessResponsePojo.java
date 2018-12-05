package business.pojo;

import java.io.Serializable;

import business.exception.ResponseCode;

/**
 * 该数据体用于进行返回信息描述。
 */
public class BusinessResponsePojo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -2456254862958624358L;

    public BusinessResponsePojo() {
        this.desc = new DescPojo();
    }

    public BusinessResponsePojo(AbstractPojo data, String result_msg, ResponseCode result_code) {
        this.data = data;
        this.desc = new DescPojo(result_msg, result_code);
    }

    /**
     * 返回的请求结果查询
     */
    private AbstractPojo data;

    private DescPojo desc;

    /**
     * @return the data
     */
    public AbstractPojo getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(AbstractPojo data) {
        this.data = data;
    }

    public DescPojo getDesc() {
        return desc;
    }

    public void setDesc(DescPojo desc) {
        this.desc = desc;
    }
}
