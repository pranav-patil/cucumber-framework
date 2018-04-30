package com.library.hibernate.dao;
import com.library.hibernate.domain.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionDAO extends JpaRepository<TransactionRecord, Long> {

    @Query(value = "select * from #{#entityName} b where b.collection=?1", nativeQuery = true)
    List<TransactionRecord> findByCollection(String collection);

    @Query(value = "select collection,service,refreshDuration,executionDate from ScheduleTask b where b.collection = :collection AND b.service=:service")
    List<TransactionRecord> findByCollectionAndService(@Param("collection") String collection, @Param("service") String service);
}
