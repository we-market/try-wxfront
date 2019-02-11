package cn.wemarket.wxfront.common.dto;

import cn.wemarket.wxfront.common.StatusEnum;
import cn.wemarket.wxfront.web.function.dto.BaseResponseDTO;

public class WechatBaseResponseDTO extends BaseResponseDTO {
    public WechatBaseResponseDTO(){
        super();
    }
    public WechatBaseResponseDTO(StatusEnum statusEnum) {
        super(statusEnum);
    }
}
