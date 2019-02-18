package cn.wemarket.wxfront.controller;

import cn.wemarket.wxfront.common.dto.WeChatBaseRequestDTO;
import cn.wemarket.wxfront.web.function.ExecServiceTemplate;
import cn.wemarket.wxfront.web.function.controller.BaseController;
import cn.wemarket.wxfront.web.function.dto.BaseDTO;
import cn.wemarket.wxfront.web.function.dto.BizErrors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class WeChatBaseController extends BaseController {
    @Value("${wemarket.wxfront.web.request-timeout-millseconds}")
    private int requestTimeout;

    public int getRequestTimeout(){
        return requestTimeout;
    }

    @Override
    protected <E extends BaseDTO, T> void postService(E requestDto, T responseDto, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Method callerMethod, String requestUri, BizErrors errors) {

    }

    @Override
    protected <E extends BaseDTO, T> DeferredResult<T> execute(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, E requestDTO, ExecServiceTemplate<E, T> template, T timecoutReturnDefaultObject, T exceptionReturnDefaultObject, long timeoutMilliSeconds) {
        if (requestDTO instanceof WeChatBaseRequestDTO){
            WeChatBaseRequestDTO baseRequestDTO = (WeChatBaseRequestDTO) requestDTO;
        }
        return super.execute(httpServletRequest, httpServletResponse, requestDTO, template, timecoutReturnDefaultObject, exceptionReturnDefaultObject, timeoutMilliSeconds);
    }
}
