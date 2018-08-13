package com.forceFilesEditor.dao;

import com.forceFilesEditor.model.RuleSetsDomain;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RuleSetsDomainMongoRepository extends MongoRepository<RuleSetsDomain, Integer> {
    RuleSetsDomain findByOrgId(String orgId);
}
