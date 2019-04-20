package cukes.steps;

import com.library.service.DateServiceImpl;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cukes.stub.DateStubService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

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

    @Given("current date is {date_iso_local_date_time}")
    public void setupCurrentDate(LocalDateTime currentDate) {
        dateService.setCurrentDate(java.util.Date.from(currentDate.atZone(ZoneId.systemDefault()).toInstant()));
    }

    @Given("^current date is today with timezone \"(.*?)\"$")
    public void setupCurrentDateAsToday(String timezone) {
        dateService.setCurrentDate(DateServiceImpl.getCurrentDate(TimeZone.getTimeZone(timezone)));
    }
}