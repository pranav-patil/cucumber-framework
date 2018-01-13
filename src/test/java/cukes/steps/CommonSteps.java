package cukes.steps;

import com.library.datatypes.Role;
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

    @Given("^I'm the \"([^\"]*)\" user with the id \"(.*?)\"$")
    public void i_m_a_user(Role role, String userId) throws Throwable {

        switch (Integer.parseInt(userId)) {
            case 235003:
                if (role.equals(Role.ADMINISTRATOR)) {
                    setUserDetails("X087150", "Holly", "James");
                } else if (role.equals(Role.CONTRACTOR)) {
                    setUserDetails("X410604", "Kelly", "Henshaw");
                } else if (role.equals(Role.EMPLOYEE)) {
                    setUserDetails("X934869", "BOUN", "Khamwanthong");
                }
                break;
            case 235054:
                if (role.equals(Role.ADMINISTRATOR)) {
                    setUserDetails("X632815", "Dylan", "Saddington");
                } else if (role.equals(Role.BUSINESS)) {
                    setUserDetails("X122829", "Wayne", "Henshaw");
                } else if (role.equals(Role.CONTRACTOR)) {
                    setUserDetails("X013050", "JENNY", "Shingles");
                }
                break;
            case 234040:
                if (role.equals(Role.ADMINISTRATOR)) {
                    setUserDetails("X632815", "Dylan", "Saddington");
                } else if (role.equals(Role.MANAGER)) {
                    setUserDetails("X122829", "Wayne", "Henshaw");
                } else if (role.equals(Role.SALES_REPRESENTATIVE)) {
                    setUserDetails("X013050", "JENNY", "Shingles");
                }
                break;
            default:
                setUserDetails("JJ92167", "Bad", "DealerID");
                break;
        }
    }

    @Given("^current date is \"(.*?)\"$")
    public void setupCurrentDate(@Format(DEFAULT_DATE_FORMAT) Date currentDate) {
        dateService.setCurrentDate(currentDate);
    }

    @Given("^current date is today with timezone \"(.*?)\"$")
    public void setupCurrentDateAsToday(String timezone) throws Throwable {
        dateService.setCurrentDate(DateServiceImpl.getCurrentDate(TimeZone.getTimeZone(timezone)));
    }

    private void setUserNameByRole(String userId, String role, String firstName) {

        sessionUtility.setUserId(userId);
        sessionUtility.setFirstName(firstName);

        if (role.equalsIgnoreCase("Employee")) {
            sessionUtility.setLastName("Employee");
        } else if (role.equalsIgnoreCase("Administrator")) {
            sessionUtility.setLastName("Admin");
        } else if (role.equalsIgnoreCase("Manager")) {
            sessionUtility.setLastName("Manager");
        } else {
            sessionUtility.setLastName("Unknown Role");
        }
    }

    private void setUserDetails(String userId, String firstName, String lastName) {
        sessionUtility.setUserId(userId);
        sessionUtility.setFirstName(firstName);
        sessionUtility.setLastName(lastName);
    }
}