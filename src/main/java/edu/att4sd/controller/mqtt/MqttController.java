package edu.att4sd.controller.mqtt;

import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.stereotype.Component;

@Component
public class MqttController extends MqttPahoMessageDrivenChannelAdapter {
	private static final String DUMMY_TOPIC = "dummy";
	
	public MqttController(MqttPahoClientFactory clientFactory, String identifier) {
		super(identifier, clientFactory, DUMMY_TOPIC);
		this.removeTopic(DUMMY_TOPIC);
	}

}
