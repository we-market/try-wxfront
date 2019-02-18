package cn.wemarket.wxfront.biz.service.impl;

import cn.wemarket.wxfront.biz.service.WeChatService;
import cn.wemarket.wxfront.common.StatusEnum;
import cn.wemarket.wxfront.common.dto.WeChatLoginRequestDTO;
import cn.wemarket.wxfront.common.dto.WeChatLoginResponseDTO;
import cn.wemarket.wxfront.common.dto.WechatBaseResponseDTO;
import cn.wemarket.wxfront.common.util.JsonMapper;
import cn.wemarket.wxfront.integration.sao.WeChatSAO;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("cn.wemarket.wxfront.biz.service.WeChatService")
public class WechatServiceImpl implements WeChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WechatServiceImpl.class);
    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();
    @Value("${wechat.appsecret}")
    private String appSecret;

    @Autowired
    @Qualifier("cn.wemarket.wxfront.integration.sao.WeChatSAO")
    private WeChatSAO weChatSAO;

    @Override
    public WeChatLoginResponseDTO login(WeChatLoginRequestDTO requestDTO) {
        WeChatLoginResponseDTO responseDTO = new WeChatLoginResponseDTO();
        if (StringUtils.isEmpty(requestDTO.getJsCode()) || StringUtils.isEmpty(requestDTO.getAppId())){
            LOGGER.info("invalid request !");
            responseDTO.setResponseStatus(StatusEnum.INVALID_REQUEST);
            return responseDTO;
        }
        List<NameValuePair> params = setURLParams(requestDTO);
        String response = weChatSAO.jscode2Session(params);
        if (response == null){
            LOGGER.info("fail to login...");
            responseDTO.setResponseStatus(StatusEnum.INTERNAL_SERVER_ERROR);
            return responseDTO;
        }

        responseDTO = JSON_MAPPER.fromJson(response, WeChatLoginResponseDTO.class);
        if (responseDTO == null){
            LOGGER.info("fail to parse json...");
            responseDTO.setResponseStatus(StatusEnum.INTERNAL_SERVER_ERROR);
            return responseDTO;
        }

        return responseDTO;
    }

    /**
     * 构造URL请求参数
     * */
    private List<NameValuePair> setURLParams(WeChatLoginRequestDTO requestDTO){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("appid", requestDTO.getAppId()));
        params.add(new BasicNameValuePair("secret", appSecret));
        params.add(new BasicNameValuePair("js_code", requestDTO.getJsCode()));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));

        return params;

    }
}
