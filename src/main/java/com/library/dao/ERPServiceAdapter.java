package com.library.dao;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.response.MessageCode;
import com.library.validation.ServiceException;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

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
    public static final String ACCEPT_CHARSET_UTF8 = "UTF-8";

    public String getRequest(String serviceUrl, MediaType mediaType) throws IOException {
        serviceUrl = generateUrl(serviceUrl);
        CloseableHttpResponse response = get(serviceUrl, mediaType);
        return getResponseString(response);
    }

    public CloseableHttpResponse get(String serviceUrl, MediaType mediaType) throws IOException {
        HttpGet httpGet = new HttpGet(serviceUrl);
        httpGet.setHeader(HttpHeaders.ACCEPT, mediaType.toString());
        CloseableHttpClient httpClient = getHttpClient();
        HttpHost target = new HttpHost(serviceHost, servicePort, serviceScheme);
        HttpClientContext context = getHttpClientContext(target);
        return httpClient.execute(target, httpGet, context);
    }

    public String post(String serviceUrl, Object object, MediaType mediaType) throws IOException {
        serviceUrl = generateUrl(serviceUrl);
        String string = getString(object, mediaType);
        CloseableHttpResponse response = post(serviceUrl, new StringEntity(string), mediaType);
        return getResponseString(response);
    }

    public CloseableHttpResponse post(String serviceUrl, StringEntity entity, MediaType mediaType) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        HttpHost target = new HttpHost(serviceHost, servicePort, serviceScheme);
        HttpClientContext context = getHttpClientContext(target);
        HttpPost httpPost = new HttpPost(serviceUrl);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, mediaType.toString());
        httpPost.setHeader(HttpHeaders.ACCEPT, mediaType.toString());
        httpPost.setHeader(HttpHeaders.ACCEPT_CHARSET, ACCEPT_CHARSET_UTF8);
        httpPost.setEntity(entity);
        return httpClient.execute(target, httpPost, context);
    }

    public String put(String serviceUrl, Object object, MediaType mediaType) throws IOException {
        serviceUrl = generateUrl(serviceUrl);
        String string = getString(object, mediaType);
        CloseableHttpResponse response = put(serviceUrl, new StringEntity(string), mediaType);
        return getResponseString(response);
    }

    public CloseableHttpResponse put(String serviceUrl, StringEntity entity, MediaType mediaType) throws IOException {
        CloseableHttpClient httpClient = getHttpClient();
        HttpHost target = new HttpHost(serviceHost, servicePort, serviceScheme);
        HttpClientContext context = getHttpClientContext(target);
        HttpPut httpPut = new HttpPut(serviceUrl);
        httpPut.setHeader(HttpHeaders.CONTENT_TYPE, mediaType.toString());
        httpPut.setHeader(HttpHeaders.ACCEPT, mediaType.toString());
        httpPut.setHeader(HttpHeaders.ACCEPT_CHARSET, ACCEPT_CHARSET_UTF8);
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
        return HttpClients.custom()
                          .setDefaultRequestConfig(defaultRequestConfig)
                          .build();
    }

    private HttpClientContext getHttpClientContext(HttpHost target) {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(target, basicAuth);
        HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(authCache);
        return context;
    }

    public <T> T getObject(String response, Class<T> className) {
        try {
            return getJsonObject(response, className);
        } catch (Exception ex) {
            try {
                return getXMLObject(response, className);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public <T> T getJsonObject(String response, Class<T> className) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, className);
    }

    public <T> T getXMLObject(String response, Class<T> className) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(className);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(response);
        return (T) unmarshaller.unmarshal(reader);
    }

    public <T> List<T> getObjectList(String response, Class<T> className) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, mapper.getTypeFactory().constructCollectionType(List.class, className));
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

    private String getString(Object object, MediaType mediaType) {

        try {
            if(MediaType.APPLICATION_JSON == mediaType) {
                return getJsonString(object);
            }

            if (MediaType.APPLICATION_XML == mediaType) {
                return getXmlString(object);
            }
        }catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    private String getJsonString(Object object) throws JsonProcessingException {
        if (object != null) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        }
        return null;
    }

    private String getXmlString(Object object) throws JAXBException {
        StringWriter writer = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(object, writer);
        return writer.toString();
    }
}
