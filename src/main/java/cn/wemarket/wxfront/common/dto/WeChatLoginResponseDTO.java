package cn.wemarket.wxfront.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WeChatLoginResponseDTO extends WechatBaseResponseDTO{
    //用户唯一标识
    private String openid;

    //会话密钥
    @JsonProperty("session_key")
    private String sessionKey;

    //用户在开放平台的唯一标识符
    private String unionid;

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }
}
