package cukes.stub;


import cukes.dto.ComplexHashMap;
import cukes.dto.ServiceIdentifier;
import cukes.dto.ServiceResponse;
import cukes.helper.ContentTypeService;
import cukes.type.ContentType;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicStatusLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Component
public class ServiceStubHttpClient {

    @Autowired
    private ContentTypeService contentTypeService;
    private ComplexHashMap<ServiceIdentifier, ServiceResponse> responseData = new ComplexHashMap<>();

    public static final String SERVICE_MOCK_RESPONSE_PATH = "/cukes/service-stub-response/";

    protected CloseableHttpResponse getStubResponse(HttpMethod httpMethod, String url, ContentType contentType, StringEntity stringEntity) throws IOException {

        String jsonRequestPayload = null;
        if(stringEntity != null) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(stringEntity.getContent(), writer, Charset.defaultCharset());
            jsonRequestPayload = writer.toString();
        }

        ServiceResponse serviceResponseData = getResponseData(httpMethod, url, contentType, jsonRequestPayload);
        String serviceResponseString = null;

        if(serviceResponseData != null) {

            if(serviceResponseData.getResponseString() != null) {
                serviceResponseString = serviceResponseData.getResponseString();
            }
            else if(serviceResponseData.getResponseFile() != null) {
                String path = serviceResponseData.getResponseFile().getPath();
                byte[] encoded = Files.readAllBytes(Paths.get(path));
                serviceResponseString = new String(encoded, Charset.defaultCharset());
            }
        }


        if(serviceResponseString == null) {
            throw new RuntimeException("No Response found in Stub for url: " + url);
        }

        return createMockCloseableHttpResponse(serviceResponseString);
    }

    private CloseableHttpResponse createMockCloseableHttpResponse(String responseString) throws IOException {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);

        byte[] bytes = responseString.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(bytes);

        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.OK.value(), "OK"));
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(inputStream);
        when(entity.getContentLength()).thenReturn((long)bytes.length);

        return response;
    }

    public ServiceResponse getResponseData(HttpMethod httpMethod, String url, ContentType contentType, String requestString) {
        ServiceIdentifier identifier = new ServiceIdentifier(httpMethod, url, contentType);
        List<ServiceResponse> allResponsesForService = responseData.getAll(identifier);

        if(allResponsesForService != null) {
            for (ServiceResponse serviceResponse : allResponsesForService) {
                Map<String, String> requestConditions = serviceResponse.getRequestConditions();
                boolean matchedConditions = contentTypeService.matchContentByConditions(contentType, requestString, requestConditions, false);

                if(matchedConditions) {
                    return serviceResponse;
                }
            }
        }

        return null;
    }

    public List<ServiceResponse> getResponseData(HttpMethod httpMethod, String url, ContentType contentType) {
        ServiceIdentifier identifier = new ServiceIdentifier(httpMethod, url, contentType);
        return responseData.getAll(identifier);
    }

    public void setResponseData(HttpMethod httpMethod, String url, ContentType contentType, HttpStatus httpStatus, String responseString) {
        ServiceIdentifier identifier = new ServiceIdentifier(httpMethod, url, contentType);
        ServiceResponse response = new ServiceResponse(httpStatus, responseString);
        this.responseData.put(identifier, response);
    }

    public void setResponseData(HttpMethod httpMethod, String url, ContentType contentType, HttpStatus httpStatus, String responseString, Map<String, String> requestConditions) {
        ServiceIdentifier identifier = new ServiceIdentifier(httpMethod, url, contentType);
        ServiceResponse response = new ServiceResponse(httpStatus, responseString, requestConditions);
        this.responseData.put(identifier, response);
    }

    public void setResponseData(HttpMethod httpMethod, String url, ContentType contentType, HttpStatus httpStatus, File responseFile) {
        ServiceIdentifier identifier = new ServiceIdentifier(httpMethod, url, contentType);
        ServiceResponse response = new ServiceResponse(httpStatus, responseFile);
        this.responseData.put(identifier, response);
    }

    public void setResponseData(HttpMethod httpMethod, String url, ContentType contentType, HttpStatus httpStatus, File responseFile, Map<String, String> requestConditions) {
        ServiceIdentifier identifier = new ServiceIdentifier(httpMethod, url, contentType);
        ServiceResponse response = new ServiceResponse(httpStatus, responseFile, requestConditions);
        this.responseData.put(identifier, response);
    }

    public void clearStubResponses() {
        responseData.clear();
    }
}
