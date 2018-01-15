package cukes.steps;


import com.library.jms.JMSMessageSender;
import cucumber.api.java.en.When;
import cukes.type.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

public class JmsSteps extends BaseStepDefinition {

    @Autowired
    private JMSMessageSender jmsMessageSender;

    public static final String BASE_JMS_MESSAGE_PATH = "/cukes/integration/jms-messages/";

    @When("^The notification queue receives the (JSON|XML|FORM) message with filename \"(.*?)\"$")
    public void sendNotification(ContentType contentType, String filename) throws Exception {
        String requestPayload = getFileContent(contentType, BASE_JMS_MESSAGE_PATH + filename);
        jmsMessageSender.sendMessage(requestPayload);
    }
}
