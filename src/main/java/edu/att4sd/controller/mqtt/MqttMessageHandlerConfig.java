package edu.att4sd.controller.mqtt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageHandler;

@Configuration
@EnableIntegration
public class MqttMessageHandlerConfig {
	
	@Bean
    @ServiceActivator(inputChannel = "mqttReceiverOutputChannel")
    public MessageHandler mqttMessageHandler() {
		return new MqttMessageHandler();
	}
}
