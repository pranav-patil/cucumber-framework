package com.library.mongodb.dao;

import com.library.mongodb.domain.Notification;
import com.mongodb.WriteConcern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class NotificationDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void init() {
        mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
    }

    public void save(Notification notification) {
        mongoTemplate.save(notification);
    }
}
