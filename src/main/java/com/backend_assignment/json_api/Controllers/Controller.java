package com.backend_assignment.json_api.Controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend_assignment.json_api.DTO.Response.InsertResponse;
import com.backend_assignment.json_api.Services.DataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class Controller {

    private final DataService dataService;

    @PostMapping("/dataset/{datasetName}/record")
    public ResponseEntity<InsertResponse> insertController(@PathVariable String datasetName,
            @RequestBody Map<String, Object> jsonData) {

        if (jsonData == null || jsonData.isEmpty()) {
            throw new IllegalArgumentException("JSON body cannot be empty");
        }

        InsertResponse response = dataService.insertRecord(datasetName, jsonData);

        return ResponseEntity.ok(response);
    }

}
