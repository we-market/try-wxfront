package cn.wemarket.wxfront.common.dto;

import cn.wemarket.wxfront.common.StatusEnum;

public class WechatBaseResponseDTO {
    //错误码
    private String errcode;

    //错误码描述
    private String errmsg;

    public WechatBaseResponseDTO(StatusEnum statusEnum){
        this.errcode = statusEnum.getCode();
        this.errmsg = statusEnum.getMsg();
    }

    public String getErrcode() {
        return errcode;
    }

    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
