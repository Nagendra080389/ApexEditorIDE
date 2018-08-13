package com.forceFilesEditor.dao;

import com.forceFilesEditor.model.UserDomain;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserDomainMongoRepository extends MongoRepository<UserDomain, Integer> {
    UserDomain findByUserIdAndOrgId(String userId, String orgId);
}
