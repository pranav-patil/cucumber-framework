package cukes.steps;

import com.library.response.MessageSeverity;
import com.library.response.ServiceResponse;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cukes.type.ContentType;
import io.cucumber.datatable.DataTable;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HttpSteps extends HttpStepDefinition {

    private Scenario scenario;
    private static final String HTTP_MOCK_REQUEST_PATH = "/cukes/http-service-request/";
    private static final String HTTP_MOCK_RESPONSE_PATH = "/cukes/http-service-response/";

    @Before
    public void before(Scenario scenario) {
        this.scenario = scenario;
    }

    @Given("^HTTP Session has an attribute \"(.*?)\" with the value \"(.*?)\"$")
    public void addStringAttributeToHttpSession(String attributeName, String attributeValue) {
        if(StringUtils.isNotBlank(attributeName)) {
            getMockHttpSession().setAttribute(attributeName, attributeValue);
        }
    }

    @Given("^HTTP Session has an attribute \"(.*?)\" with class \"(.*?)\" and (JSON|XML|FORM) value$")
    public void addObjectAttributeToHttpSession(String attributeName, Class<?> clazz, ContentType contentType, String contentValue) {
        if(StringUtils.isNotBlank(attributeName) && clazz != null && StringUtils.isNotBlank(contentValue)) {
            Object attributeValue = contentTypeService.getContentTypeObject(contentType, clazz, contentValue);
            getMockHttpSession().setAttribute(attributeName, attributeValue);
        }
    }

    @Given("^HTTP Session does not contain the attribute \"(.*?)\"$")
    public void removeAttributeFromHttpSession(String attributeName) {
        if(StringUtils.isNotBlank(attributeName)) {
            getMockHttpSession().removeAttribute(attributeName);
        }
    }

    @Before("@ClearHttpSession")
    public void clearHttpSession() {
        getMockHttpSession().clearAttributes();
    }

    @When("^HTTP GET Service is called with URL \"(.*?)\" returning (JSON|XML|FORM) response$")
    public void callGETService(String serviceUrl, ContentType contentType) throws Exception {
        ResultActions resultActions = get(serviceUrl, contentType);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP POST Service URL \"(.*?)\" is called returning (JSON|XML|FORM) response$")
    public void callPOSTServiceWithNoRequestPayload(String serviceUrl, ContentType contentType) throws Exception {
        ResultActions resultActions = post(serviceUrl, contentType);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP PUT Service URL \"(.*?)\" is called returning (JSON|XML|FORM) response$")
    public void callPUTService(String serviceUrl, ContentType contentType) throws Exception {
        ResultActions resultActions = put(serviceUrl, contentType);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP DELETE Service URL \"(.*?)\" is called returning (JSON|XML|FORM) response$")
    public void callDELETEService(String serviceUrl, ContentType contentType) throws Exception {
        ResultActions resultActions = delete(serviceUrl, contentType);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP POST Service is called with URL \"(.*?)\" and (JSON|XML|FORM) request filename \"(.*?)\"$")
    public void callPOSTServiceWithFilePayload(String serviceUrl, ContentType contentType, String filename) throws Exception {
        String requestPayload = getFileContent(contentType,HTTP_MOCK_REQUEST_PATH + filename);
        ResultActions resultActions = post(serviceUrl, contentType, requestPayload);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP POST Service is called with URL \"(.*?)\" and (JSON|XML|FORM) request$")
    public void callPOSTServiceWithRequestPayload(String serviceUrl, ContentType contentType, String payload) throws Exception {
        ResultActions resultActions = post(serviceUrl, contentType, payload);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP POST Service is called with URL \"(.*?)\" and (JSON|XML|FORM) request class \"(.*?)\" having request$")
    public void callPOSTServiceWithRequestPayloadForClass(String serviceUrl, ContentType contentType, Class<?> clazz, String payload) throws Exception {
        Object object = contentTypeService.getContentTypeObject(contentType, clazz, payload);
        ResultActions resultActions = post(serviceUrl, contentType, object);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP POST Service is called with URL \"(.*?)\" and (JSON|XML|FORM) request is response from previous service URL \"(.*?)\"$")
    public void callPOSTServiceWithPreviousResponsePayload(String serviceUrl, ContentType contentType, String previousServiceUrl) throws Exception {
        ResultActions previousHttpResult = getHTTPResult(scenario, previousServiceUrl);
        String previousResponse = previousHttpResult.andReturn().getResponse().getContentAsString();
        ResultActions resultActions = post(serviceUrl, contentType, previousResponse);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP PUT Service is called with URL \"(.*?)\" and (JSON|XML|FORM) request$")
    public void callPUTServiceWithRequestPayload(String serviceUrl, ContentType contentType, String payload) throws Exception {
        ResultActions resultActions = put(serviceUrl, contentType, payload);
        setHTTPResult(scenario, resultActions);
    }

    @Then("^Verify HTTP Status equals \"(.*?)\"$")
    public void verifyHTTPStatusOnly(HttpStatus httpStatus) throws Exception {
        ResultActions resultActions = getHTTPResult(scenario);
        resultActions.andExpect(status().is(httpStatus.value()));
    }

    @Then("^Verify HTTP Status is \"(.*?)\" and (JSON|XML|FORM) response matches with conditions$")
    public void verifyResponseWithConditions$(HttpStatus httpStatus, ContentType contentType, DataTable dataTable) throws Exception {
        ResultActions resultActions = getHTTPResult(scenario);
        MvcResult result = resultActions.andExpect(status().is(httpStatus.value()))
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        contentTypeService.matchContentByConditions(contentType, responseString, getMap(dataTable), true);
    }

    @Then("^Verify HTTP Status is \"(.*?)\" and response matches with (JSON|XML|FORM) filename \"(.*?)\"$")
    public void verifyResponseWithFile(HttpStatus httpStatus, ContentType contentType, String filename) throws Exception {
        String expectedResponse = getFileContent(contentType,HTTP_MOCK_RESPONSE_PATH + filename);
        verifyResponse(httpStatus, contentType, expectedResponse);
    }

    @Then("^Verify HTTP Status is \"(.*?)\" and response matches with (JSON|XML|FORM)$")
    public void verifyResponse(HttpStatus httpStatus, ContentType contentType, String expectedResponse) throws Exception {
        ResultActions resultActions = getHTTPResult(scenario);
        MvcResult result = resultActions.andExpect(status().is(httpStatus.value())).andReturn();
        String actualResponse = result.getResponse().getContentAsString();
        contentTypeService.assertContentByType(contentType, expectedResponse, actualResponse);
    }

    @Then("^Verify HTTP Status is \"(.*?)\" and (JSON|XML|FORM) response contains message severity \"(.*?)\" and message \"(.*?)\"$")
    public void verifyResponse(HttpStatus httpStatus, ContentType contentType, MessageSeverity severity, String message) throws Exception {
        ResultActions resultActions = getHTTPResult(scenario);
        MvcResult result = resultActions.andExpect(status().is(httpStatus.value())).andReturn();
        String responseString = result.getResponse().getContentAsString();
        ServiceResponse serviceResponse = contentTypeService.getContentTypeObject(contentType, ServiceResponse.class, responseString);

        if( message.equalsIgnoreCase("") && (severity == null) ) {
            assertTrue("Not empty: " + serviceResponse.getMessages().toString(),serviceResponse.getMessages().isEmpty());
        } else {
            assertTrue("No match: " + serviceResponse.getMessages().get(0).getSeverity().value() + " -- "
                               + serviceResponse.getMessages().get(0).getMessage() ,
                    serviceResponse.messagesHasSeverityAndMessage(severity, message));
        }
    }

    @Then("^Verify that the application log contains \"(.*?)\"$")
    public void verifyLogContains(String expectedLog) {
        String requestLog = getRequestLog();
        if (expectedLog.isEmpty()) {
            assertThat(requestLog, isEmptyOrNullString());
        }
        assertThat(requestLog, CoreMatchers.containsString(expectedLog));
    }

    @Then("^Verify that the application log is empty$")
    public void verifyLogIsEmpty() {
        String requestLog = getRequestLog();
        assertThat(requestLog, isEmptyOrNullString());
    }
}
