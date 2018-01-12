package cukes.steps;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/resources/cukes/features"},
        glue = {"cukes.steps"},
        format = {"html:target/errors/cucumber-html-report", "json:target/errors/cucumber-report.json"},
//        tags = {"~@Ignore", "@all"}
        tags = {"@customer"}
)
public class RunCukesTest {
    @BeforeClass()
    public static void initTestEnvironment() {
    }
}

