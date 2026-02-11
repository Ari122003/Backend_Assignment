package com.backend_assignment.json_api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend_assignment.json_api.Entity.JsonRecord;

@Repository
public interface JsonRecordRepository extends JpaRepository<JsonRecord, Long> {

}
