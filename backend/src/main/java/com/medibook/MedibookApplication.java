package com.medibook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = "com.medibook")
@EnableScheduling
@EnableCaching
public class MedibookApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedibookApplication.class, args);

	}

}
