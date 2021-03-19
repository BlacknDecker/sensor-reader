package edu.att4sd.controller.mqtt;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import edu.att4sd.model.TelemetryValue;
import edu.att4sd.services.TopicService;

@Component
public class MqttMessageHandler implements MessageHandler{

	@Autowired
	private TopicService service;
	
	private Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);
	private static final String TOPIC_HEADER = MqttHeaders.RECEIVED_TOPIC; 

	@Override
	public void handleMessage(Message<?> message) {
		Optional<TelemetryValue> telemetryOpt = createTelemetryValue(message);
		if(telemetryOpt.isPresent()) {
			String topic = message.getHeaders().get(TOPIC_HEADER).toString();
			TelemetryValue telemetryValue = telemetryOpt.get(); 
			logger.debug("["+topic+"] - "+telemetryValue.toString());
			service.addTelemetryValue(topic, telemetryValue);
		}
	}
	
	private Optional<TelemetryValue> createTelemetryValue(Message<?> message){
		Optional<TelemetryValue> telemetryOpt = Optional.empty(); 
		if(message.getHeaders().containsKey(TOPIC_HEADER)) {
			String value = message.getPayload().toString();			
			TelemetryValue received = new TelemetryValue(Instant.now(), value);
			telemetryOpt = Optional.of(received);
		}
		return telemetryOpt;
	}

}
