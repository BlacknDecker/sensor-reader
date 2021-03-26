package edu.att4sd.view;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

import edu.att4sd.controller.web.TopicWebController;
import edu.att4sd.model.TelemetryValue;
import edu.att4sd.model.Topic;
import edu.att4sd.services.TopicService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = TopicWebController.class)
public class TopicWebControllerHtmlTest {
	
	@Autowired
	private WebClient webClient;
	
	@Value("${broker:tcp://localhost}")
	private String brokerUrl;
	
	@MockBean
	private TopicService topicService;
	
	
	@Test
	void testHomePageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/");
		assertThat(page.getTitleText()).isEqualTo("Topics");
	}
	
	@Test
	void testHomePageShowsBrokerUrl() throws Exception {
		HtmlPage page = webClient.getPage("/");
		
		HtmlParagraph brokerInfo = page.getHtmlElementById("broker_info");
		assertThat(brokerInfo.asText()).isEqualTo("Broker: "+brokerUrl);
	}
	
	@Test
	void testHomePageWithNoTopics() throws Exception {
		when(topicService.getAllTopics()).thenReturn(Collections.emptyList());
		
		HtmlPage page = webClient.getPage("/");
		
		assertThat(page.getBody().getTextContent()).contains("No topics");
	}
	
	@Test
	void testHomePageWithTopics() throws Exception {
		when(topicService.getAllTopics())
			.thenReturn(asList(createTestTopic("abcd", "test/path1", ""),
							   createTestTopic("efgh", "test/path2", "1.0", "2.3")));
		
		HtmlPage page = webClient.getPage("/");
		
		assertThat(page.getBody().getTextContent()).doesNotContain("No topics");
		HtmlTable table = page.getHtmlElementById("topics_table");
		assertThat(table.asText())
			.isEqualTo("Topics\n" +
					   "test/path1\n" +
					   "test/path2");
	}
	

	
	/* Utils */

	private Topic createTestTopic(String id, String path, String... values) {
		Topic testTopic = new Topic(path, new ArrayList<>());
		testTopic.setId(id);
		Arrays.stream(values).forEach(value -> testTopic.getTelemetry()
													.add(new TelemetryValue(getTimestamp(), value)));
		return testTopic;
	}

	private Instant getTimestamp() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}
	
	
}
