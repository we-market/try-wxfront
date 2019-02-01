package cn.wemarket.wxfront.controller;

import cn.wemarket.wxfront.biz.service.WeChatService;
import cn.wemarket.wxfront.common.dto.WeChatLoginRequestDTO;
import cn.wemarket.wxfront.common.dto.WechatBaseResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/login")
public class LoginController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    @Qualifier("cn.wemarket.wxfront.biz.service.WeChatService")
    private WeChatService weChatService;

    /**
     * 小程序登陆
     * */
    public @ResponseBody WechatBaseResponseDTO Login(
            @RequestBody WeChatLoginRequestDTO requestDTO,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse){
        LOGGER.info("User Login |request {}", requestDTO);

        return weChatService.login(requestDTO);
    }
}
