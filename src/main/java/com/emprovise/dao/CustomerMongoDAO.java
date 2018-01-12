package com.emprovise.dao;

import com.emprovise.mongodb.Customer;
import com.mongodb.WriteConcern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class CustomerMongoDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void init() {
        mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
    }

    public void save(Customer customer) throws Exception {
        mongoTemplate.save(customer);
    }

    public void saveAll(List<Customer> customers) throws Exception {
        if (customers != null) {
            for (Customer customer : customers) {
                save(customer);
            }
        }
    }

    public Customer findById(Long itemId) {
        return mongoTemplate.findById(itemId, Customer.class);
    }

    public void delete(Customer customer) {
        mongoTemplate.remove(customer);
    }

    public Customer findByCustomerName(String customerName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(customerName));
        return mongoTemplate.findOne(query, Customer.class);
    }
}
