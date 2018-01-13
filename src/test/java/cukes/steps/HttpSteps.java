package cukes.steps;

import com.library.response.MessageSeverity;
import com.library.response.ServiceResponse;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.junit.matchers.JUnitMatchers;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HttpSteps extends BaseStepDefinition {

    public static final String HTTP_MOCK_REQUEST_PATH = "/cukes/http-service-request/";
    public static final String HTTP_MOCK_RESPONSE_PATH = "/cukes/http-service-response/";
    private Scenario scenario;

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

    @Given("^HTTP Session has an attribute \"(.*?)\" with class \"(.*?)\" and JSON value$")
    public void addObjectAttributeToHttpSession(String attributeName, Class<?> clazz, String jsonValue) throws IOException {
        if(StringUtils.isNotBlank(attributeName) && clazz != null && StringUtils.isNotBlank(jsonValue)) {
            Object attributeValue = getObject(clazz, jsonValue);
            getMockHttpSession().setAttribute(attributeName, attributeValue);
        }
    }

    @Given("^HTTP Session does not contain the attribute \"(.*?)\"$")
    public void removeAttributeFromHttpSession(String attributeName, Class<?> clazz, String jsonValue) throws IOException {
        if(StringUtils.isNotBlank(attributeName)) {
            getMockHttpSession().removeAttribute(attributeName);
        }
    }

    @Before("@ClearHttpSession")
    public void clearHttpSession() {
        getMockHttpSession().clearAttributes();
    }

    @When("^HTTP GET Service is called with URL \"(.*?)\"$")
    public void callGETService(String serviceUrl) throws Exception {
        ResultActions resultActions = get(serviceUrl);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP POST Service URL \"(.*?)\" is called$")
    public void callPOSTServiceWithNoRequestPayload(String serviceUrl) throws Exception {
        ResultActions resultActions = post(serviceUrl);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP POST Service is called with URL \"(.*?)\" and JSON request filename \"(.*?)\"$")
    public void callPOSTServiceWithFilePayload(String serviceUrl, String filename) throws Exception {
        String requestPayload = getJSONFileString(HTTP_MOCK_REQUEST_PATH + filename);
        ResultActions resultActions = post(serviceUrl, requestPayload);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP POST Service is called with URL \"(.*?)\" and JSON request$")
    public void callPOSTServiceWithRequestPayload(String serviceUrl, String jsonString) throws Exception {
        ResultActions resultActions = post(serviceUrl, jsonString);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP POST Service is called with URL \"(.*?)\" and JSON request class \"(.*?)\" having JSON request$")
    public void callPOSTServiceWithRequestPayloadForClass(String serviceUrl, Class<?> clazz, String jsonString) throws Exception {
        Object object = getObject(clazz, jsonString);
        ResultActions resultActions = post(serviceUrl, object);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP POST Service is called with URL \"(.*?)\" and JSON request is JSON response from previous service URL \"(.*?)\"$")
    public void callPOSTServiceWithPreviousResponsePayload(String serviceUrl, String previousServiceUrl) throws Exception {
        ResultActions previousHttpResult = getHTTPResult(scenario, previousServiceUrl);
        String previousJsonResponse = previousHttpResult.andReturn().getResponse().getContentAsString();
        ResultActions resultActions = post(serviceUrl, previousJsonResponse);
        setHTTPResult(scenario, resultActions);
    }

    @When("^HTTP PUT Service is called with URL \"(.*?)\" and JSON request$")
    public void callPUTServiceWithRequestPayload(String serviceUrl, String jsonString) throws Exception {
        ResultActions resultActions = put(serviceUrl, jsonString);
        setHTTPResult(scenario, resultActions);
    }

    @Then("^Verify HTTP Status equals \"(.*?)\"$")
    public void thenVerifyHTTPStatusOnly(HttpStatus httpStatus) throws Exception {
        ResultActions resultActions = getHTTPResult(scenario);
        resultActions.andExpect(status().is(httpStatus.value()));
    }

    @Then("^Verify HTTP Status is \"(.*?)\" and response matches with conditions$")
    public void thenVerifyResponseWithConditions$(HttpStatus httpStatus, DataTable dataTable) throws Exception {
        ResultActions resultActions = getHTTPResult(scenario);
        MvcResult result = resultActions.andExpect(status().is(httpStatus.value()))
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        assertRequestConditions(jsonResponse, getMap(dataTable));
    }

    @Then("^Verify HTTP Status is \"(.*?)\" and response matches with JSON filename \"(.*?)\"$")
    public void thenVerifyResponseWithJSONFile(HttpStatus httpStatus, String filename) throws Exception {
        String expectedResponse = getJSONFileString(HTTP_MOCK_RESPONSE_PATH + filename);
        thenVerifyResponseWithJSON(httpStatus, expectedResponse);
    }

    @Then("^Verify HTTP Status is \"(.*?)\" and response matches with JSON$")
    public void thenVerifyResponseWithJSON(HttpStatus httpStatus, String expectedJSON) throws Exception {
        ResultActions resultActions = getHTTPResult(scenario);
        MvcResult result = resultActions.andExpect(status().is(httpStatus.value())).andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        assertJSONStrings(expectedJSON, jsonResponse);
    }

    @Then("^Verify HTTP Status is \"(.*?)\" and service response contains message severity \"(.*?)\" and message \"(.*?)\"$")
    public void thenVerifyResponseWithJSON(HttpStatus httpStatus, MessageSeverity severity, String message) throws Exception {
        ResultActions resultActions = getHTTPResult(scenario);
        MvcResult result = resultActions.andExpect(status().is(httpStatus.value())).andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        ServiceResponse serviceResponse = getObject(ServiceResponse.class, jsonResponse);

        if( message.equalsIgnoreCase("") && (severity == null) ) {
            assertTrue("Not empty: " + serviceResponse.getMessages().toString(),serviceResponse.getMessages().isEmpty());
        } else {
            assertTrue("No match: " + serviceResponse.getMessages().get(0).getSeverity().value() + " -- "
                               + serviceResponse.getMessages().get(0).getMessage() ,
                    serviceResponse.messagesHasSeverityAndMessage(severity, message));
        }

    }

    @Then("^Verify that the application log contains \"(.*?)\"$")
    public void thenVerifyLogContains(String expectedLog) throws Exception {
        String requestLog = getRequestLog();
        if (expectedLog.isEmpty()) {
            assertThat(requestLog, isEmptyOrNullString());
        }
        assertThat(requestLog, JUnitMatchers.containsString(expectedLog));
    }

    private void assertRequestConditions(String jsonResponse, Map<String, String> requestConditions) {

        if(jsonResponse == null || requestConditions == null) {
            return;
        }

        DocumentContext documentContext = JsonPath.parse(jsonResponse);

        for (Map.Entry<String, String> entry : requestConditions.entrySet()) {

            String pathKey = entry.getKey();

            if(StringUtils.isBlank(pathKey)) {
                continue;
            }

            pathKey = pathKey.trim();

            if(!pathKey.startsWith("$.")) {
                pathKey = "$." + pathKey;
            }

            String actualValue = String.valueOf(documentContext.read(pathKey));
            assertEquals(entry.getValue(), actualValue);
        }
    }

    private String getJSONFileString(String filename) throws URISyntaxException, IOException {
        if(filename != null && !filename.endsWith(".json")) {
            filename = filename + ".json";
        }

        URL resource = this.getClass().getResource(filename);
        return getFileString(new File(resource.toURI()));
    }
}
