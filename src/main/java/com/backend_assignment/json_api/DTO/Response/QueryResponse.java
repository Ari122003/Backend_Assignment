package com.backend_assignment.json_api.DTO.Response;

import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryResponse {

    private Map<String, List<Map<String, Object>>> groupedRecords;

    private List<Map<String, Object>> sortedRecords;
}