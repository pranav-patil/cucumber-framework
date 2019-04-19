# Cucumber Integration Test Showcase

Integration testing is a vital for testing the functionality of an application, more so than JUnit tests in some cases. [Cucumber-JVM](https://github.com/cucumber/cucumber-jvm) is prominent tool use for  Behaviour-Driven Development(BDD). It is mostly recommended to write well documented features in plain English and code corresponding java glue methods to perform step actions. But with this approach writing tests requires indepth understanding of Java as well as application code which prevents people with just Quality assurance background to contribute directly in writing tests.  
The Cucumber Integration Test Showcase is setup based on the premise that, **Tests should be fragile, easy to setup/maintain with very little or no understanding of underlying application code**.   

Stubbing or setting up Test data is one of the most painful part of any integration test. Usually this is achieved as follows: 

* Setting up mock objects in java which would be returned by stubbed service layer, which works fine but is tied to application classes (including service layer) and involves heavy maintenance. 
* Setting up test data in test environment (which is hard enough) and invoking actual systems in test environment. This requires special infrastructure for integration testing alone and makes the tests dependent on the environment.
* Using reusable JS functions/expressions and Gerkin to setup test doubles with [Karate](https://github.com/intuit/karate), a cucumber based web testing tool. This is easier compared to above but still requires some learning curve and writing long JS/Gerkin statements.

The Cucumber Integration Test Showcase, combines the simplicity from first approach with the reusability from last approach. The stubs/mocks are easy to setup with very less or no custom code, using just standard feature steps and hard coded response files. It also provides full fledged mock databases both SQL and MongoDb, and mock JMS capability. It uses **spring.active.profile** property to stub out http clients which invoke external services to provide a hardcoded response based on request parameters.

### Cucumber Features

* Supports JSON, XML and FORM (urlencoded form) content types.
* Integrated [WireMock](http://wiremock.org) server provides all the mock services which return mock responses after validating the requests.
* Json matching using [jsonassert](https://github.com/skyscreamer/JSONassert) and determine values in json path expression using [json-path](https://github.com/json-path/JsonPath).
* [XmlUnit](https://github.com/xmlunit/xmlunit) is used for XML matching and XPath for path expression match.
* Supports most of SQL databases and [MongoDB](https://www.mongodb.com/).
* [Embedded H2 Database](http://www.h2database.com/html/main.html) executes as an in-memory SQL database.
* [Embedded MongoDB](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo) which executes in separate process.
* HttpSteps help to call any HTTP service mostly using REST, but can be customized to use any other format.
* ServiceSteps sets up stubs for WireMock service providing external mock services.
* JmsSteps provides ability to send messages to the mock queue using [mockrunner](https://mockrunner.github.io/).
* SecuritySteps lets you define the user id for login and the access roles associated with the user.
* SQLSteps enables to insert, delete records, execute basic SQL queries and verify records with fields exists in the SQL database.
* MongoSteps enables to add, delete records, execute queries and verify records with fields exists in the MongoDB.
* Supports [Auto Mock](https://github.com/rinoto/spring-auto-mock) which [dynamically mocks](https://dzone.com/articles/automatically-inject-mocks) dependent beans which are not found in Spring configuration.

### Running Cucumber Tests

In order to run the cucumber tests, either execute RunCukesTest.java from IDE or execute below maven command.

    $ mvn test

### Example Cucumber Tests

Below are some of the Cucumber Examples. All the below steps are generic and can be used to call HTTP service, storing/verify records in MongoDB etc. In the below example the first Given step adds a stub in WireMock service so that when the application client invokes the POST service `/internal/erp/addCustomer` and the request payload has attributes `CustomerId` with value `1` and `FullName` with value `John Dillinger`, then the stub returns an HTTP 200 response.
The next given step resets the sequence `CustomerSeq` in embedded MongoDB to 1. Then given step `collection "customer" has data` adds a records in customer collection with field/values specified in the Data table.
A user session is created for user Richard Kelvin and id USER01, using the last given step. Hence all the stubs replace any external dependencies, including services, databases, jms and security.
Now the When HTTP step simulates an HTTP call through MockMvc to the service controllers with the specified request payload. The call returns with a success or failure depending on the test scenario.
Finally the Then verify HTTP step validate the response from the simulated HTTP call checking the HTTP status and the response payload. We also verify the status of mongoDB verifying that a new record was indeed added in customer collection which match the field/values specified in the Data table.
In some cases if we want to look for specific error message being logged rather than the standard HTTP response, that can be verified using the last verify step checking the application log. 

```gherkin
  Scenario: Add a new customer details in JSON format
    Given ERP POST "/internal/erp/addCustomer" service returns success when JSON request matches the values
      | CustomerId | FullName       |
      | 2          | John Dillinger |
    And MongoDB sequence "CustomerSeq" has counter 1
    And collection "customer" has data
      | customerId | firstName | lastName | age | email         | creationDate                 | lastUpdatedDate              | locked | country |
      | 1          | Jason     | Bourne   | 31  | jbarn@cia.gov | 2016-10-01T00:00:00.000-0500 | 2016-10-01T00:00:00.000-0500 | false  | Ghana   |
    And User "Richard Kelvin" login with id "USER01" and businessActivity "ROLE_ADMIN"
    When HTTP POST Service is called with URL "/customer/add" and JSON request
    """
        { "firstName": "John",
          "lastName": "Dillinger",
          "age": 21,
          "email": "dillinger@police.gov"
        }
    """
    Then Verify HTTP Status is "OK" and response matches with JSON filename "customerAddResponse"
    And Verify that MongoDB collection "customer" has 1 records which match the conditions
      | customerId | firstName | lastName  | age | email                |
      | 2          | John      | Dillinger | 21  | dillinger@police.gov |
    And Verify that the application log is empty
```

