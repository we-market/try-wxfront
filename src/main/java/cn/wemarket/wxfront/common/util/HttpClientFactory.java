package cn.wemarket.wxfront.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.Consts;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@Component("httpClientFactory")
public class HttpClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientFactory.class);

    @Value("${try.wxfront.httpclient.validate.inactivitymillseconds:1000}")
    private int validateInactivityMillSeconds;

    @Value("${try.wxfront.httpclient.message.maxheadercount:200}")
    private int messageMaxHeaderCount;

    @Value("${try.wxfront.httpclient.message.maxlinelength:2000}")
    private int messageMaxLineLength;

    @Value("${try.wxfront.httpclient.connection.maxtotal:200}")
    private int connMaxTotal;

    @Value("${try.wxfront.httpclient.connection.defaultmaxperroute:200}")
    private int connDefaultMaxPerRoute;

    @Value("${try.wxfront.httpclient.connection.evictidletimoutmillseconds:5000}")
    private int connEvictIdleConnectionsTimeoutMillSeconds;

    @Value("${try.wxfront.httpclient.socket.connecttimeout:3000}")
    private int connectTimeout;

    @Value("${try.wxfront.httpclient.socket.sockettimeout:5000}")
    private int socketTimeout;

    public static RequestConfig resetRequestConfig(int socketTimeout, int connectTimeout) {
        return RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
                .setExpectContinueEnabled(true).setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout)
                .build();
    }


    /**
     * 创建https单向认证客户端
     *
     * @return httpclient客户端
     */
    public CloseableHttpClient createHttpClientForSimpleHttps() {
        // SSL context for secure connections can be created either based on
        // system or application specific properties.
        SSLContext sslcontext = SSLContexts.createSystemDefault();

        // Create a registry of custom connection socket factories for supported
        // protocol schemes.
        String[] supportedProtocols = new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"};
        String[] supportedCipherSuites = null;
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", new SSLConnectionSocketFactory(sslcontext, supportedProtocols, supportedCipherSuites, new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                }))
                .register("http", new PlainConnectionSocketFactory())
                .build();


        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        // Create socket configuration
        SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
        // Configure the connection manager to use socket configuration either
        // by default or for a specific host.
        connManager.setDefaultSocketConfig(socketConfig);
        // Validate connections after 1 sec of inactivity
        final int fValidateInactivityMillSeconds = validateInactivityMillSeconds;
        connManager.setValidateAfterInactivity(fValidateInactivityMillSeconds);

        // Create message constraints
        MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(messageMaxHeaderCount)
                .setMaxLineLength(messageMaxLineLength).build();
        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8)
                .setMessageConstraints(messageConstraints).build();

        // Configure the connection manager to use connection configuration
        // either
        // by default or for a specific host.
        connManager.setDefaultConnectionConfig(connectionConfig);
        // connManager.setConnectionConfig(new HttpHost("somehost", 80),
        // ConnectionConfig.DEFAULT);

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(connMaxTotal);
        connManager.setDefaultMaxPerRoute(connDefaultMaxPerRoute);

        // Use custom cookie store if necessary.
        CookieStore cookieStore = new BasicCookieStore();
        // Use custom credentials provider if necessary.
        //CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        // Create global request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
                .setExpectContinueEnabled(true).setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout)
                .build();

        // Create an HttpClient with the given custom dependencies and
        // configuration.
        HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connManager)
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(defaultRequestConfig).evictExpiredConnections()
                .evictIdleConnections(connEvictIdleConnectionsTimeoutMillSeconds, TimeUnit.MILLISECONDS);

        return httpClientBuilder.build();

    }

    /**
     * 创建https单向认证客户端,不用代理
     *
     * @return httpclient客户端
     */
    public CloseableHttpClient createHttpClientForSimpleHttpsWithoutProxy() {
        // SSL context for secure connections can be created either based on
        // system or application specific properties.
        SSLContext sslcontext = SSLContexts.createSystemDefault();

        // Create a registry of custom connection socket factories for supported
        // protocol schemes.
        String[] supportedProtocols = new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"};//new String[]{};
        String[] supportedCipherSuites = null;
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", new SSLConnectionSocketFactory(sslcontext, supportedProtocols, supportedCipherSuites, new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        LOG.info("host name|{}", s);
                        return true;

                    }
                }))
                .register("http", new PlainConnectionSocketFactory())
                .build();


        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        // Create socket configuration
        SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
        // Configure the connection manager to use socket configuration either
        // by default or for a specific host.
        connManager.setDefaultSocketConfig(socketConfig);
        // Validate connections after 1 sec of inactivity
        final int fValidateInactivityMillSeconds = validateInactivityMillSeconds;
        connManager.setValidateAfterInactivity(fValidateInactivityMillSeconds);

        // Create message constraints
        MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(messageMaxHeaderCount)
                .setMaxLineLength(messageMaxLineLength).build();
        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8)
                .setMessageConstraints(messageConstraints).build();

        // Configure the connection manager to use connection configuration
        // either
        // by default or for a specific host.
        connManager.setDefaultConnectionConfig(connectionConfig);
        // connManager.setConnectionConfig(new HttpHost("somehost", 80),
        // ConnectionConfig.DEFAULT);

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(connMaxTotal);
        connManager.setDefaultMaxPerRoute(connDefaultMaxPerRoute);

        // Use custom cookie store if necessary.
        CookieStore cookieStore = new BasicCookieStore();
        // Use custom credentials provider if necessary.
        //CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        // Create global request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
                .setExpectContinueEnabled(true).setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout)
                .build();

        // Create an HttpClient with the given custom dependencies and
        // configuration.
        HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connManager)
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(defaultRequestConfig).evictExpiredConnections()
                .evictIdleConnections(connEvictIdleConnectionsTimeoutMillSeconds, TimeUnit.MILLISECONDS);

        return httpClientBuilder.build();

    }
    
    /**
     * 创建https双向认证客户端 （不需要根证书）
     *
     * @param keyStoreInputStream    客户端证书
     * @param keyStorePassword 客户端证书密码
     * @return httpclient客户端
     * @throws Exception 
     */
    public CloseableHttpClient createHttpClientForCertificationHttpsNoTrust(InputStream keyStoreInputStream, String keyStorePassword, int socketTimeout) throws Exception {
        // SSL context for secure connections can be created either based on
        // system or application specific properties.
        Assert.notNull(keyStoreInputStream, "keyStoreInputStream is null");

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            throw new Exception("key store fail", e);
        }

        try {
            keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());
        } catch (Exception e) {
            throw new Exception("key store load fail", e);
        } finally {
            if (keyStoreInputStream != null) {
                try {
                    keyStoreInputStream.close();
                } catch (IOException ex) {
                    LOG.warn("keyStore inputstream close fail", ex);
                }
            }
        }
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom().//useProtocol("TLSv1.2").
                    loadKeyMaterial(keyStore, keyStorePassword.toCharArray()).build();
        } catch (Exception e) {
            throw new Exception("key store fail", e);
        }

        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        // Create a registry of custom connection socket factories for supported
        // protocol schemes.
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                //.register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();

        // Use custom DNS resolver to override the system DNS resolution.
        DnsResolver dnsResolver = new SystemDefaultDnsResolver() {

            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                boolean ipv4 = InetAddressUtils.isIPv4Address(host);
                if (ipv4) {
                    String[] splits = host.split("\\.");
                    byte[] hostByte = new byte[]{
                            (byte) ((int) (Integer.parseInt(splits[0]) & 255L)),
                            (byte) ((int) (Integer.parseInt(splits[1]) & 255L)),
                            (byte) ((int) (Integer.parseInt(splits[2]) & 255L)),
                            (byte) ((int) (Integer.parseInt(splits[3]) & 255L))
                    };
                    return new InetAddress[]{InetAddress.getByAddress(host, hostByte)};
                } else {
                    return super.resolve(host);
                }
            }

        };

        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry, dnsResolver);

        // Create socket configuration
        SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
        // Configure the connection manager to use socket configuration either
        // by default or for a specific host.
        connManager.setDefaultSocketConfig(socketConfig);
        // Validate connections after 1 sec of inactivity
        final int fValidateInactivityMillSeconds = validateInactivityMillSeconds;
        connManager.setValidateAfterInactivity(fValidateInactivityMillSeconds);

        // Create message constraints
        MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(messageMaxHeaderCount)
                .setMaxLineLength(messageMaxLineLength).build();
        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8)
                .setMessageConstraints(messageConstraints).build();

        // Configure the connection manager to use connection configuration
        // either
        // by default or for a specific host.
        connManager.setDefaultConnectionConfig(connectionConfig);
        // connManager.setConnectionConfig(new HttpHost("somehost", 80),
        // ConnectionConfig.DEFAULT);

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(connMaxTotal);
        connManager.setDefaultMaxPerRoute(connDefaultMaxPerRoute);

        // Use custom cookie store if necessary.
        CookieStore cookieStore = new BasicCookieStore();
        // Use custom credentials provider if necessary.
        //CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        // Create global request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
                .setExpectContinueEnabled(true).setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout)
                .build();

        // Create an HttpClient with the given custom dependencies and
        // configuration.

        HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connManager)
            .setDefaultCookieStore(cookieStore)
            .setDefaultRequestConfig(defaultRequestConfig).evictExpiredConnections()
            .evictIdleConnections(connEvictIdleConnectionsTimeoutMillSeconds, TimeUnit.MILLISECONDS)
            .setSSLSocketFactory(sslsf);
        
        CloseableHttpClient httpclient = httpClientBuilder.build();
        return httpclient;
    }

}
