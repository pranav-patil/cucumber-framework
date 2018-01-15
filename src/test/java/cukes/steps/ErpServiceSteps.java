package cukes.steps;

import com.library.domain.ErpResponse;
import cucumber.api.DataTable;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cukes.stub.ServiceStubHttpClient;
import cukes.type.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErpServiceSteps extends BaseStepDefinition {

    @Autowired
    private ServiceStubHttpClient stubBaseHttpClient;
    @Value("${SERVICE_SCHEME}")
    private String serviceScheme;
    @Value("${SERVICE_HOST}")
    private String serviceHost;
    @Value("${SERVICE_PORT}")
    private int servicePort;

    private static final Pattern SERVICE_PATTERN = Pattern.compile("^/internal/erp/(.*)$");

    @Given("^(GET|PUT|POST) \"(.*?)\" service with (JSON|XML|FORM) request returns success$")
    public void serviceSuccessForService(HttpMethod method, String serviceUrl, ContentType contentType) {
        serviceUrl = getFullURL(serviceUrl);
        ErpResponse erpResponse = createResponse("Success", "S", "Success");
        String responseString = contentTypeService.getContentTypeString(contentType, erpResponse);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, responseString);
    }

    @Given("^(GET|PUT|POST) \"(.*?)\" service with (JSON|XML|FORM) request returns success with filename \"(.*?)\"$")
    public void serviceSuccessWithFileResponseForService(HttpMethod method, String serviceUrl, ContentType contentType, String filename) throws Exception {
        serviceUrl = getFullURL(serviceUrl);
        File file = getFile(contentType, ServiceStubHttpClient.SERVICE_MOCK_RESPONSE_PATH + filename);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, file);
    }

    @Given("^(GET|PUT|POST) \"(.*?)\" service returns success when (JSON|XML|FORM) request matches the values$")
    public void serviceSuccessForServiceWithRequestCondition(HttpMethod method, String serviceUrl, ContentType contentType, DataTable conditionTable) {
        serviceUrl = getFullURL(serviceUrl);
        ErpResponse erpResponse = createResponse("Success","S", "Success");
        String responseString = contentTypeService.getContentTypeString(contentType, erpResponse);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, responseString, getMap(conditionTable));
    }

    @Given("^(GET|PUT|POST) \"(.*?)\" service returns success with filename \"(.*?)\" when (JSON|XML|FORM) request matches the values$")
    public void serviceSuccessWithFileResponseForServiceWithRequestCondition(HttpMethod method, String serviceUrl, String filename, ContentType contentType,
                                                                             DataTable conditionTable) throws Exception {
        serviceUrl = getFullURL(serviceUrl);
        File file = getFile(contentType, ServiceStubHttpClient.SERVICE_MOCK_RESPONSE_PATH + filename);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, file, getMap(conditionTable));
    }

    @Given("^(GET|PUT|POST) \"(.*?)\" service with (JSON|XML|FORM) request returns error with http status \"(.*?)\" and filename \"(.*?)\"$")
    public void serviceErrorWithFileResponseForService(HttpMethod method, String serviceUrl, ContentType contentType, HttpStatus httpStatus,
                                                       String filename) throws Exception {
        serviceUrl = getFullURL(serviceUrl);
        File file = getFile(contentType, ServiceStubHttpClient.SERVICE_MOCK_RESPONSE_PATH + filename);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, httpStatus, file);
    }

    @Given("^(PUT|POST) \"(.*?)\" service returns error with http status \"(.*?)\" and filename \"(.*?)\" when (JSON|XML|FORM) request matches the values$")
    public void serviceErrorWithFileResponseForServiceWithRequestCondition(HttpMethod method, String serviceUrl, HttpStatus httpStatus, String filename,
                                                                            ContentType contentType, DataTable conditionTable) throws Exception {
        serviceUrl = getFullURL(serviceUrl);
        File file = getFile(contentType, ServiceStubHttpClient.SERVICE_MOCK_RESPONSE_PATH + filename);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, httpStatus, file, getMap(conditionTable));
    }

    @Given("^(GET|PUT|POST) \"(.*?)\" service returns error with http status \"(.*?)\" and having (JSON|XML|FORM) response error code \"(.*?)\" and error message \"(.*?)\"$")
    public void serviceErrorForService(HttpMethod method, String serviceUrl, HttpStatus httpStatus, ContentType contentType,
                                       String errorCode, String errorDescription) {
        serviceUrl = getFullURL(serviceUrl);
        ErpResponse erpResponse = createResponse("Error", errorCode, errorDescription);
        String responseString = contentTypeService.getContentTypeString(contentType, erpResponse);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, httpStatus, responseString);
    }

    @Given("^(PUT|POST) \"(.*?)\" service returns error with http status \"(.*?)\" and having (JSON|XML|FORM) response error code \"(.*?)\" and error message \"(.*?)\" when request matches the values$")
    public void serviceErrorForServiceWithRequestCondition(HttpMethod method, String serviceUrl, HttpStatus httpStatus, ContentType contentType,
                                                           String errorCode, String errorDescription, DataTable conditionTable) {
        serviceUrl = getFullURL(serviceUrl);
        ErpResponse erpResponse = createResponse("Error", errorCode, errorDescription);
        String responseString = contentTypeService.getContentTypeString(contentType, erpResponse);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, httpStatus, responseString, getMap(conditionTable));
    }

    @Before("@ClearServiceStub")
    public void clearServiceStub() {
        stubBaseHttpClient.clearStubResponses();
    }

    private String getFullURL(String url) {

        Matcher matcher = SERVICE_PATTERN.matcher(url);

        if (matcher.find()) {
            return String.format("%s://%s:%d%s", serviceScheme, serviceHost, servicePort, url);
        }

        throw new RuntimeException("Invalid Service URL " + url);
    }

    private ErpResponse createResponse(String status, String code, String message) {
        ErpResponse erpResponse = new ErpResponse();
        erpResponse.setId(String.valueOf(getRandomNumber()));
        erpResponse.setCode(code);
        erpResponse.setStatus(status);
        erpResponse.setMessage(message);
        return erpResponse;
    }

    private int getRandomNumber() {
        Random random = new Random();
        return random.nextInt((10000 - 1) + 1) + 1;
    }
}
