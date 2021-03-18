package edu.att4sd.services;

import org.springframework.stereotype.Service;

import edu.att4sd.model.TelemetryValue;

@Service
public class TopicService {
	
	private static final String TEMPORARY_IMPLEMENTATION = "Temporary implementation";
	
	public void addTelemetryValue(String topicPath, TelemetryValue newValue) {
		throw new UnsupportedOperationException(TEMPORARY_IMPLEMENTATION);
	}
	
}
