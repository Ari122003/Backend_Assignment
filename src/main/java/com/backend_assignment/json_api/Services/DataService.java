package com.backend_assignment.json_api.Services;

import java.util.Map;

import com.backend_assignment.json_api.DTO.Response.InsertResponse;

public interface DataService {
    public InsertResponse insertRecord(String datasetName, Map<String, Object> jsonData);
}
