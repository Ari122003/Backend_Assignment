package com.backend_assignment.json_api.Services;

import java.util.Map;

import com.backend_assignment.json_api.DTO.Response.InsertResponse;
import com.backend_assignment.json_api.DTO.Response.QueryResponse;

public interface DataService {
    public InsertResponse insertRecord(String datasetName, Map<String, Object> jsonData);

    public QueryResponse executeQuery(String datasetName, String groupBy, String sortBy, String order);
}
