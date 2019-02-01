package cn.wemarket.wxfront.common.dto;

public class WeChatLoginRequestDTO extends WeChatBaseRequestDTO{
    private String appId;
    private String jsCode;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getJsCode() {
        return jsCode;
    }

    public void setJsCode(String jsCode) {
        this.jsCode = jsCode;
    }
}
