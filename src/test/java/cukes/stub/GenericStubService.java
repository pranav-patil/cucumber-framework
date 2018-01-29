package cukes.stub;

import cukes.dto.ComplexHashMap;
import cukes.dto.GenericServiceType;
import cukes.dto.ServiceIdentifier;
import cukes.dto.ServiceResponse;
import cukes.helper.ContentTypeService;
import cukes.type.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

@Component
public class GenericStubService {

    @Autowired
    private ContentTypeService contentTypeService;
    @Autowired
    private Map<String, GenericServiceType> genericServiceMap;

    private ComplexHashMap<ServiceIdentifier, ServiceResponse> responseData = new ComplexHashMap<>();

    public String getStubResponse(HttpMethod httpMethod, String url, ContentType contentType, String requestPayload) throws IOException {

        String stubResponse = null;
        ServiceResponse serviceResponseData = getResponseData(httpMethod, url, contentType, requestPayload);

        if(serviceResponseData != null) {

            if(serviceResponseData.getResponseString() != null) {
                stubResponse = serviceResponseData.getResponseString();
            }
            else if(serviceResponseData.getResponseFile() != null) {
                String path = serviceResponseData.getResponseFile().getPath();
                byte[] encoded = Files.readAllBytes(Paths.get(path));
                stubResponse = new String(encoded, Charset.defaultCharset());
            }
        }

        if(stubResponse == null) {
            throw new RuntimeException("No Stub Response found for service url: " + url);
        }

        return stubResponse;
    }

    private ServiceResponse getResponseData(HttpMethod httpMethod, String url, ContentType contentType, String requestString) {
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

    public String getFullURL(String serviceName, String serviceUrl) {
        GenericServiceType genericServiceType = genericServiceMap.get(serviceName);

        if(genericServiceType != null) {

            if(genericServiceType.getUrlPattern() != null) {
                Matcher matcher = genericServiceType.getUrlPattern().matcher(serviceUrl);

                if (!matcher.find()) {
                    throw new RuntimeException(String.format("Invalid %s Service URL %s", serviceName, serviceUrl));
                }
            }

            if(StringUtils.isNotBlank(genericServiceType.getScheme()) && StringUtils.isNotBlank(genericServiceType.getHost())) {
                return String.format("%s://%s:%d%s", genericServiceType.getScheme(), genericServiceType.getHost(),
                                                     genericServiceType.getPort(), serviceUrl);
            }
        }
        return serviceUrl;
    }

    public String getFullFilePath(String serviceName, String filename) {
        GenericServiceType genericServiceType = genericServiceMap.get(serviceName);

        if(genericServiceType != null && StringUtils.isNotBlank(genericServiceType.getResponseFilePath())) {
            return genericServiceType.getResponseFilePath() + filename;
        }
        return filename;
    }
}
