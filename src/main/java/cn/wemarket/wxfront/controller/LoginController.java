package cn.wemarket.wxfront.controller;

import cn.wemarket.wxfront.biz.service.WeChatService;
import cn.wemarket.wxfront.common.dto.WeChatLoginRequestDTO;
import cn.wemarket.wxfront.common.dto.WeChatLoginResponseDTO;
import cn.wemarket.wxfront.common.dto.WechatBaseResponseDTO;
import cn.wemarket.wxfront.web.function.dto.BizErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/mini")
public class LoginController extends WeChatBaseController{
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    @Qualifier("cn.wemarket.wxfront.biz.service.WeChatService")
    private WeChatService weChatService;

    /**
     * 小程序登陆
     * */
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public DeferredResult<WechatBaseResponseDTO> Login(
            @RequestParam(value = "appid", required = true) String appId,
            @RequestParam(value = "js_code", required = true) String jsCode,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse){
        LOGGER.info("User Login |request appid:{}, js_code:{}", appId, jsCode);
        WeChatLoginRequestDTO loginRequestDTO = new WeChatLoginRequestDTO();
        loginRequestDTO.setAppId(appId);
        loginRequestDTO.setJsCode(jsCode);

        WeChatLoginResponseDTO defaultErrorLoginResponseDTO = new WeChatLoginResponseDTO();
        defaultErrorLoginResponseDTO.setErrcode("1001");
        defaultErrorLoginResponseDTO.setErrmsg("系统繁忙，请稍后再试");

        return this.execute(httpServletRequest, httpServletResponse,loginRequestDTO,
                (WeChatLoginRequestDTO requestDTO, BizErrors errors) ->{
                    return this.weChatService.login(requestDTO);
                }, defaultErrorLoginResponseDTO, defaultErrorLoginResponseDTO, getRequestTimeout());
    }
}
