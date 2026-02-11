package com.backend_assignment.json_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class BackendAssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendAssignmentApplication.class, args);
		log.info("Starting The Application.......");
	}

}
