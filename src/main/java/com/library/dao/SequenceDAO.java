package com.library.dao;

import com.library.mongodb.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class SequenceDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Long getNextSequence(String sequenceName, Long sequenceStart) {
        Sequence sequence = new Sequence(sequenceName, sequenceStart);
        Query seqQuery = new Query(Criteria.where("name").is(sequenceName));
        Sequence existingSeq = mongoTemplate.findOne(seqQuery, Sequence.class);
        if (existingSeq == null) {
            mongoTemplate.insert(sequence);
        }

        final Sequence tagSequence = mongoTemplate.findAndModify(seqQuery,
                new Update().inc("counter", 1),
                new FindAndModifyOptions().returnNew(true),
                Sequence.class);

        return tagSequence.getCounter();
    }
}
