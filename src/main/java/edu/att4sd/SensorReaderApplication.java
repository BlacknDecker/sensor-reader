package edu.att4sd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableAutoConfiguration
public class SensorReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(SensorReaderApplication.class, args);
	}

}
