package cukes.steps;

import com.library.domain.ErpResponse;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cukes.stub.GenericStubService;
import cukes.type.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Random;

import static cukes.config.GenericServiceConfig.ERP_SERVICE;

public class ErpServiceSteps extends BaseStepDefinition {

    @Autowired
    private GenericStubService genericStubService;

    @Given("^ERP (GET|PUT|POST) \"(.*?)\" service with (JSON|XML|FORM) request returns success$")
    public void serviceSuccessForService(HttpMethod method, String serviceUrl, ContentType contentType) {
        serviceUrl = genericStubService.getFullURL(ERP_SERVICE, serviceUrl);
        ErpResponse erpResponse = createResponse("Success", "S", "Success");
        String responseString = contentTypeService.getContentTypeString(contentType, erpResponse);
        genericStubService.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, responseString);
    }

    @Given("^ERP (GET|PUT|POST) \"(.*?)\" service returns success when (JSON|XML|FORM) request matches the values$")
    public void serviceSuccessForServiceWithRequestCondition(HttpMethod method, String serviceUrl, ContentType contentType, DataTable conditionTable) {
        serviceUrl = genericStubService.getFullURL(ERP_SERVICE, serviceUrl);
        ErpResponse erpResponse = createResponse("Success","S", "Success");
        String responseString = contentTypeService.getContentTypeString(contentType, erpResponse);
        genericStubService.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, responseString, getMap(conditionTable));
    }

    @Given("^ERP (GET|PUT|POST) \"(.*?)\" service returns error with http status \"(.*?)\" and having (JSON|XML|FORM) response error code \"(.*?)\" and error message \"(.*?)\"$")
    public void serviceErrorForService(HttpMethod method, String serviceUrl, HttpStatus httpStatus, ContentType contentType,
                                       String errorCode, String errorDescription) {
        serviceUrl = genericStubService.getFullURL(ERP_SERVICE, serviceUrl);
        ErpResponse erpResponse = createResponse("Error", errorCode, errorDescription);
        String responseString = contentTypeService.getContentTypeString(contentType, erpResponse);
        genericStubService.setResponseData(method, serviceUrl, contentType, httpStatus, responseString);
    }

    @Given("^ERP (PUT|POST) \"(.*?)\" service returns error with http status \"(.*?)\" and having (JSON|XML|FORM) response error code \"(.*?)\" and error message \"(.*?)\" when request matches the values$")
    public void serviceErrorForServiceWithRequestCondition(HttpMethod method, String serviceUrl, HttpStatus httpStatus, ContentType contentType,
                                                           String errorCode, String errorDescription, DataTable conditionTable) {
        serviceUrl = genericStubService.getFullURL(ERP_SERVICE, serviceUrl);
        ErpResponse erpResponse = createResponse("Error", errorCode, errorDescription);
        String responseString = contentTypeService.getContentTypeString(contentType, erpResponse);
        genericStubService.setResponseData(method, serviceUrl, contentType, httpStatus, responseString, getMap(conditionTable));
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
