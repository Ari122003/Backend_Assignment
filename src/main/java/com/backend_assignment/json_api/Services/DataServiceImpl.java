package com.backend_assignment.json_api.Services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.backend_assignment.json_api.DTO.Response.InsertResponse;
import com.backend_assignment.json_api.DTO.Response.QueryResponse;
import com.backend_assignment.json_api.Entity.JsonRecord;
import com.backend_assignment.json_api.Exception.InvalidFieldException;
import com.backend_assignment.json_api.Exception.JsonProcessingCustomException;
import com.backend_assignment.json_api.Exception.ResourceNotFoundException;
import com.backend_assignment.json_api.Repository.JsonRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataServiceImpl implements DataService {

    private final JsonRecordRepository jsonRecordRepository;
    private final ObjectMapper objectMapper;

    // Inserts a new JSON record into the specified dataset

    @Override
    @Transactional
    public InsertResponse insertRecord(String dataset, Map<String, Object> jsonData) {

        try {
            // Convert Map to JSON string for storage
            String jsonString = objectMapper.writeValueAsString(jsonData);

            // Build and save the record entity
            JsonRecord jsonRecord = JsonRecord.builder()
                    .datasetName(dataset)
                    .jsonData(jsonString)
                    .build();

            JsonRecord savedRecord = jsonRecordRepository.save(jsonRecord);

            // Return success response with record details
            return InsertResponse.builder()
                    .message("Record added successfully")
                    .dataset(dataset)
                    .recordId(savedRecord.getId())
                    .build();

        } catch (JsonProcessingException e) {
            throw new JsonProcessingCustomException("Error processing JSON data", e);
        }
    }

    // Executes queries on dataset with support for grouping and sorting

    @Override
    public QueryResponse executeQuery(String datasetName, String groupBy, String sortBy, String order) {

        // Fetch all records for the dataset
        List<JsonRecord> data = jsonRecordRepository.findByDatasetName(datasetName);

        if (data.isEmpty()) {
            throw new ResourceNotFoundException("No records found for dataset: " + datasetName);
        }

        // Deserialize JSON strings to Map objects
        List<Map<String, Object>> records = new ArrayList<>();

        data.forEach(record -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> json = (Map<String, Object>) objectMapper.readValue(record.getJsonData(),
                        Map.class);
                records.add(json);
            } catch (JsonProcessingException e) {
                throw new JsonProcessingCustomException("Error processing JSON data", e);
            }
        });

        // Handle GROUP BY operation if requested
        if (groupBy != null && !groupBy.isEmpty()) {

            Map<String, List<Map<String, Object>>> groupedRecords = new HashMap<>();

            // Group records by the specified field value
            for (Map<String, Object> record : records) {

                if (record.get(groupBy) == null) {
                    throw new InvalidFieldException("Group by field '" + groupBy + "' not found in record");
                }

                groupedRecords.computeIfAbsent(record.get(groupBy).toString(), k -> new ArrayList<>()).add(record);
            }

            return QueryResponse.builder()
                    .groupedRecords(groupedRecords)
                    .build();

        }

        // Handle SORT BY operation if requested
        List<Map<String, Object>> sortedRecords = new ArrayList<>(records);

        if (sortBy != null && !sortBy.isEmpty()) {

            // Create a comparator that handles both numeric and string values
            Comparator<Map<String, Object>> comparator = Comparator.comparing(
                    record -> {
                        Object value = record.get(sortBy);
                        if (value == null) {
                            throw new InvalidFieldException(
                                    "Sort by field '" + sortBy + "' not found in record");
                        }
                        return value;
                    },
                    (v1, v2) -> {
                        // Compare numerically if both values are numbers
                        if (v1 instanceof Number && v2 instanceof Number) {
                            return Double.compare(
                                    ((Number) v1).doubleValue(),
                                    ((Number) v2).doubleValue());
                        }
                        // Otherwise compare as strings
                        return v1.toString().compareTo(v2.toString());
                    });

            // Reverse order if descending is requested
            if ("desc".equalsIgnoreCase(order)) {
                comparator = comparator.reversed();
            }

            sortedRecords.sort(comparator);

        }

        // Return sorted (or unsorted if no sortBy) records
        return QueryResponse.builder()
                .sortedRecords(sortedRecords)
                .build();
    }
}
