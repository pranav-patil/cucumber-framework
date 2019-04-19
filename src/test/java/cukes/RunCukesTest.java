package cukes;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.presentation.PresentationMode;
import net.masterthought.cucumber.sorting.SortingMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    public static void setup() {
        System.setProperty("environment", "prod");
    }

    @AfterClass
    public static void teardown() {
        Runtime.getRuntime().addShutdownHook(new Thread(RunCukesTest::generateHTMLReports));
    }

    private static void generateHTMLReports() {
        File reportOutputDirectory = new File("target");
        List<String> jsonFiles = new ArrayList<>();
        jsonFiles.add("target/cucumber-json-report.json");

        String buildNumber = "1";
        String projectName = "Cucumber Framework Showcase";
        Configuration configuration = new Configuration(reportOutputDirectory, projectName);
        configuration.setBuildNumber(buildNumber);

        configuration.addClassifications("Platform", "Windows");
        configuration.addClassifications("Browser", "Chrome");
        configuration.addClassifications("Branch", "release/1.0");
        configuration.setSortingMethod(SortingMethod.NATURAL);
        configuration.addPresentationModes(PresentationMode.EXPAND_ALL_STEPS);

        ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, configuration);
        reportBuilder.generateReports();
    }
}

