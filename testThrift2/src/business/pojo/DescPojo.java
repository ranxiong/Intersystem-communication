package business.pojo;

import java.io.Serializable;

import business.exception.ResponseCode;

public class DescPojo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4081588423913750219L;

    /**
     * 接口请求的返回代码，从这个代码中可以识别接口调用是否成功，以及调用失败时的错误类型。 具体的code定义参见技术文档
     */
    private ResponseCode result_code;

    /**
     * 错误的中文描述，错误信息的详细中文描述
     */
    private String result_msg = "";

    public DescPojo() {

    }

    public DescPojo(String result_msg, ResponseCode result_code) {
        this.result_msg = result_msg;
        this.result_code = result_code;
    }

    public ResponseCode getResult_code() {
        return result_code;
    }

    public void setResult_code(ResponseCode result_code) {
        this.result_code = result_code;
    }

    public String getResult_msg() {
        return result_msg;
    }

    public void setResult_msg(String result_msg) {
        this.result_msg = result_msg;
    }
}
