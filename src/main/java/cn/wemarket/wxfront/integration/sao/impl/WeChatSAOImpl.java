package cn.wemarket.wxfront.integration.sao.impl;

import cn.wemarket.wxfront.common.util.HttpsUtils;
import cn.wemarket.wxfront.integration.sao.WeChatSAO;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;

@Repository("cn.wemarket.wxfront.integration.sao.WeChatSAO")
public class WeChatSAOImpl implements WeChatSAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeChatSAOImpl.class);

    @Value("${wechat.baseurl}")
    private String weChatBaseUrl;

    @Autowired
    private HttpsUtils httpsUtils;

    @Override
    public String jscode2Session(List<NameValuePair> params) {
        try {
            URI uri = new URIBuilder().setScheme("https").setHost(weChatBaseUrl)
                    .setPath("/sns/jscode2session").setParameters(params).build();
            return httpsUtils.getHttpsRequest(uri.toString());
        } catch (Exception e) {
            LOGGER.error("jscode2Session fail...", e);
            return null;
        }
    }
}
