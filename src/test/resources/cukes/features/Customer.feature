@customer @all

@ClearServiceStub
Feature: Add a new customer and get the customer details.

  Background:
    Given current date is "2016-08-13T12:08:56.235-0600"

  Scenario: Add a new customer details
    Given POST "/internal/erp/addCustomer" service returns success when request matches the values
      | customerId | customerName   |
      | 2          | John Dillinger |
    And MongoDB sequence "CustomerSeq" has counter 1
    And collection "customer" has data
      | customerId | name           | age     | creationDate                 | lastUpdatedDate              | locked | country |
      | 1          | Jason Bourne   | 31      | 2016-10-01T00:00:00.000-0500 | 2016-10-01T00:00:00.000-0500 | false  | Ghana   |
    And User "Richard Kelvin" login with id "USER01" and businessActivity "ROLE_ADMIN"
    When HTTP POST Service is called with URL "/customer/add" and JSON request
    """
        { "name": "John Dillinger",
          "age": 21
        }
    """
    Then Verify HTTP Status is "OK" and response matches with JSON filename "customerAddServiceResponse"
    And Verify that MongoDB collection "customer" has 1 records which match the conditions
      | customerId | name           | age     |
      | 2          | John Dillinger | 21      |
    And Verify that the application log contains ""
