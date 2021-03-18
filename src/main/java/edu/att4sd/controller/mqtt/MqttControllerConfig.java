package edu.att4sd.controller.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableIntegration
public class MqttControllerConfig {

	@Value("${broker}")
	private String brokerUrl;

	@Bean
	public MessageChannel mqttControllerOutputChannel() {
		return new DirectChannel();
	}

	@Bean
	public MqttConnectOptions getReceiverMqttConnectOptions() {
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		mqttConnectOptions.setCleanSession(true);
		mqttConnectOptions.setConnectionTimeout(10);
		mqttConnectOptions.setKeepAliveInterval(90);
		mqttConnectOptions.setAutomaticReconnect(true);
		mqttConnectOptions.setServerURIs(new String[] { brokerUrl });
		return mqttConnectOptions;
	}

	@Bean
	public MqttPahoClientFactory mqttClientFactory() {
		DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
		factory.setConnectionOptions(getReceiverMqttConnectOptions());
		return factory;
	}
	
    @Bean
    public MessageProducer mqttReceiver(MessageChannel mqttControllerOutputChannel) {
    	MqttController receiver = new MqttController(mqttClientFactory(), "TelemetryReceiver");
        receiver.setConverter(new DefaultPahoMessageConverter());
        receiver.setOutputChannel(mqttControllerOutputChannel);
        receiver.setAutoStartup(false);
        return receiver;
    }
    
}
