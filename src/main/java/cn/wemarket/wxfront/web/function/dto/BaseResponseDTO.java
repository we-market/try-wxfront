package cn.wemarket.wxfront.web.function.dto;

import cn.wemarket.wxfront.common.StatusEnum;

public class BaseResponseDTO extends BaseDTO {
    //错误码
    private String errcode;

    //错误码描述
    private String errmsg;

    public BaseResponseDTO(){

    }

    public BaseResponseDTO(StatusEnum statusEnum){
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

    public void setResponseStatus(StatusEnum statusEnum){
        this.errcode = statusEnum.getCode();
        this.errmsg = statusEnum.getMsg();
    }
}
