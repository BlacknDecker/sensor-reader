package edu.att4sd.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.WebDriverManager;

public class TopicWebControllerE2E {
	private static int port = Integer.parseInt(System.getProperty("server.port", "8080"));
	private static String baseUrl = "http://localhost:" + port;
	private Logger logger = LoggerFactory.getLogger(TopicWebControllerE2E.class);
	private WebDriver driver;

	@BeforeAll
	public static void setupClass() {
		// setup Chrome Driver
		WebDriverManager.chromedriver().setup();
	}

	@BeforeEach
	public void setup() {
		baseUrl = "http://localhost:" + port;
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

	@AfterEach
	public void teardown() {
		driver.quit();
	}

	@Test
	public void testHomePage() {
		driver.get(baseUrl);
		// the "New Topic" link is present with href containing /new
		driver.findElement(By.cssSelector("a[href*='/new"));
	}
	
	@Test
	void testCreateNewTopic() {
		String topicPath = "test/path/newtopic/1";
		driver.get(baseUrl);
		// Click on New Topic
		driver.findElement(By.cssSelector("a[href*='/new")).click();
		// Fill the form
		driver.findElement(By.name("path")).sendKeys(topicPath);
		// Click save
		driver.findElement(By.name("submit_button")).click();
		// Check that in the home page (where we are redirected after saving) is displayed the topic
		assertThat(driver.findElement(By.id("topics_table")).getText()).contains(topicPath);
	}
	
	@Test
	void testShowTopic() {
		String topicPath = "test/path/newtopic/2";
		driver.get(baseUrl);
		// Create new Topic
		driver.findElement(By.cssSelector("a[href*='/new")).click();
		driver.findElement(By.name("path")).sendKeys(topicPath);
		driver.findElement(By.name("submit_button")).click();

		// Find the row with the topic in the homepage table
		List<WebElement> tableRows = driver.findElement(By.id("topics_table")).findElements(By.tagName("tr"));
		Optional<WebElement> topicRow = tableRows.stream()
													.filter(row -> row.getText().contains(topicPath))
													.findFirst();
		assertThat(topicRow).isPresent();
		// Go to the show page
		topicRow.get().findElement(By.cssSelector("a[href*='/show/'")).click();
		// Verify
		assertThat(driver.findElement(By.id("topic_path")).getText().contains(topicPath));
	}
	
	@Test
	void testDeleteTopic() {
		String topicPath = "test/path/newtopic/3";
		driver.get(baseUrl);
		// Create new Topic
		driver.findElement(By.cssSelector("a[href*='/new")).click();
		driver.findElement(By.name("path")).sendKeys(topicPath);
		driver.findElement(By.name("submit_button")).click();

		// Find the row with the topic in the homepage table
		List<WebElement> tableRows = driver.findElement(By.id("topics_table")).findElements(By.tagName("tr"));
		Optional<WebElement> topicRow = tableRows.stream()
													.filter(row -> row.getText().contains(topicPath))
													.findFirst();
		assertThat(topicRow).isPresent();
		// Click delete
		topicRow.get().findElement(By.cssSelector("a[href*='/delete/'")).click();
		
		// Verify topic is no more in the homepage table
		assertThat(driver.findElement(By.id("topics_table")).getText()).doesNotContain(topicPath);
	}
	
	@Test
	void testAbortNewTopicCreation() {
		String topicPath = "test/path/newtopic/4";
		driver.get(baseUrl);
		// Create new Topic
		driver.findElement(By.cssSelector("a[href*='/new")).click();
		driver.findElement(By.name("path")).sendKeys(topicPath);
		// Click cancel
		driver.findElement(By.id("cancel_button")).click();
		
		// Verify that we are redirected on the homepage and the topic is not in the table
		assertThat(driver.findElement(By.id("topics_table")).getText()).doesNotContain(topicPath);	
	}
}
