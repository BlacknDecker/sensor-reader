package edu.att4sd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;


@SpringBootApplication
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class}) 	// Prevent monitor thread exception
public class SensorReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(SensorReaderApplication.class, args);
	}

}
