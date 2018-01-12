# Cucumber Integration Test Showcase

The basic premise on which cucumber test framework is setup on is as follows:

* All the Cucumber Steps should be generic as possible providing complete reusability.
* Cucumber tests are for integration testing and not for any documentation, business reference.
* All the external systems connecting to the application must be either stubbed or mocked.

In order to run the cucumber tests, execute RunCukesTest.java.

Cucumber Features.

* Embedded in-memory MongoDB which executes in separate process.
* Json matching using jsonassert and determine values in json path expression using json-path.
* HttpSteps help to call any HTTP service mostly using REST, but can be customized to use any other format.
* ServiceSteps provide integration with any external service, may it be for fetching the data or posting the data.

