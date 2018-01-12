package cukes.steps;

import com.emprovise.response.ResponseMessage;
import com.emprovise.response.ServiceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cukes.config.LogbackCapture;
import cukes.config.MvcContext;
import cukes.config.MvcFilter;
import cukes.stub.SessionStubContext;
import org.json.JSONException;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@ActiveProfiles("stub")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cucumber.xml"})
@WebAppConfiguration
@WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
public class BaseStepDefinition {

    protected static final Logger LOGGER = Logger.getLogger(RunCukesTest.class.getName());
    @Autowired
    protected SessionStubContext sessionUtility;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MvcFilter mvcFilter;
    @Autowired
    private MvcContext mvcContext;
    @Autowired
    private LogbackCapture logbackCapture;

    private MockMvc mockMvc;
    private MockHttpSession mockSession;

    @PostConstruct
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                                      .addFilters(mvcFilter).build();
        mockSession = new MockHttpSession(webApplicationContext.getServletContext(), UUID.randomUUID().toString());

    }

    protected String getErrorMessage(ServiceResponse bean) {
        StringBuilder builder = new StringBuilder();

        if (bean != null && bean.getMessages() != null) {
            for (ResponseMessage message : bean.getMessages()) {
                builder.append(message.getMessage());
            }
        }
        return builder.toString();
    }

    public ResultActions get(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .session(mockSession));
    }

    public ResultActions post(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .session(mockSession));
    }

    public ResultActions post(String url, String request) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .session(mockSession)
                .content(request));
    }

    public ResultActions put(String url, String request) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.put(url)
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .session(mockSession)
                                       .content(request));
    }

    public ResultActions post(String url, Object object) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .session(mockSession)
                                                        .content(getJSONString(object)));
    }

    public String getJSONString(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    public String getFileString(File file) throws URISyntaxException, IOException {

        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        return new String(encoded, Charset.defaultCharset());
    }

    public <T> T getObject(Class<T> clazz, String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(jsonString, clazz);
    }

    public Map<String, String> getMap(DataTable dataTable) {
        Map<String, String> finalMap = new HashMap<>();
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> map : maps) {
            finalMap.putAll(map);
        }
        return finalMap;
    }

    public void assertJSONStrings(String expectedJSON, String actualJSON) throws JSONException {
        JSONCompareResult result = JSONCompare.compareJSON(expectedJSON, actualJSON, JSONCompareMode.LENIENT);
        if(result.failed()) {
            System.out.println("act json: " + actualJSON);
            System.out.println("expected json: " + expectedJSON);
            throw new AssertionError(result.getMessage());
        }
    }

    public MockMvc getMockMvc() {
        return this.mockMvc;
    }

    public MockHttpSession getMockHttpSession() {
        return this.mockSession;
    }

    public void setHTTPResult(Scenario scenario, ResultActions resultActions) {
        mvcContext.addHTTPResult(scenario, resultActions);
    }

    public ResultActions getHTTPResult(Scenario scenario) {
        return mvcContext.getLastHTTPResult(scenario);
    }

    public ResultActions getHTTPResult(Scenario scenario, String serviceUrl) {
        return mvcContext.getLastHTTPResult(scenario, serviceUrl);
    }

    public <T> T getHTTPResultObject(Scenario scenario, Class<T> clazz) throws Exception {
        return getHTTPResultObject(mvcContext.getLastHTTPResult(scenario), clazz);
    }

    public <T> T getHTTPResultObject(ResultActions resultActions, Class<T> clazz) throws Exception {
        if(resultActions != null) {
            MvcResult result = resultActions.andReturn();
            return getObject(clazz, result.getResponse().getContentAsString());
        }
        return null;
    }

    public String getRequestLog() {
        return logbackCapture.getCapturedLog();
    }
}
