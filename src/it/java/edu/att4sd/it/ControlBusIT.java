package edu.att4sd.it;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.Arrays;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.att4sd.controller.mqtt.MqttReceiver;
import edu.att4sd.repositories.TopicRepository;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ControlBusIT {

	@Autowired
	private TopicRepository topicRepository;
	
	@Autowired
	private MqttReceiver mqttReceiver;
		
	@LocalServerPort
	private int port;
	
	private static final String TEST_TOPIC_PATH = "/test/path";
	private Logger logger = LoggerFactory.getLogger(ControlBusIT.class);
	private WebDriver driver;
	private String baseUrl;
	
	@BeforeAll
	void setup() {
		baseUrl = "http://localhost:" + port;
		driver = new HtmlUnitDriver();
	}
	
	@AfterAll
	void teardown() {
		driver.quit();
	}
	
	@BeforeEach
	void cleanUp() {
		// always start with an empty database
		topicRepository.deleteAll();
		// check subscribed topics
		if(mqttReceiver.getTopic().length > 1) {
			String defaultTopic = mqttReceiver.getDefaultTopic();
			Arrays.stream(mqttReceiver.getTopic())
				  .filter((topicPath) -> topicPath!=defaultTopic)
				  .forEach((topicPath) -> mqttReceiver.removeTopic(topicPath));
		}
		assertThat(mqttReceiver.getTopic()).containsExactly(mqttReceiver.getDefaultTopic());
	}
	
	@Test
	void testWhenNewTopicIsAddedFromWebpageMqttReceiverShouldSubscribe() {
		// Add new topic from webpage
		driver.get(baseUrl + "/new");
		driver.findElement(By.name("path")).sendKeys(TEST_TOPIC_PATH);
		driver.findElement(By.name("submit_button")).click();
		
		// Verify the receiver is now subscribed to that topic
		await().atMost(3, SECONDS).until(mqttReceiverHasSubscribedToATopic());
		assertThat(Arrays.stream(mqttReceiver.getTopic()))
			.containsExactly(mqttReceiver.getDefaultTopic(), 
							 TEST_TOPIC_PATH);		
		// Cleanup subscriptions
		mqttReceiver.removeTopic(TEST_TOPIC_PATH);
	}
	
	@Test
	void testDuplicateTopic() {
		// Add new topic from webpage
		driver.get(baseUrl + "/new");
		driver.findElement(By.name("path")).sendKeys(TEST_TOPIC_PATH);
		driver.findElement(By.name("submit_button")).click();
				
		// Add duplicate topic
		driver.get(baseUrl + "/new");
		driver.findElement(By.name("path")).sendKeys(TEST_TOPIC_PATH);
		driver.findElement(By.name("submit_button")).click();
		
		// Verify the receiver is now subscribed to that topic
		await().atMost(3, SECONDS).until(mqttReceiverHasSubscribedToATopic());
		assertThat(Arrays.stream(mqttReceiver.getTopic()))
			.containsExactly(mqttReceiver.getDefaultTopic(), 
							 TEST_TOPIC_PATH);		
		// Cleanup subscriptions
		mqttReceiver.removeTopic(TEST_TOPIC_PATH);
	}
	
	private Callable<Boolean> mqttReceiverHasSubscribedToATopic(){
		return () -> mqttReceiver.getTopic().length > 1; // Default is always present
	}
	
}
