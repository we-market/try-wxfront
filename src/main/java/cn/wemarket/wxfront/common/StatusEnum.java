package cn.wemarket.wxfront.common;

public enum  StatusEnum {
    SUCCESS("0", "成功"),
    INVALID_REQUEST("999996","不合法请求"),
    REQUEST_UNSAFE("999997","系统请求不安全"),
    FRONT_INTERNAL_SERVER_ERROR("999998", "系统异常，请您稍后再试"),
    INTERNAL_SERVER_ERROR("9999999", "服务器处理失败，请您稍后再试");

    String code;
    String msg;

    StatusEnum(String code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
