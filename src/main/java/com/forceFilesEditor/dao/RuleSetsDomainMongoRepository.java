package com.forceFilesEditor.dao;

import com.forceFilesEditor.model.RuleSetsDomain;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

public interface RuleSetsDomainMongoRepository extends MongoRepository<RuleSetsDomain, Integer> {
    RuleSetsDomain findByorgId(String orgId);
}
