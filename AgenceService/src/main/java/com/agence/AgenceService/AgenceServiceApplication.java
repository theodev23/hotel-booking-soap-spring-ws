package com.agence.AgenceService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.agence")
public class AgenceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgenceServiceApplication.class, args);
	}

}
