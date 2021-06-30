package com.nekonade.dao.db.repository;

import com.nekonade.dao.db.entity.data.task.TaskDB;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskDBRepository extends MongoRepository<TaskDB,String> {
}
