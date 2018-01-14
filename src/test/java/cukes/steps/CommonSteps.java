package cukes.steps;

import com.library.service.DateServiceImpl;
import cucumber.api.Format;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cukes.stub.DateStubService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.TimeZone;

import static cukes.stub.DateStubService.DEFAULT_DATE_FORMAT;

public class CommonSteps extends BaseStepDefinition {

    private Scenario scenario;

    @Autowired
    private DateStubService dateService;

    @Before
    public void before(Scenario scenario) {
        this.scenario = scenario;
    }

    @Before
    public void printastartmarker() {
        System.out.println("\n Start: " + this.scenario.getName() + "\n");
    }

    @After
    public void printamarker() {
         System.out.println("\n End: " + this.scenario.getName() + "\n");
    }

    @Given("^current date is \"(.*?)\"$")
    public void setupCurrentDate(@Format(DEFAULT_DATE_FORMAT) Date currentDate) {
        dateService.setCurrentDate(currentDate);
    }

    @Given("^current date is today with timezone \"(.*?)\"$")
    public void setupCurrentDateAsToday(String timezone) throws Throwable {
        dateService.setCurrentDate(DateServiceImpl.getCurrentDate(TimeZone.getTimeZone(timezone)));
    }
}