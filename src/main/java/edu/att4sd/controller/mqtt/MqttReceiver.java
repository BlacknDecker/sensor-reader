package edu.att4sd.controller.mqtt;

import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.stereotype.Component;

@Component
public class MqttReceiver extends MqttPahoMessageDrivenChannelAdapter {
	private static final String DEFAULT_TOPIC = "$dummy";
	
	public MqttReceiver(MqttPahoClientFactory clientFactory, String identifier) {
		super(identifier, clientFactory, DEFAULT_TOPIC);
	}
	
	public String getDefaultTopic() {
		return DEFAULT_TOPIC;
	}

}
