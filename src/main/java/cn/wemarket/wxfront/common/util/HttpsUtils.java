package cn.wemarket.wxfront.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component("httpsUtils")
public class HttpsUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpsUtils.class);

  private static CloseableHttpClient simpleHttpsClient = null;
  private static CloseableHttpClient certHttpsClient = null;
  private static CloseableHttpClient simpleHttpsClientWithoutProxy = null;

  private final static Map<String, CloseableHttpClient> httpsClientCache = new ConcurrentHashMap<>();
  private static ResourceLoader resourceLoader = new DefaultResourceLoader();

  @Autowired
  private HttpClientFactory httpClientFactory;


  public HttpClientFactory getHttpClientFactory() {
    return httpClientFactory;
  }

  public void setHttpClientFactory(HttpClientFactory httpClientFactory) {
    this.httpClientFactory = httpClientFactory;
  }


  public void closeHttpClients() {
    if (simpleHttpsClient != null) {
      try {
        simpleHttpsClient.close();
      } catch (IOException e) {
        LOGGER.error("close httpclient fail", e);
      }

    }

    if (certHttpsClient != null) {
      try {
        certHttpsClient.close();
      } catch (IOException e) {
        LOGGER.error("close httpclient fail", e);
      }

    }
  }

  public String postHttpsRequest(String path, String param) throws Exception {

    long start = System.currentTimeMillis();
    String result = null;

    if (simpleHttpsClient == null) {
      simpleHttpsClient = httpClientFactory.createHttpClientForSimpleHttps();
    }

    HttpPost httpPost = new HttpPost(path);
    //设置请求头格式
    httpPost.setHeader("Accept", "application/json");
    StringEntity reqentity = new StringEntity(param, Consts.UTF_8);
    httpPost.setEntity(reqentity);
    CloseableHttpResponse response = null;
    try {
      response = simpleHttpsClient.execute(httpPost);
      result = EntityUtils.toString(response.getEntity(), Consts.UTF_8).trim();
    } catch (Exception e) {
      LOGGER.error("post fail, url :" + path, e);
    } finally {
      if (response != null) {
        try {
          response.close();
        } catch (Exception e) {
          LOGGER.error("response close fail", e);
        }
      }
      httpPost.releaseConnection();

      long end = System.currentTimeMillis();
      LOGGER.info("http post|path|{}|param|{}|response|{}|elapse milliseconds|{}", path, param, result != null ? StringUtils.replaceAll(result, ">\n", ">") : result, end - start);
    }
    return result;

  }

  public String upload(String path, HttpEntity entity) throws Exception {

    long start = System.currentTimeMillis();
    String result = null;

    if (simpleHttpsClient == null) {
      simpleHttpsClient = httpClientFactory.createHttpClientForSimpleHttps();
    }

    HttpPost httpPost = new HttpPost(path);
    httpPost.setEntity(entity);
    CloseableHttpResponse response = null;

    try {
      response = simpleHttpsClient.execute(httpPost);
      result = EntityUtils.toString(response.getEntity(), Consts.UTF_8).trim();
    } catch (Exception e) {
      LOGGER.error("upload fail, url: " + path, e);
    } finally {
      if (response != null) {
        try {
          response.close();
        } catch (Exception e) {
          LOGGER.error("response close fail", e);
        }
      }
      httpPost.releaseConnection();

      long end = System.currentTimeMillis();
      LOGGER.info("upload|path|{}|response|{}|milliseconds|{}", path, result, end - start);
    }
    return result;

  }

  public String getHttpsRequest(String path) throws Exception {

    long start = System.currentTimeMillis();
    String result = null;

    if (simpleHttpsClient == null) {
      simpleHttpsClient = httpClientFactory.createHttpClientForSimpleHttps();
    }

    HttpGet httpGet = new HttpGet(path);

    CloseableHttpResponse response = null;
    try {
      response = simpleHttpsClient.execute(httpGet);
      result = EntityUtils.toString(response.getEntity(), Consts.UTF_8).trim();
    } catch (Exception e) {
      LOGGER.error("get fail, url: " + path, e);
    } finally {
      if (response != null) {
        try {
          response.close();
        } catch (Exception e) {
          LOGGER.error("response close fail", e);
        }
      }
      if (httpGet != null) {
        httpGet.releaseConnection();
      }

      long end = System.currentTimeMillis();
      LOGGER.info("http get|path|{}|response|{}|milliseconds|{}", path, result, end - start);

    }


    return result;

  }

  public String getHttpsRequestWithoutProxy(String path) throws Exception {

    long start = System.currentTimeMillis();
    String result = null;

    if (simpleHttpsClientWithoutProxy == null) {
      simpleHttpsClientWithoutProxy = httpClientFactory.createHttpClientForSimpleHttpsWithoutProxy();
    }

    HttpGet httpGet = new HttpGet(path);

    CloseableHttpResponse response = null;
    try {
      response = simpleHttpsClientWithoutProxy.execute(httpGet);
      result = EntityUtils.toString(response.getEntity(), Consts.UTF_8).trim();
    } catch (Exception e) {
      LOGGER.error("post fail, url: " + path, e);
    } finally {
      if (response != null) {
        try {
          response.close();
        } catch (Exception e) {
          LOGGER.error("response close fail", e);
        }
      }
      if (httpGet != null) {
        httpGet.releaseConnection();
      }

      long end = System.currentTimeMillis();
      LOGGER.info("http get|path|{}|response|{}|milliseconds|{}", path, result, end - start);

    }

    return result;

  }


  public String downloadFileWithPost(String url, String param, HttpServletResponse httpServletResponse) throws Exception {
    StringBuffer stringBuffer = new StringBuffer();
    if (simpleHttpsClient == null) {
      simpleHttpsClient = httpClientFactory.createHttpClientForSimpleHttps();
    }
    HttpPost httpPost = new HttpPost(url);
    StringEntity reqentity = new StringEntity(param, Consts.UTF_8);
    httpPost.setEntity(reqentity);
    CloseableHttpResponse response = null;
    OutputStream outputStream = httpServletResponse.getOutputStream();
    response = simpleHttpsClient.execute(httpPost);
    if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
      HttpEntity entity = response.getEntity();
      InputStream in = entity.getContent();
      byte[] buffer = new byte[4096];
      int readLength = 0;
      while ((readLength = in.read(buffer)) > 0) {
        outputStream.write(buffer, 0, readLength);
        stringBuffer.append(new String(buffer, 0, readLength));
      }
      outputStream.flush();
      outputStream.close();
    }
    return stringBuffer.toString();
  }

}
