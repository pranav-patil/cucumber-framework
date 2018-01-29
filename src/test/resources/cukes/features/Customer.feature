@customer @all

@ClearServiceStub
Feature: Add a new customer and get the customer details.

  Background:
    Given current date is "2016-08-13T12:08:56.235-0600"

  Scenario: Add a new customer details in JSON format
    Given ERP POST "/internal/erp/addCustomer" service returns success when JSON request matches the values
      | CustomerId | FullName       |
      | 2          | John Dillinger |
    And MongoDB sequence "CustomerSeq" has counter 1
    And collection "customer" has data
      | customerId | name           | age     | creationDate                 | lastUpdatedDate              | locked | country |
      | 1          | Jason Bourne   | 31      | 2016-10-01T00:00:00.000-0500 | 2016-10-01T00:00:00.000-0500 | false  | Ghana   |
    And User "Richard Kelvin" login with id "USER01" and businessActivity "ROLE_ADMIN"
    When HTTP POST Service is called with URL "/customer/add" and JSON request
    """
        { "firstName": "John",
          "lastName": "Dillinger",
          "age": 21
        }
    """
    Then Verify HTTP Status is "OK" and response matches with JSON filename "customerAddResponse"
    And Verify that MongoDB collection "customer" has 1 records which match the conditions
      | customerId | name           | age     |
      | 2          | John Dillinger | 21      |
    And Verify that the application log is empty

  Scenario: Add a new customer details in XML format
    Given ERP POST "/internal/erp/addCustomer" service returns success when XML request matches the values
      | ERPCustomer.CustomerId | ERPCustomer.FullName |
      | 3                      | James Bill           |
    And User "Richard Kelvin" login with id "USER01" and businessActivity "ROLE_ADMIN"
    When HTTP POST Service is called with URL "/customer/add" and XML request
    """
       <customer>
            <firstName>James</firstName>
            <lastName>Bill</lastName>
            <age>31</age>
       </customer>
    """
    Then Verify HTTP Status is "OK" and response matches with XML filename "customerAddResponse"
    And Verify that MongoDB collection "customer" has 1 records which match the conditions
      | customerId | name       | age     |
      | 3          | James Bill | 31      |

   Scenario: Find existing customer and fetch customer details by customer id
     Given JSON response for "ERP" GET service "/internal/erp/allCustomers"
       | CustomerId | FullName       | Age | PhoneNumber | Address     | EmailAddress       | Country |
       | 12312      | Jason Bourne   | 31  | 9991348945  | Langley, VA | jbourne@cia.gov    | USA     |
       | 45667      | John Dillinger | 45  | 7092134567  | Chicago, IL | johnDill@gmail.com | USA     |

    When HTTP GET Service is called with URL "/customer/all" returning JSON response
    Then Verify HTTP Status is "OK" and response matches with JSON filename "allCustomersResponse"

  Scenario: ERP System sends JMS notification message which is added to the mongodb
    Given collection "notification" has no records
    When The notification queue receives the XML message with filename "sample_notification.xml"
    Then Verify that MongoDB collection "notification" has 1 records which match the conditions
      | identifier | type | message                  |
      | MSG00345   | INFO | Customer details updated |
