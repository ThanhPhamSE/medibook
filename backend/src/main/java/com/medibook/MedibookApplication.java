package com.medibook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MedibookApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedibookApplication.class, args);

	}

}
