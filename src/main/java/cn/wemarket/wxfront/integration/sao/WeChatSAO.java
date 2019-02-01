package cn.wemarket.wxfront.integration.sao;

import org.apache.http.NameValuePair;

import java.util.List;

public interface WeChatSAO {
    public String jscode2Session(List<NameValuePair> params);
}
