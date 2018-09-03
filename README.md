# Cucumber Integration Test Showcase

Integration testing is a vital for testing the functionality of an application, more so than JUnit tests in some cases. [Cucumber-JVM](https://github.com/cucumber/cucumber-jvm) is prominent tool use for  Behaviour-Driven Development(BDD). It is mostly recommended to write well documented features in plain English and code corresponding java glue methods to perform step actions. But with this approach writing tests requires indepth understanding of Java as well as application code which prevents people with just Quality assurance background to contribute directly in writing tests.  
The Cucumber Integration Test Showcase is setup based on the premise that, **Tests should be fragile, easy to setup/maintain with very little or no understanding of underlying application code**.   

Stubbing or setting up Test data is one of the most painful part of any integration test. Usually this is achieved as follows: 

* Setting up mock objects in java which would be returned by stubbed service layer, which works fine but is tied to application classes (including service layer) and involves heavy maintenance. 
* Setting up test data in test environment (which is hard enough) and invoking actual systems in test environment. This requires special infrastructure for integration testing alone and makes the tests dependent on the environment.
* Using reusable JS functions/expressions and Gerkin to setup test doubles with [Karate](https://github.com/intuit/karate), a cucumber based web testing tool. This is easier compared to above but still requires some learning curve and writing long JS/Gerkin statements.

The Cucumber Integration Test Showcase, combines the simplicity from first approach with the reusability from last approach. The stubs/mocks are easy to setup with very less or no custom code, using just standard feature steps and hard coded response files. It also provides full fledged mock databases both SQL and MongoDb, and mock JMS capability.
It uses **spring.active.profile** property to stub out http clients which invoke external services to provide a hardcoded response based on request parameters. 
In order to run the cucumber tests, execute RunCukesTest.java.

Cucumber Features.

* Supports JSON, XML and FORM (urlencoded form) content types.
* Json matching using [jsonassert](https://github.com/skyscreamer/JSONassert) and determine values in json path expression using [json-path](https://github.com/json-path/JsonPath).
* [XmlUnit](https://github.com/xmlunit/xmlunit) is used for XML matching and XPath for path expression match.
* Supports most of SQL databases and [MongoDB](https://www.mongodb.com/).
* [Embedded H2 Database](http://www.h2database.com/html/main.html) executes as an in-memory SQL database.
* [Embedded MongoDB](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo) which executes in separate process.
* HttpSteps help to call any HTTP service mostly using REST, but can be customized to use any other format.
* ServiceSteps provide integration with any external service, may it be for fetching the data or posting the data.
* JmsSteps provides ability to send messages to the mock queue using [mockrunner](https://mockrunner.github.io/).
* SecuritySteps lets you define the user id for login and the access roles associated with the user.
* SQLSteps enables to insert, delete records, execute basic SQL queries and verify records with fields exists in the SQL database.
* MongoSteps enables to add, delete records, execute queries and verify records with fields exists in the MongoDB.
* Supports [Auto Mock](https://github.com/rinoto/spring-auto-mock) which [dynamically mocks](https://dzone.com/articles/automatically-inject-mocks) dependent beans which are not found in Spring configuration.
