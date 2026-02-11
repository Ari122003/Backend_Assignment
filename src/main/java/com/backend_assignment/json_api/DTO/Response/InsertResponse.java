package com.backend_assignment.json_api.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder

public class InsertResponse {

    private String message;
    private String dataset;
    private long recordId;

}
