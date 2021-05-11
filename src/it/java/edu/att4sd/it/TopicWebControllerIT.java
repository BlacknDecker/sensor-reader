package edu.att4sd.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.att4sd.it.utilities.MessageCounterChannelInterceptor;
import edu.att4sd.model.TelemetryValue;
import edu.att4sd.model.Topic;
import edu.att4sd.repositories.TopicRepository;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TopicWebControllerIT {
	
	@Autowired
	private TopicRepository topicRepository;
	
	@Autowired
	@Qualifier("commandChannel")
	private DirectChannel commandChannel;
	
	@LocalServerPort
	private int port;
	
	private Logger logger = LoggerFactory.getLogger(TopicWebControllerIT.class);
	private static final MessageCounterChannelInterceptor messageCounter = new MessageCounterChannelInterceptor();
	private WebDriver driver;
	private String baseUrl;
	
	@BeforeAll
	void setup() {
		baseUrl = "http://localhost:" + port;
		driver = new HtmlUnitDriver();
		// Add channel interceptor
		commandChannel.addInterceptor(messageCounter);
	}
	
	@AfterAll
	void teardown() {
		driver.quit();
	}
	
	@BeforeEach
	void cleanUp() {
		// always start with an empty database
		topicRepository.deleteAll();
		// Reset message counter
		messageCounter.resetCount();
	}
	

	@Test
	void testHomePage() {
		Topic testTopic = topicRepository.save(createTestTopic("test/path/1", "1.1"));
		
		driver.get(baseUrl);
		
		// table shows the test topic
		assertThat(driver.findElement(By.id("topics_table")).getText()).contains("test/path/1", "Show", "Delete");
		
		// the show link has the correct id in the link
		driver.findElement(By.cssSelector("a[href*='/show/"+testTopic.getId()+"']"));
		
		// the delete link has the correct id in the link
		driver.findElement(By.cssSelector("a[href*='/delete/"+testTopic.getId()+"']"));
	}
	

	@Test
	void testNewTopicPage() throws Exception {
		driver.get(baseUrl + "/new");
		
		driver.findElement(By.name("path")).sendKeys("test/path");
		driver.findElement(By.name("submit_button")).click();
		
		// Check that the new topic has been saved
		assertThat(topicRepository.findByPath("test/path")).isNotEmpty();
		// Check that the new topic has been notified on commandChannel
		assertThat(messageCounter.sendCount.get()).isEqualTo(1);
	}
	
	@Test
	void testNewTopicPageIgnoresDuplicateTopic() throws Exception {
		driver.get(baseUrl + "/new");
		String topicPath = "test/path/1";
		// Save topic
		driver.findElement(By.name("path")).sendKeys(topicPath);
		driver.findElement(By.name("submit_button")).click();
		// Check that the topic has been saved
		assertThat(topicRepository.findByPath(topicPath)).isNotEmpty();
		Topic saved = topicRepository.findByPath(topicPath).get();
		assertThat(messageCounter.sendCount.get()).isEqualTo(1);
		
		// Insert duplicate
		driver.get(baseUrl + "/new");
		driver.findElement(By.name("path")).sendKeys(topicPath);
		driver.findElement(By.name("submit_button")).click();
		// Verify
		assertThat(topicRepository.findAll()).hasSize(1);
		assertThat(topicRepository.findByPath(topicPath).get()).isEqualTo(saved);
		assertThat(messageCounter.sendCount.get()).isEqualTo(1);
	}
	
	
	@Test
	void testShowTopicPage() throws Exception {
		Topic testTopic = topicRepository.save(createTestTopic("test/path/1", "1.1", "1.2"));
		
		driver.get(baseUrl + "/show/" + testTopic.getId());
		
		// Check topic path is displayed
		assertThat(driver.findElement(By.id("topic_path")).getText()).contains("test/path/1");
		// Check telemetry is displayed
		assertThat(driver.findElement(By.id("telemetry_table")).getText())
			.contains("1.1", "1.2");
	}
	
	
	/* Utils */

	private Topic createTestTopic(String path, String... values) {
		Topic testTopic = new Topic(path, new ArrayList<>());
		Arrays.stream(values).forEach(value -> testTopic.getTelemetry()
													.add(new TelemetryValue(getTimestamp(), value)));
		return testTopic;
	}

	private Instant getTimestamp() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}
}
