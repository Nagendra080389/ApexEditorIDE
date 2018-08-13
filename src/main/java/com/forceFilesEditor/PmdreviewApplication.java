package com.forceFilesEditor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.forceFilesEditor.dao")
public class PmdreviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(PmdreviewApplication.class, args);
	}
}
