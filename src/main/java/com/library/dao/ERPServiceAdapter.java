package com.library.dao;


import com.library.response.MessageCode;
import com.library.validation.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.StringWriter;

@Component
@Profile("!stub")
public class ERPServiceAdapter {

    @Value("${SERVICE_SCHEME}")
    private String serviceScheme;
    @Value("${SERVICE_HOST}")
    private String serviceHost;
    @Value("${SERVICE_PORT}")
    private int servicePort;
    @Value("${SERVICE_USERNAME}")
    private String serviceUserName;
    @Value("${SERVICE_PASSWORD}")
    private String servicePassword;

    public static Integer DEFAULT_TIMEOUT = 120000;
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String HTTP_HEADER_ACCEPT = "Accept";
    public static final String HTTP_HEADER_ACCEPT_CHARSET = "Accept-Charset";
    public static final String ACCEPT_CHARSET_UTF8 = "UTF-8";

    public String getRequest(String serviceUrl) throws IOException {
        serviceUrl = generateUrl(serviceUrl);
        CloseableHttpResponse response = get(serviceUrl);
        return getResponseString(response);
    }

    public CloseableHttpResponse get(String serviceUrl) throws IOException {
        HttpGet httpGet = new HttpGet(serviceUrl);
        CloseableHttpClient httpClient = getHttpClient();
        HttpHost target = new HttpHost(serviceHost, servicePort, serviceScheme);
        HttpClientContext context = getHttpClientContext(target);
        return httpClient.execute(target, httpGet, context);
    }

    public String post(Object object, String serviceUrl) throws IOException {
        serviceUrl = generateUrl(serviceUrl);
        CloseableHttpResponse response = post(new StringEntity(getJsonString(object)), serviceUrl);
        return getResponseString(response);
    }

    public CloseableHttpResponse post(StringEntity entity, String serviceUrl) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        HttpHost target = new HttpHost(serviceHost, servicePort, serviceScheme);
        HttpClientContext context = getHttpClientContext(target);
        HttpPost httpPost = new HttpPost(serviceUrl);
        httpPost.setHeader(HTTP_HEADER_CONTENT_TYPE, APPLICATION_JSON);
        httpPost.setHeader(HTTP_HEADER_ACCEPT, APPLICATION_JSON);
        httpPost.setHeader(HTTP_HEADER_ACCEPT_CHARSET, ACCEPT_CHARSET_UTF8);
        httpPost.setEntity(entity);
        return httpClient.execute(target, httpPost, context);
    }

    public String put(Object object, String serviceUrl) throws IOException {
        serviceUrl = generateUrl(serviceUrl);
        CloseableHttpResponse response = put(new StringEntity(getJsonString(object)), serviceUrl);
        return getResponseString(response);
    }

    public CloseableHttpResponse put(StringEntity entity, String serviceUrl) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        HttpHost target = new HttpHost(serviceHost, servicePort, serviceScheme);
        HttpClientContext context = getHttpClientContext(target);
        HttpPut httpPut = new HttpPut(serviceUrl);
        httpPut.setHeader(HTTP_HEADER_CONTENT_TYPE, APPLICATION_JSON);
        httpPut.setHeader(HTTP_HEADER_ACCEPT, APPLICATION_JSON);
        httpPut.setHeader(HTTP_HEADER_ACCEPT_CHARSET, ACCEPT_CHARSET_UTF8);
        httpPut.setEntity(entity);
        return httpClient.execute(target, httpPut, context);
    }

    private CloseableHttpClient getHttpClient() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(serviceHost, servicePort),new UsernamePasswordCredentials(serviceUserName, servicePassword));
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                                                            .setSocketTimeout(DEFAULT_TIMEOUT)
                                                            .setConnectTimeout(DEFAULT_TIMEOUT)
                                                            .setConnectionRequestTimeout(DEFAULT_TIMEOUT)
                                                            .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                                                    .setDefaultRequestConfig(defaultRequestConfig)
                                                    .build();
        return httpClient;
    }

    private HttpClientContext getHttpClientContext(HttpHost target) {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(target, basicAuth);
        HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(authCache);
        return context;
    }

    public <T> T getObject(String response, Class<T> className) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, className);
    }

    protected String getResponseString(CloseableHttpResponse httpResponse) throws IOException {

        HttpStatus httpStatus = null;

        if (httpResponse != null && httpResponse.getStatusLine() != null) {
            httpStatus = HttpStatus.valueOf(httpResponse.getStatusLine().getStatusCode());
        }

        StringWriter writer = new StringWriter();

        if (httpStatus != null && httpResponse.getEntity() != null && httpResponse.getEntity().getContent() != null && httpResponse.getEntity().getContentLength() != 0) {
            IOUtils.copy(httpResponse.getEntity().getContent(), writer, "UTF-8");
        }

        String response = writer.toString();

        if (httpStatus != null && (httpStatus.is4xxClientError() || httpStatus.is5xxServerError())) {
            throw new ServiceException(MessageCode.UNKNOWN_ERROR, httpResponse.getStatusLine().getReasonPhrase()+"\n"+response, httpStatus.value());
        }

        return response;
    }

    private String generateUrl(String pathUrl) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(serviceScheme)
                .host(serviceHost)
                .port(servicePort)
                .path(pathUrl)
                .build();
        return uriComponents.toUriString();
    }

    private String getJsonString(Object object) throws JsonProcessingException {
        if (object != null) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        }
        return null;
    }
}
