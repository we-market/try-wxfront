package cn.wemarket.wxfront.web.function.dto;

public class BizError extends BaseDTO{
    private final String code;
    private final Object[] arguments;
    private final String defaultMessage;


    public BizError(String code, Object[] arguments, String defaultMessage) {
        this.code = code;
        this.arguments = arguments;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
