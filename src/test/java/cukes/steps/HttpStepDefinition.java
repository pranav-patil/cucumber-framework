package cukes.steps;

import com.library.response.ResponseMessage;
import com.library.response.ServiceResponse;
import cucumber.api.Scenario;
import cukes.config.LogbackCapture;
import cukes.config.MvcContext;
import cukes.config.MvcFilter;
import cukes.type.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@ActiveProfiles("stub")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cucumber.xml"})
@WebAppConfiguration
@WithMockUser(username = "admin", authorities = { "ADMIN", "USER" })
public class HttpStepDefinition extends BaseStepDefinition {

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
    protected static final Logger logger = Logger.getLogger(HttpStepDefinition.class.getName());

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

    public ResultActions get(String url, ContentType contentType) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(contentType.mediaType())
                .accept(contentType.mediaType())
                .session(mockSession));
    }

    public ResultActions post(String url, ContentType contentType) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                                        .contentType(contentType.mediaType())
                                        .accept(contentType.mediaType())
                                        .session(mockSession));
    }

    public ResultActions post(String url, ContentType contentType, Object object) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                                                        .contentType(contentType.mediaType())
                                                        .accept(contentType.mediaType())
                                                        .session(mockSession)
                                                        .content(contentTypeService.getContentTypeString(contentType, object)));
    }

    public ResultActions post(String url, ContentType contentType, String request) throws Exception {

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(url)
                                                        .contentType(contentType.mediaType())
                                                        .accept(contentType.mediaType())
                                                        .session(mockSession);

        if(contentType == ContentType.FORM && !StringUtils.isBlank(request)) {
            Map<String, String> parameters = contentTypeService.getParameters(request);

            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                requestBuilder = requestBuilder.param(entry.getKey(), entry.getValue());
            }
        } else {
            requestBuilder = requestBuilder.content(request);
        }

        return mockMvc.perform(requestBuilder);
    }

    public ResultActions put(String url, ContentType contentType) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.put(url)
                                .contentType(contentType.mediaType())
                                .accept(contentType.mediaType())
                                .session(mockSession));
    }

    public ResultActions put(String url, ContentType contentType, String request) throws Exception {

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put(url)
                .contentType(contentType.mediaType())
                .accept(contentType.mediaType())
                .session(mockSession);

        if(contentType == ContentType.FORM && !StringUtils.isBlank(request)) {
            Map<String, String> parameters = contentTypeService.getParameters(request);

            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                requestBuilder = requestBuilder.param(entry.getKey(), entry.getValue());
            }
        } else {
            requestBuilder = requestBuilder.content(request);
        }

        return mockMvc.perform(requestBuilder.content(request));
    }

    public ResultActions delete(String url, ContentType contentType) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.delete(url)
                                .contentType(contentType.mediaType())
                                .accept(contentType.mediaType())
                                .session(mockSession));
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
            ContentType contentType = ContentType.getContentType(result.getResponse().getContentType());
            return contentTypeService.getContentTypeObject(contentType, clazz, result.getResponse().getContentAsString());
        }
        return null;
    }

    public String getRequestLog() {
        return logbackCapture.getCapturedLog();
    }
}
