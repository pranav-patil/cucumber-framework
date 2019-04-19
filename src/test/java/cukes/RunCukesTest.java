package cukes;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(
        monochrome = true,
        features = {"src/test/resources/cukes/features"},
        glue = {"cukes.steps"},
        plugin = {"pretty", "html:target/cucumber-html-report", "json:target/cucumber-json-report.json" },
//        tags = {"~@Ignore", "@all"}
        tags = {"@customer"}
)
public class RunCukesTest {
    @BeforeClass()
    public static void initTestEnvironment() {
        System.setProperty("environment", "prod");
    }
}

