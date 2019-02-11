package cn.wemarket.wxfront.biz.service;

import cn.wemarket.wxfront.common.dto.WeChatLoginRequestDTO;
import cn.wemarket.wxfront.common.dto.WeChatLoginResponseDTO;

public interface WeChatService {
    public WeChatLoginResponseDTO login(WeChatLoginRequestDTO requestDTO);
}
