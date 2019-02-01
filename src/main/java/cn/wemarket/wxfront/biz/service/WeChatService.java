package cn.wemarket.wxfront.biz.service;

import cn.wemarket.wxfront.common.dto.WeChatLoginRequestDTO;
import cn.wemarket.wxfront.common.dto.WechatBaseResponseDTO;

public interface WeChatService {
    public WechatBaseResponseDTO login(WeChatLoginRequestDTO requestDTO);
}
