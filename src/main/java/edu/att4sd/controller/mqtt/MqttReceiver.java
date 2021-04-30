package edu.att4sd.controller.mqtt;

import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
public class MqttReceiver extends MqttPahoMessageDrivenChannelAdapter {
	private static final String DEFAULT_TOPIC = "$dummy";
	
	public MqttReceiver(MqttPahoClientFactory clientFactory, String identifier) {
		super(identifier, clientFactory, DEFAULT_TOPIC);
	}
	
	// Add a topic eventually ignores duplicates
	@ManagedOperation
	public void addTopicIgnoreDuplicates(String topic, int qos) {
		try {
			super.addTopic(topic, qos);			
		} catch (MessagingException e) {
			super.removeTopic(topic);
			super.addTopic(topic, qos);		//To update qos
		}
	}
	
	public String getDefaultTopic() {
		return DEFAULT_TOPIC;
	}
	
}
