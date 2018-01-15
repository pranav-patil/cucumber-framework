package cukes.steps;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceSteps extends BaseStepDefinition {

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
        JsonObject response = createResponse("S", "Success");
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, response.toString());
    }

    @Given("^(GET|PUT|POST) \"(.*?)\" service with (JSON|XML|FORM) request returns success with filename \"(.*?)\"$")
    public void serviceSuccessWithFileResponseForService(HttpMethod method, String serviceUrl, ContentType contentType, String filename) throws Exception {
        serviceUrl = getFullURL(serviceUrl);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, getFile(filename));
    }

    @Given("^(GET|PUT|POST) \"(.*?)\" service returns success when (JSON|XML|FORM) request matches the values$")
    public void serviceSuccessForServiceWithRequestCondition(HttpMethod method, String serviceUrl, ContentType contentType, DataTable conditionTable) {
        serviceUrl = getFullURL(serviceUrl);
        JsonObject response = createResponse("S", "Success");
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, response.toString(), getMap(conditionTable));
    }

    @Given("^(GET|PUT|POST) \"(.*?)\" service returns success with filename \"(.*?)\" when (JSON|XML|FORM) request matches the values$")
    public void serviceSuccessWithFileResponseForServiceWithRequestCondition(HttpMethod method, String serviceUrl, String filename, ContentType contentType,
                                                                             DataTable conditionTable) throws Exception {
        serviceUrl = getFullURL(serviceUrl);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, getFile(filename), getMap(conditionTable));
    }

    @Given("^(GET|PUT|POST) \"(.*?)\" service with (JSON|XML|FORM) request returns error with http status \"(.*?)\" and filename \"(.*?)\"$")
    public void serviceErrorWithFileResponseForService(HttpMethod method, String serviceUrl, ContentType contentType, HttpStatus httpStatus,
                                                       String filename) throws Exception {
        serviceUrl = getFullURL(serviceUrl);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, httpStatus, getFile(filename));
    }

    @Given("^(PUT|POST) \"(.*?)\" service returns error with http status \"(.*?)\" and filename \"(.*?)\" when (JSON|XML|FORM) request matches the values$")
    public void serviceErrorWithFileResponseForServiceWithRequestCondition(HttpMethod method, String serviceUrl, HttpStatus httpStatus, String filename,
                                                                            ContentType contentType, DataTable conditionTable) throws Exception {
        serviceUrl = getFullURL(serviceUrl);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, httpStatus, getFile(filename), getMap(conditionTable));
    }

    @Given("^(GET|PUT|POST) \"(.*?)\" service returns error with http status \"(.*?)\" and having (JSON|XML|FORM) response error code \"(.*?)\" and error message \"(.*?)\"$")
    public void serviceErrorForService(HttpMethod method, String serviceUrl, HttpStatus httpStatus, ContentType contentType,
                                       String errorCode, String errorDescription) {
        serviceUrl = getFullURL(serviceUrl);
        JsonObject response = createErrorResponse(errorCode, errorDescription);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, httpStatus, response.toString());
    }

    @Given("^(PUT|POST) \"(.*?)\" service returns error with http status \"(.*?)\" and having (JSON|XML|FORM) response error code \"(.*?)\" and error message \"(.*?)\" when request matches the values$")
    public void serviceErrorForServiceWithRequestCondition(HttpMethod method, String serviceUrl, HttpStatus httpStatus, ContentType contentType,
                                                           String errorCode, String errorDescription, DataTable conditionTable) {
        serviceUrl = getFullURL(serviceUrl);
        JsonObject response = createErrorResponse(errorCode, errorDescription);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, httpStatus, response.toString(), getMap(conditionTable));
    }

    @Given("^(GET|PUT|POST) \"(.*?)\" service throws exception with http status \"(.*?)\" and having (JSON|XML|FORM) response error code \"(.*?)\" and error message \"(.*?)\"$")
    public void serviceExceptionForService(HttpMethod method, String serviceUrl, HttpStatus httpStatus, ContentType contentType,
                                           String errorCode, String errorDescription) {
        serviceUrl = getFullURL(serviceUrl);
        JsonObject response = createExceptionResponse(errorCode, errorDescription);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, httpStatus, response.toString());
    }

    @Given("^(PUT|POST) \"(.*?)\" service throws exception with http status \"(.*?)\" and having (JSON|XML|FORM) response error code \"(.*?)\" and error message \"(.*?)\" when request matches the values$")
    public void serviceExceptionForServiceWithRequestCondition(HttpMethod method, String serviceUrl, HttpStatus httpStatus, ContentType contentType,
                                                               String errorCode, String errorDescription, DataTable conditionTable) {
        serviceUrl = getFullURL(serviceUrl);
        JsonObject response = createExceptionResponse(errorCode, errorDescription);
        stubBaseHttpClient.setResponseData(method, serviceUrl, contentType, httpStatus, response.toString(), getMap(conditionTable));
    }

    @Before("@ClearServiceStub")
    public void clearServiceStub() {
        stubBaseHttpClient.clearStubResponses();
    }

    private File getFile(String filename) throws URISyntaxException {

        if(filename != null && !filename.endsWith(".json")) {
            filename = filename + ".json";
        }

        URL resource = this.getClass().getResource(ServiceStubHttpClient.SERVICE_MOCK_RESPONSE_PATH + filename);
        return new File(resource.toURI());
    }

    private String getFullURL(String url) {

        Matcher matcher = SERVICE_PATTERN.matcher(url);

        if (matcher.find()) {
            return String.format("%s://%s:%d%s", serviceScheme, serviceHost, servicePort, url);
        }

        throw new RuntimeException("Invalid Service URL " + url);
    }

    private JsonObject createResponse(String status, String message) {

        JsonObject response = new JsonObject();
        response.addProperty("ID", "0001");
        response.addProperty("Status", status);
        response.addProperty("Message", message);
        return response;
    }

    private JsonObject createErrorResponse(String errorCode, String errorMessage) {
        JsonObject response = createResponse("E", errorMessage);
        response.addProperty("Code", errorCode);
        return response;
    }

    private JsonObject createExceptionResponse(String errorCode, String errorMessage) {
        JsonObject errorElement = new JsonObject();

        errorElement.addProperty("code", errorCode);

        JsonObject messageElement = new JsonObject();
        messageElement.addProperty("lang", "en");
        messageElement.addProperty("value", errorMessage);
        errorElement.add("message", messageElement);

        JsonObject innererrorElement = new JsonObject();
        innererrorElement.addProperty("transactionid", String.valueOf((100000 + Math.random() * 900000)));
        innererrorElement.addProperty("timestamp", String.valueOf(System.currentTimeMillis()));

        JsonObject errorResolutionElement = new JsonObject();
        errorResolutionElement.addProperty("Transaction", "Run transaction TX545454 with the timestamp above for more details");
        errorResolutionElement.addProperty("Details", "See 24234423 for more details");
        innererrorElement.add("Error_Resolution", errorResolutionElement);

        JsonArray errordetailsArray = new JsonArray();

        JsonObject errordetails = new JsonObject();
        errordetails.addProperty("code", errorCode);
        errordetails.addProperty("message", errorMessage);
        errordetails.addProperty("propertyref", "");
        errordetails.addProperty("severity", "error");
        errordetails.addProperty("target", "");
        errordetailsArray.add(errordetails);
        innererrorElement.add("errordetails", errordetailsArray);

        errorElement.add("innererror", innererrorElement);

        JsonObject json = new JsonObject();
        json.add("error", errorElement);
        return json;
    }
}
