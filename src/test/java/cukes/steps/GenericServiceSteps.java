package cukes.steps;

import cucumber.api.DataTable;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cukes.stub.WireMockService;
import cukes.type.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.List;
import java.util.Map;

public class GenericServiceSteps extends BaseStepDefinition {

    @Autowired
    private WireMockService wireMockService;

    @Given("^JSON response for \"(.*?)\" GET service \"(.*?)\"$")
    public void serviceGetJSONResponse(String serviceName, String serviceUrl, DataTable dataTable) {
        List<Map<String, String>> mapList = dataTable.asMaps(String.class, String.class);
        String responseString = contentTypeService.getJSONArrayString(mapList);
        wireMockService.setResponseData(HttpMethod.GET, serviceUrl, ContentType.JSON, HttpStatus.OK, responseString);
    }

    @Given("^XML response with array element \"(.*?)\" and root element \"(.*?)\" for \"(.*?)\" GET service \"(.*?)\"$")
    public void serviceGetXMLResponse(String arrayElement, String rootElement, String serviceName,
                                      String serviceUrl, DataTable dataTable) {
        List<Map<String, String>> mapList = dataTable.asMaps(String.class, String.class);
        String responseString = contentTypeService.getXMLArrayString(mapList, rootElement, arrayElement);
        wireMockService.setResponseData(HttpMethod.GET, serviceUrl, ContentType.JSON, HttpStatus.OK, responseString);
    }

    @Given("^filename \"(.*?)\" is (JSON|XML|FORM) response for \"(.*?)\" GET service \"(.*?)\"$")
    public void serviceFileResponse(String filename, ContentType contentType, String serviceName, String serviceUrl) throws Exception {
        String fullFilePath = "/cukes/service-stub-response/" + filename + contentType.extension();
        String responseString = getFileContent(contentType, fullFilePath);
        wireMockService.setResponseData(HttpMethod.GET, serviceUrl, contentType, HttpStatus.OK, responseString);
    }

    @Given("^\"(.*?)\" (GET|PUT|POST) \"(.*?)\" service with (JSON|XML|FORM) request returns success with filename \"(.*?)\"$")
    public void serviceSuccessWithFileResponseForService(String serviceName, HttpMethod method, String serviceUrl,
                                                         ContentType contentType, String filename) throws Exception {
        String fullFilePath = "/cukes/service-stub-response/" + filename + contentType.extension();
        File file = getFile(contentType, fullFilePath);
        wireMockService.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, file);
    }

    @Given("^\"(.*?)\" (GET|PUT|POST) \"(.*?)\" service returns success with filename \"(.*?)\" when (JSON|XML|FORM) request matches the values$")
    public void serviceSuccessWithFileResponseForServiceWithRequestCondition(String serviceName, HttpMethod method, String serviceUrl,
                                                                             String filename, ContentType contentType,
                                                                             DataTable conditionTable) throws Exception {
        String fullFilePath = "/cukes/service-stub-response/" + filename + contentType.extension();
        File file = getFile(contentType, fullFilePath);
        wireMockService.setResponseData(method, serviceUrl, contentType, HttpStatus.OK, file, getMap(conditionTable));
    }

    @Given("^\"(.*?)\" (GET|PUT|POST) \"(.*?)\" service with (JSON|XML|FORM) request returns error with http status \"(.*?)\" and filename \"(.*?)\"$")
    public void serviceErrorWithFileResponseForService(String serviceName, HttpMethod method, String serviceUrl, ContentType contentType,
                                                       HttpStatus httpStatus, String filename) throws Exception {
        String fullFilePath = "/cukes/service-stub-response/" + filename + contentType.extension();
        File file = getFile(contentType, fullFilePath);
        wireMockService.setResponseData(method, serviceUrl, contentType, httpStatus, file);
    }

    @Given("^\"(.*?)\" (PUT|POST) \"(.*?)\" service returns error with http status \"(.*?)\" and filename \"(.*?)\" when (JSON|XML|FORM) request matches the values$")
    public void serviceErrorWithFileResponseForServiceWithRequestCondition(String serviceName, HttpMethod method, String serviceUrl,
                                                                           HttpStatus httpStatus, String filename,
                                                                           ContentType contentType, DataTable conditionTable) throws Exception {
        String fullFilePath = "/cukes/service-stub-response/" + filename + contentType.extension();
        File file = getFile(contentType, fullFilePath);
        wireMockService.setResponseData(method, serviceUrl, contentType, httpStatus, file, getMap(conditionTable));
    }

    @Before("@ClearServiceStub")
    public void clearServiceStub() {
        wireMockService.clearStubs();
    }

}
