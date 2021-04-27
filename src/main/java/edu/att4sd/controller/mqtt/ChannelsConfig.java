package edu.att4sd.controller.mqtt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableIntegration
public class ChannelsConfig {
	
	@Bean(name="mqttReceiverOutputChannel")
	public MessageChannel mqttReceiverOutputChannel() {
		return new DirectChannel();
	}
	
	@Bean(name="commandChannel")
	public MessageChannel commandChannel() {
		return new DirectChannel();
	}

}
