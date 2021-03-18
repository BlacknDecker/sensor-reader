package edu.att4sd.controller.mqtt;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import edu.att4sd.model.TelemetryValue;
import edu.att4sd.services.TopicService;

@Component
public class MqttMessageHandler implements MessageHandler{
	
	private Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);
	
	@Autowired
	private TopicService service;

	@Override
	public void handleMessage(Message<?> message) {
		String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
		String value = message.getPayload().toString();
		TelemetryValue received = new TelemetryValue(Instant.now(), value);
		logger.debug("["+topic+"] - "+received.toString());
		service.addTelemetryValue(topic, received);
	}

}
