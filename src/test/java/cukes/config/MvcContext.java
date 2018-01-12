package cukes.config;

import cucumber.api.Scenario;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@Component
public class MvcContext {

    private String currentScenario;
    private List<ResultActions> httpResultList = new ArrayList<>();

    public void addHTTPResult(Scenario scenario, ResultActions resultActions) {

        if(currentScenario == null || !currentScenario.equals(scenario.getId())) {
            this.currentScenario = scenario.getId();
            httpResultList.clear();
        }

        httpResultList.add(resultActions);
    }

    public ResultActions getLastHTTPResult(Scenario scenario, final String serviceUrl) {

        ResultActions matchedResultActions = null;

        if(currentScenario != null && currentScenario.equals(scenario.getId()) && StringUtils.isNotBlank(serviceUrl)) {

            ListIterator<ResultActions> iterator = httpResultList.listIterator(httpResultList.size());

            while (iterator.hasPrevious()) {
                ResultActions resultActions = iterator.previous();
                MockHttpServletRequest request = resultActions.andReturn().getRequest();

                if(serviceUrl.equals(request.getRequestURI())) {
                    matchedResultActions = resultActions;
                    break;
                }
            }
        }

        return matchedResultActions;
    }

    public ResultActions getLastHTTPResult(Scenario scenario) {

        if(currentScenario != null && currentScenario.equals(scenario.getId())) {
            return httpResultList.get(httpResultList.size() - 1);
        }

        return null;
    }
}
