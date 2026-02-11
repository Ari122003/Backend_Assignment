package com.backend_assignment.json_api.Services;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.backend_assignment.json_api.DTO.Response.InsertResponse;
import com.backend_assignment.json_api.Entity.JsonRecord;
import com.backend_assignment.json_api.Repository.JsonRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataServiceImpl implements DataService {

    private final JsonRecordRepository jsonRecordRepository;
    private final ObjectMapper objectMapper;

    @Override
    public InsertResponse insertRecord(String datasetName, Map<String, Object> jsonData) {

        try {
            String jsonString = objectMapper.writeValueAsString(jsonData);

            JsonRecord jsonRecord = JsonRecord.builder()
                    .datasetName(datasetName)
                    .jsonData(jsonString)
                    .build();

            JsonRecord savedRecord = jsonRecordRepository.save(jsonRecord);

            return InsertResponse.builder()
                    .message("Record added successfully")
                    .dataset(datasetName)
                    .recordId(savedRecord.getId())
                    .build();

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON data", e);
        }
    }
}
