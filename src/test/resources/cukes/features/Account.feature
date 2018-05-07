@account @all

@ClearServiceStub
Feature: Add a new customer and get the customer details.

  Background:
    Given current date is "2016-08-13T12:08:56.235-0600"

  Scenario: Add a new account details in JSON format
    Given SQL table "account" has data
      | customerId | firstName | lastName | email         | branchId | accountId | balance | passcode |
      | 1          | Jason     | Bourne   | jbarn@cia.gov | B1234    | A0012     | 45600   | Vivo123  |
    And collection "customer" has data
      | customerId | firstName | lastName  | age | email                | creationDate                 | lastUpdatedDate              | locked | country |
      | 3          | John      | Dillinger | 21  | dillinger@police.gov | 2016-10-01T00:00:00.000-0500 | 2016-10-01T00:00:00.000-0500 | false  | USA     |
    And User "Richard Kelvin" login with id "USER01" and businessActivity "ROLE_ADMIN"
    When HTTP POST Service is called with URL "/account/setup" and JSON request
    """
        { "customerId": "3",
          "passcode": "Secret123",
          "branchId": "B2345",
          "balance": 5600.56
        }
    """
    Then Verify HTTP Status equals "OK"
    And Verify that SQL table "account" has 1 records which match the conditions
      | customerId | firstName | lastName  | email                | branchId | passcode  | balance |
      | 3          | John      | Dillinger | dillinger@police.gov | B2345    | Secret123 | 5600.56 |
    And Verify that 1 records are fetched by SQL query
    """
    SELECT * FROM ACCOUNT WHERE CUSTOMERID = '3'
    """
    And Verify that the application log is empty
