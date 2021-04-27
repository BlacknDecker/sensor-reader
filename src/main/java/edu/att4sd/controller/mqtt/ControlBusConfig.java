package edu.att4sd.controller.mqtt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.ExpressionControlBusFactoryBean;

@Configuration
@EnableIntegration
public class ControlBusConfig {
				
	@Bean
	@ServiceActivator(inputChannel = "commandChannel")
	public ExpressionControlBusFactoryBean controlBus() {
	    return new ExpressionControlBusFactoryBean();
	}
	
}
