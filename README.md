# Cucumber Integration Test Showcase

The basic premise on which cucumber test framework is setup on is as follows:

* All the Cucumber Steps should be generic as possible providing complete reusability.
* Cucumber tests are for integration testing and not for any documentation, business reference.
* All the external systems connecting to the application must be either stubbed or mocked.

In order to run the cucumber tests, execute RunCukesTest.java.

Cucumber Features.

* Supports JSON, XML and FORM (urlencoded form) content types.
* Json matching using [jsonassert](https://github.com/skyscreamer/JSONassert) and determine values in json path expression using [json-path](https://github.com/json-path/JsonPath).
* [XmlUnit](https://github.com/xmlunit/xmlunit) is used for XML matching and XPath for path expression match.
* Supports [MongoDB](https://www.mongodb.com/) currently.
* [Embedded MongoDB](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo) which executes in separate process.
* HttpSteps help to call any HTTP service mostly using REST, but can be customized to use any other format.
* ServiceSteps provide integration with any external service, may it be for fetching the data or posting the data.
* JmsSteps provides ability to send messages to the mock queue using [mockrunner](https://mockrunner.github.io/).
* SecuritySteps lets you define the user id for login and the access roles associated with the user.
* MongoSteps enables to add, delete records, execute queries and verify records with fields exists in the database.
* Supports [Auto Mock](https://github.com/rinoto/spring-auto-mock) which [dynamically mocks](https://dzone.com/articles/automatically-inject-mocks) dependent beans which are not found in Spring configuration.
