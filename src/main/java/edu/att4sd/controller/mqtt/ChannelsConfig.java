package edu.att4sd.controller.mqtt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;

@Configuration
public class ChannelsConfig {
	
	private static final String TEMPORARY_IMPLEMENTATION = "Temporary implementation";
	
	@Bean(name="commandChannel")
	public MessageChannel commandChannel() {
		throw new UnsupportedOperationException(TEMPORARY_IMPLEMENTATION);
	}
	
}
