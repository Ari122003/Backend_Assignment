package com.backend_assignment.json_api.Services;

import com.backend_assignment.json_api.DTO.Response.InsertResponse;
import com.backend_assignment.json_api.DTO.Response.QueryResponse;
import com.backend_assignment.json_api.Entity.JsonRecord;
import com.backend_assignment.json_api.Exception.InvalidFieldException;
import com.backend_assignment.json_api.Exception.JsonProcessingCustomException;
import com.backend_assignment.json_api.Exception.ResourceNotFoundException;
import com.backend_assignment.json_api.Repository.JsonRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataServiceImplTest {

    @Mock
    private JsonRecordRepository jsonRecordRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DataServiceImpl dataService;

    private Map<String, Object> testJsonData;
    private JsonRecord testJsonRecord;
    private String testDatasetName;

    @BeforeEach
    void setUp() {
        testDatasetName = "testDataset";
        testJsonData = new HashMap<>();
        testJsonData.put("name", "John");
        testJsonData.put("age", 30);
        testJsonData.put("city", "New York");

        testJsonRecord = JsonRecord.builder()
                .id(1L)
                .datasetName(testDatasetName)
                .jsonData("{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}")
                .build();
    }

    @Test
    void testInsertRecord_Success() throws JsonProcessingException {
        // Arrange
        String jsonString = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
        when(objectMapper.writeValueAsString(testJsonData)).thenReturn(jsonString);
        when(jsonRecordRepository.save(any(JsonRecord.class))).thenReturn(testJsonRecord);

        // Act
        InsertResponse response = dataService.insertRecord(testDatasetName, testJsonData);

        // Assert
        assertNotNull(response);
        assertEquals("Record added successfully", response.getMessage());
        assertEquals(testDatasetName, response.getDataset());
        assertEquals(1L, response.getRecordId());

        verify(objectMapper, times(1)).writeValueAsString(testJsonData);
        verify(jsonRecordRepository, times(1)).save(any(JsonRecord.class));
    }

    @Test
    void testInsertRecord_JsonProcessingException() throws JsonProcessingException {
        // Arrange
        when(objectMapper.writeValueAsString(testJsonData))
                .thenThrow(new JsonProcessingException("JSON error") {
                });

        // Act & Assert
        assertThrows(JsonProcessingCustomException.class, () -> {
            dataService.insertRecord(testDatasetName, testJsonData);
        });

        verify(objectMapper, times(1)).writeValueAsString(testJsonData);
        verify(jsonRecordRepository, never()).save(any(JsonRecord.class));
    }

    @Test
    void testExecuteQuery_NoRecordsFound() {
        // Arrange
        when(jsonRecordRepository.findByDatasetName(testDatasetName))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            dataService.executeQuery(testDatasetName, null, null, null);
        });

        assertEquals("No records found for dataset: " + testDatasetName, exception.getMessage());
        verify(jsonRecordRepository, times(1)).findByDatasetName(testDatasetName);
    }

    @Test
    void testExecuteQuery_WithSortingAscending() throws JsonProcessingException {
        // Arrange
        List<JsonRecord> records = createTestRecords();
        when(jsonRecordRepository.findByDatasetName(testDatasetName)).thenReturn(records);

        Map<String, Object> record1 = new HashMap<>();
        record1.put("name", "Alice");
        record1.put("age", 25);

        Map<String, Object> record2 = new HashMap<>();
        record2.put("name", "Bob");
        record2.put("age", 35);

        Map<String, Object> record3 = new HashMap<>();
        record3.put("name", "Charlie");
        record3.put("age", 30);

        when(objectMapper.readValue(records.get(0).getJsonData(), Map.class)).thenReturn(record1);
        when(objectMapper.readValue(records.get(1).getJsonData(), Map.class)).thenReturn(record2);
        when(objectMapper.readValue(records.get(2).getJsonData(), Map.class)).thenReturn(record3);

        // Act
        QueryResponse response = dataService.executeQuery(testDatasetName, null, "age", "asc");

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSortedRecords());
        assertEquals(3, response.getSortedRecords().size());
        assertEquals(25, response.getSortedRecords().get(0).get("age"));
        assertEquals(30, response.getSortedRecords().get(1).get("age"));
        assertEquals(35, response.getSortedRecords().get(2).get("age"));

        verify(jsonRecordRepository, times(1)).findByDatasetName(testDatasetName);
    }

    @Test
    void testExecuteQuery_WithSortingDescending() throws JsonProcessingException {
        // Arrange
        List<JsonRecord> records = createTestRecords();
        when(jsonRecordRepository.findByDatasetName(testDatasetName)).thenReturn(records);

        Map<String, Object> record1 = new HashMap<>();
        record1.put("name", "Alice");
        record1.put("age", 25);

        Map<String, Object> record2 = new HashMap<>();
        record2.put("name", "Bob");
        record2.put("age", 35);

        Map<String, Object> record3 = new HashMap<>();
        record3.put("name", "Charlie");
        record3.put("age", 30);

        when(objectMapper.readValue(records.get(0).getJsonData(), Map.class)).thenReturn(record1);
        when(objectMapper.readValue(records.get(1).getJsonData(), Map.class)).thenReturn(record2);
        when(objectMapper.readValue(records.get(2).getJsonData(), Map.class)).thenReturn(record3);

        // Act
        QueryResponse response = dataService.executeQuery(testDatasetName, null, "age", "desc");

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSortedRecords());
        assertEquals(3, response.getSortedRecords().size());
        assertEquals(35, response.getSortedRecords().get(0).get("age"));
        assertEquals(30, response.getSortedRecords().get(1).get("age"));
        assertEquals(25, response.getSortedRecords().get(2).get("age"));

        verify(jsonRecordRepository, times(1)).findByDatasetName(testDatasetName);
    }

    @Test
    void testExecuteQuery_WithStringSorting() throws JsonProcessingException {
        // Arrange
        List<JsonRecord> records = createTestRecords();
        when(jsonRecordRepository.findByDatasetName(testDatasetName)).thenReturn(records);

        Map<String, Object> record1 = new HashMap<>();
        record1.put("name", "Charlie");
        record1.put("age", 25);

        Map<String, Object> record2 = new HashMap<>();
        record2.put("name", "Alice");
        record2.put("age", 35);

        Map<String, Object> record3 = new HashMap<>();
        record3.put("name", "Bob");
        record3.put("age", 30);

        when(objectMapper.readValue(records.get(0).getJsonData(), Map.class)).thenReturn(record1);
        when(objectMapper.readValue(records.get(1).getJsonData(), Map.class)).thenReturn(record2);
        when(objectMapper.readValue(records.get(2).getJsonData(), Map.class)).thenReturn(record3);

        // Act
        QueryResponse response = dataService.executeQuery(testDatasetName, null, "name", "asc");

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSortedRecords());
        assertEquals(3, response.getSortedRecords().size());
        assertEquals("Alice", response.getSortedRecords().get(0).get("name"));
        assertEquals("Bob", response.getSortedRecords().get(1).get("name"));
        assertEquals("Charlie", response.getSortedRecords().get(2).get("name"));

        verify(jsonRecordRepository, times(1)).findByDatasetName(testDatasetName);
    }

    @Test
    void testExecuteQuery_WithGrouping() throws JsonProcessingException {
        // Arrange
        List<JsonRecord> records = createTestRecords();
        when(jsonRecordRepository.findByDatasetName(testDatasetName)).thenReturn(records);

        Map<String, Object> record1 = new HashMap<>();
        record1.put("name", "Alice");
        record1.put("city", "New York");

        Map<String, Object> record2 = new HashMap<>();
        record2.put("name", "Bob");
        record2.put("city", "London");

        Map<String, Object> record3 = new HashMap<>();
        record3.put("name", "Charlie");
        record3.put("city", "New York");

        when(objectMapper.readValue(records.get(0).getJsonData(), Map.class)).thenReturn(record1);
        when(objectMapper.readValue(records.get(1).getJsonData(), Map.class)).thenReturn(record2);
        when(objectMapper.readValue(records.get(2).getJsonData(), Map.class)).thenReturn(record3);

        // Act
        QueryResponse response = dataService.executeQuery(testDatasetName, "city", null, null);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getGroupedRecords());
        assertEquals(2, response.getGroupedRecords().size());
        assertTrue(response.getGroupedRecords().containsKey("New York"));
        assertTrue(response.getGroupedRecords().containsKey("London"));
        assertEquals(2, response.getGroupedRecords().get("New York").size());
        assertEquals(1, response.getGroupedRecords().get("London").size());

        verify(jsonRecordRepository, times(1)).findByDatasetName(testDatasetName);
    }

    @Test
    void testExecuteQuery_GroupByFieldNotFound() throws JsonProcessingException {
        // Arrange
        List<JsonRecord> records = createTestRecords();
        when(jsonRecordRepository.findByDatasetName(testDatasetName)).thenReturn(records);

        Map<String, Object> record1 = new HashMap<>();
        record1.put("name", "Alice");
        record1.put("age", 25);

        when(objectMapper.readValue(records.get(0).getJsonData(), Map.class)).thenReturn(record1);

        // Act & Assert
        InvalidFieldException exception = assertThrows(InvalidFieldException.class, () -> {
            dataService.executeQuery(testDatasetName, "country", null, null);
        });

        assertTrue(exception.getMessage().contains("Group by field 'country' not found in record"));
        verify(jsonRecordRepository, times(1)).findByDatasetName(testDatasetName);
    }

    @Test
    void testExecuteQuery_SortByFieldNotFound() throws JsonProcessingException {
        // Arrange
        List<JsonRecord> records = createTestRecords();
        when(jsonRecordRepository.findByDatasetName(testDatasetName)).thenReturn(records);

        Map<String, Object> record1 = new HashMap<>();
        record1.put("name", "Alice");
        record1.put("age", 25);

        Map<String, Object> record2 = new HashMap<>();
        record2.put("name", "Bob");
        record2.put("age", 35);

        Map<String, Object> record3 = new HashMap<>();
        record3.put("name", "Charlie");
        record3.put("age", 30);

        when(objectMapper.readValue(records.get(0).getJsonData(), Map.class)).thenReturn(record1);
        when(objectMapper.readValue(records.get(1).getJsonData(), Map.class)).thenReturn(record2);
        when(objectMapper.readValue(records.get(2).getJsonData(), Map.class)).thenReturn(record3);

        // Act & Assert
        InvalidFieldException exception = assertThrows(InvalidFieldException.class, () -> {
            dataService.executeQuery(testDatasetName, null, "country", "asc");
        });

        assertTrue(exception.getMessage().contains("Sort by field 'country' not found in record"));
        verify(jsonRecordRepository, times(1)).findByDatasetName(testDatasetName);
    }

    @Test
    void testExecuteQuery_JsonProcessingExceptionDuringQuery() throws JsonProcessingException {
        // Arrange
        List<JsonRecord> records = createTestRecords();
        when(jsonRecordRepository.findByDatasetName(testDatasetName)).thenReturn(records);
        when(objectMapper.readValue(anyString(), eq(Map.class)))
                .thenThrow(new JsonProcessingException("JSON parse error") {
                });

        // Act & Assert
        assertThrows(JsonProcessingCustomException.class, () -> {
            dataService.executeQuery(testDatasetName, null, null, null);
        });

        verify(jsonRecordRepository, times(1)).findByDatasetName(testDatasetName);
    }

    @Test
    void testExecuteQuery_NoSortingOrGrouping() throws JsonProcessingException {
        // Arrange
        List<JsonRecord> records = createTestRecords();
        when(jsonRecordRepository.findByDatasetName(testDatasetName)).thenReturn(records);

        Map<String, Object> record1 = new HashMap<>();
        record1.put("name", "Alice");
        record1.put("age", 25);

        Map<String, Object> record2 = new HashMap<>();
        record2.put("name", "Bob");
        record2.put("age", 35);

        Map<String, Object> record3 = new HashMap<>();
        record3.put("name", "Charlie");
        record3.put("age", 30);

        when(objectMapper.readValue(records.get(0).getJsonData(), Map.class)).thenReturn(record1);
        when(objectMapper.readValue(records.get(1).getJsonData(), Map.class)).thenReturn(record2);
        when(objectMapper.readValue(records.get(2).getJsonData(), Map.class)).thenReturn(record3);

        // Act
        QueryResponse response = dataService.executeQuery(testDatasetName, null, null, null);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSortedRecords());
        assertEquals(3, response.getSortedRecords().size());
        assertNull(response.getGroupedRecords());

        verify(jsonRecordRepository, times(1)).findByDatasetName(testDatasetName);
    }

    private List<JsonRecord> createTestRecords() {
        JsonRecord record1 = JsonRecord.builder()
                .id(1L)
                .datasetName(testDatasetName)
                .jsonData("{\"name\":\"Alice\",\"age\":25}")
                .build();

        JsonRecord record2 = JsonRecord.builder()
                .id(2L)
                .datasetName(testDatasetName)
                .jsonData("{\"name\":\"Bob\",\"age\":35}")
                .build();

        JsonRecord record3 = JsonRecord.builder()
                .id(3L)
                .datasetName(testDatasetName)
                .jsonData("{\"name\":\"Charlie\",\"age\":30}")
                .build();

        return Arrays.asList(record1, record2, record3);
    }
}
