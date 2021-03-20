package edu.att4sd.controller.mqtt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.ExpressionControlBusFactoryBean;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableIntegration
public class ControlBusConfig {
	
	@Bean
	public MessageChannel commandChannel() {
		return new DirectChannel();
	}
			
	@Bean
	@ServiceActivator(inputChannel = "commandChannel")
	public ExpressionControlBusFactoryBean controlBus() {
	    return new ExpressionControlBusFactoryBean();
	}
	
}
