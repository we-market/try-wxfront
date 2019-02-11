package cn.wemarket.wxfront.web.function.controller;

import cn.wemarket.wxfront.common.StatusEnum;
import cn.wemarket.wxfront.web.function.dto.BaseResponseDTO;
import cn.wemarket.wxfront.web.function.ExecServiceTemplate;
import cn.wemarket.wxfront.web.function.dto.BaseDTO;
import cn.wemarket.wxfront.web.function.dto.BizError;
import cn.wemarket.wxfront.web.function.dto.BizErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public abstract class BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);
    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

    @Value("${controller.execute.defaultmaxtimeout:100000}")
    private int defaultMaxTimeout = 100_000;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    @Qualifier("frontTaskExecutor")
    private ThreadPoolTaskExecutor frontTaskExecutor;

    @Autowired
    @Qualifier("messageSource")
    private ReloadableResourceBundleMessageSource bundleMessageSource;

    public int getDefaultMaxTimeout() {
        return defaultMaxTimeout;
    }

    public void setDefaultMaxTimeout(int defaultMaxTimeout) {
        this.defaultMaxTimeout = defaultMaxTimeout;
    }

    public RequestMappingHandlerMapping getHandlerMapping() {
        return handlerMapping;
    }

    public void setHandlerMapping(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    protected Locale getLocale(HttpServletRequest request) {
        Locale locale = (Locale) WebUtils.getSessionAttribute(request,
                SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);

        if (locale == null) {
            locale = request.getLocale();
        }

        if (locale == null) {
            locale = Locale.CHINESE;
        }

        return locale;
    }

    /**
     * 构造超时DeferredResult。
     *
     * @param timeoutMilliSeconds                   超时时间，单位微秒
     * @param <T>                                   消息对象
     * @param defaultTimeoutReturndefaultWebMessage 缺省超时返回的对象
     * @return DeferredResult封装的WebMessage&lt;T&gt; 对象
     */
    protected <T> DeferredResult<T> buildDeferredResultWithTimeout(long timeoutMilliSeconds, T defaultTimeoutReturndefaultWebMessage) {

        DeferredResult<T> deferredResult = new DeferredResult<T>(timeoutMilliSeconds, defaultTimeoutReturndefaultWebMessage);
        return deferredResult;
    }

    protected <E extends BaseDTO, T> DeferredResult<T> execute(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            E requestDTO,
            ExecServiceTemplate<E, T> template,
            T timecoutReturnDefaultObject,
            T exceptionReturnDefaultObject,
            long timeoutMilliSeconds) {

            return execute(httpServletRequest,
                    httpServletResponse,
                    requestDTO,
                    timeoutMilliSeconds,
                    template,
                    timecoutReturnDefaultObject,
                    exceptionReturnDefaultObject,
                    false);
    }

    /**
     * Controller类的入口方法。
     *
     * @param httpServletRequest               HttpServlet请求，主要为了处理国际化参数,预留接口
     * @param httpServlvetResponse             HttpServlet响应
     * @param requestDTO                       业务请求对象，继承BaseDTO
     * @param timeoutMilliSeconds              服务请求超时时间，单位:秒
     * @param template                         执行模板，主要为了封装固有的业务处理流程
     * @param <E>                              请求对象
     * @param <T>                              返回对象
     * @param timeoutReturndefaultWebMessage   超时返回对象
     * @param exceptionReturndefaultWebMessage 异常返回对象
     * @return DeferredResult&lt;WebMessage&lt;T&gt;&gt;
     */

    protected <E extends BaseDTO, T> DeferredResult<T> execute(
            HttpServletRequest httpServletRequest, HttpServletResponse httpServlvetResponse,
            E requestDTO, long timeoutMilliSeconds, ExecServiceTemplate<E, T> template,
            T timeoutReturndefaultWebMessage,
            T exceptionReturndefaultWebMessage, boolean throwHttpResponseErrorWhenException) {
        DeferredResult<T> deferredResult = null;
        try {
            // 检查超时时间不能超过defaultMaxTimeout秒，默认100秒（100_000）
            Assert.isTrue(timeoutMilliSeconds <= defaultMaxTimeout,
                    String.format("timeout is invalid:%s milliseconds more than %s milliseconds",
                            timeoutMilliSeconds, defaultMaxTimeout));

            deferredResult = buildDeferredResultWithTimeout(timeoutMilliSeconds, timeoutReturndefaultWebMessage);

            Method method = null;
            if (this.getHandlerMapping() != null) {
                HandlerExecutionChain handler =
                        this.getHandlerMapping().getHandler(httpServletRequest);
                HandlerMethod hm = (HandlerMethod) handler.getHandler();
                method = hm.getMethod();
            }

            String requestUri = URL_PATH_HELPER.getRequestUri(httpServletRequest);
            Locale locale = getLocale(httpServletRequest);
            DeferredResultRunnable<E, T> r = new DeferredResultRunnable<E, T>(method, requestUri,
                    deferredResult, template, requestDTO, locale, bundleMessageSource,
                    httpServletRequest, httpServlvetResponse, exceptionReturndefaultWebMessage,
                    throwHttpResponseErrorWhenException);

            frontTaskExecutor.submit(r);
        } catch (Exception e) {
            LOGGER.error("execute fail", e);


            T m = exceptionReturndefaultWebMessage;

            if (m != null && (m instanceof BaseResponseDTO)) {
                ((BaseResponseDTO) m)
                        .setResponseStatus(StatusEnum.FRONT_INTERNAL_SERVER_ERROR);
            }

            deferredResult = new DeferredResult<T>();
            deferredResult.setResult(m);

            if (throwHttpResponseErrorWhenException) {
                httpServlvetResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
        return deferredResult;
    }

    protected <E extends BaseDTO> BaseResponseDTO preService(E requestDto,
                                                         HttpServletRequest httpRequest, HttpServletResponse httpResponse, Method callerMethod,
                                                         String requestUri) {
        BaseResponseDTO message = new BaseResponseDTO();
        message.setResponseStatus(StatusEnum.SUCCESS);
        return message;
    }

    /**
     * 异步提交服务请求的线程类。
     */
    private class DeferredResultRunnable<E extends BaseDTO, T> implements Runnable {
        private DeferredResult<T> deferredResult;
        private ExecServiceTemplate<E, T> service;
        private E requestDto;
        private Locale locale;
        private ReloadableResourceBundleMessageSource bundleMessageSource;
        private Method callerMethod;
        private HttpServletRequest httpServletRequest;
        private HttpServletResponse httpServletResponse;
        private String requestUri;
        private T exceptionReturnDefaultObject;

        private boolean throwHttpResponseErrorWhenException;

        public DeferredResultRunnable(Method callerMethod, String requestUri, DeferredResult<T> d,
                                      ExecServiceTemplate<E, T> s, E requestDto, Locale locale,
                                      ReloadableResourceBundleMessageSource bundleMessageSource,
                                      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                      T exceptionReturnDefaultObject, boolean throwHttpResponseErrorWhenException) {
            this.deferredResult = d;
            this.requestUri = requestUri;
            this.service = s;
            this.requestDto = requestDto;
            this.locale = locale;
            this.bundleMessageSource = bundleMessageSource;
            this.callerMethod = callerMethod;
            this.httpServletRequest = httpServletRequest;
            this.httpServletResponse = httpServletResponse;
            this.exceptionReturnDefaultObject = exceptionReturnDefaultObject;
            this.throwHttpResponseErrorWhenException = throwHttpResponseErrorWhenException;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            T responseObject = null;
            T result = null;
            long beginTime = System.currentTimeMillis();
            String callerClassName = "unknow";
            String callerMethodName = "unknow";
            String statusCode = StatusEnum.SUCCESS.getCode();
            try {
                // 1.set context bind thread
                if (this.callerMethod != null) {
                    callerClassName = this.callerMethod.getDeclaringClass().getName();
                    callerMethodName = this.callerMethod.getName();
                }
                BaseResponseDTO preServiceResult = preService(requestDto, httpServletRequest,
                        httpServletResponse, this.callerMethod, this.requestUri);

                // 1.2 安全检测不过
                if (!StatusEnum.SUCCESS.getCode().equals(preServiceResult.getErrcode())) {
                    T returnMsg = exceptionReturnDefaultObject;
                    if (!StatusEnum.REQUEST_UNSAFE.getCode()
                            .equals(preServiceResult.getErrcode())) {

                        if (returnMsg instanceof BaseResponseDTO) {
                            ((BaseResponseDTO) returnMsg).setErrcode(preServiceResult.getErrcode());
                            ((BaseResponseDTO) returnMsg).setErrmsg(preServiceResult.getErrmsg());
                        }


                    }

                    deferredResult.setResult(returnMsg);
                    return;
                }


                // 2.call biz service
                BizErrors bizErrors = new BizErrors();

                result = this.service.apply(requestDto, bizErrors);

                responseObject =
                        handleSuccessResponseMessage(result, bizErrors, locale, bundleMessageSource);


                postService(requestDto, responseObject, this.httpServletRequest,
                        this.httpServletResponse, this.callerMethod, this.requestUri, bizErrors);

            } catch (Exception e) {
                // 3.error handle
                LOGGER.error("service error,request message:" + requestDto, e);
                responseObject = handleFailResponseMessage(e, this.exceptionReturnDefaultObject);

                if (this.throwHttpResponseErrorWhenException) {
                    httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                }

            } finally {
                deferredResult.setResult(responseObject);

                long endTime = System.currentTimeMillis();
                long responseTime = endTime - beginTime;

            }


        }
    }

    /**
     * 处理正常返回。
     *
     * @param result              业务服务返回对象
     * @param bizErrors           业务异常
     * @param locale              区域
     * @param bundleMessageSource 国际化消息源
     * @param <T>                 WebMesage包含的对象类型
     * @return WebMessage&lt;T&gt; 前端消息
     */
    protected <T> T handleSuccessResponseMessage(T result, BizErrors bizErrors, Locale locale,
                                                 ReloadableResourceBundleMessageSource bundleMessageSource) {
        final Locale myLocale = locale == null ? Locale.getDefault() : locale;

        if (bizErrors != null && bizErrors.hasErrors()) {
            List<BizError> allErrors = bizErrors.getAllErrors();
            BizError bizError = allErrors.get(allErrors.size() - 1);
            String code = bizError.getCode();
            String msg = bundleMessageSource.getMessage(bizError.getCode(), bizError.getArguments(),
                    bizError.getDefaultMessage(), myLocale);
            if (result != null && result instanceof BaseResponseDTO) {
                ((BaseResponseDTO) result).setErrcode(code);
                ((BaseResponseDTO) result).setErrmsg(msg);
            }

        }
        return result;
    }

    /**
     * 处理异常返回。
     *
     * @param e                                系统异常
     * @param <T>                              返回包含在WebMessage的对象类型
     * @param exceptionReturndefaultWebMessage 缺省异常时返回的对象
     * @return 封装后的前端WebMessage对象, 包含服务返回的结果对象和状态码
     */
    protected <T> T handleFailResponseMessage(Exception e, T exceptionReturndefaultWebMessage) {
        T m = exceptionReturndefaultWebMessage;

        if (m != null && (m instanceof BaseResponseDTO)) {
            ((BaseResponseDTO) m).setResponseStatus(StatusEnum.FRONT_INTERNAL_SERVER_ERROR);
        }
        return m;
    }

    /**
     * 调用服务后，处理请求结果。
     *
     * @param requestDto          请求对象
     * @param responseDto         请求结果
     * @param <E>                 请求对象
     * @param <T>                 返回对象
     * @param httpServletRequest  http请求
     * @param httpServletResponse http响应
     * @param callerMethod        调用服务的controller方法
     * @param requestUri          请求uri
     * @param errors              业务异常
     */
    protected abstract <E extends BaseDTO, T> void postService(E requestDto, T responseDto,
                                                               HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                                               Method callerMethod, String requestUri, BizErrors errors);
}
