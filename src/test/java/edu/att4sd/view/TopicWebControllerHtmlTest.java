package edu.att4sd.view;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

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
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

import edu.att4sd.controller.web.TopicWebController;
import edu.att4sd.model.TelemetryValue;
import edu.att4sd.model.Topic;
import edu.att4sd.services.TopicService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = TopicWebController.class)
class TopicWebControllerHtmlTest {
	
	@Autowired
	private WebClient webClient;
	
	@Value("${broker:tcp://localhost}")
	private String brokerUrl;
	
	@MockBean
	private TopicService topicService;
	
	@MockBean(name="commandChannel")
	private MessageChannel commandChannel;
	
	
	/* HOMEPAGE */
	
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
					   "test/path1	Show	Delete\n" +
					   "test/path2	Show	Delete"
					   );
		page.getAnchorByHref("/edit/abcd");
		page.getAnchorByHref("/delete/abcd");
		page.getAnchorByHref("/edit/efgh");
		page.getAnchorByHref("/delete/efgh");
	}
	
	@Test
	void testHomePageLinkToCreateNewTopic() throws Exception {
		HtmlPage page = webClient.getPage("/");
		
		assertThat(page.getAnchorByText("Add Topic").getHrefAttribute()).isEqualTo("/new");
	}
	
	
	/* NEW_TOPIC PAGE */
	
	
	@Test
	void testNewTopicPage() throws Exception {
		String newTopicPath = "test/path";
		Topic saved = createTestTopic("testId", newTopicPath);
		when(topicService.insertNewTopic(new Topic(newTopicPath, new ArrayList<>())))
			.thenReturn(saved);
		
		HtmlPage page = webClient.getPage("/new");
		// Get the form	
		final HtmlForm form = page.getFormByName("topic_form");
		assertThat(form.getInputByName("path").asText()).isEmpty();
		// Insert new topic
		form.getInputByName("path").setValueAttribute(newTopicPath);
		form.getButtonByName("submit_button").click();
		// Verify
		verify(topicService).insertNewTopic(new Topic(newTopicPath, new ArrayList<>()));
	}
	
	@Test
	void testNewTopicPageLinkToHomePage() throws Exception {
		HtmlPage page = webClient.getPage("/new");
		
		assertThat(page.getAnchorByText("Cancel").getHrefAttribute()).isEqualTo("/");
	}

	
	/* SHOW_TOPIC PAGE */
	
	
	@Test
	void testShowTopicPageShowsTopicPath() throws Exception {
		Topic toShow = createTestTopic("testId", "test/path");
		when(topicService.getTopicById("testId"))
			.thenReturn(toShow);
		
		HtmlPage page = webClient.getPage("/show/testId");
		
		HtmlParagraph topicPath = page.getHtmlElementById("topic_path");
		assertThat(topicPath.asText()).isEqualTo("Path: test/path");	
	}
	
	@Test
	void testShowTopicPageWhenNoTelemetryAvailable() throws Exception {
		Topic toShow = createTestTopic("testId", "test/path");
		when(topicService.getTopicById("testId"))
			.thenReturn(toShow);
		
		HtmlPage page = webClient.getPage("/show/testId");
		
		assertThat(page.getBody().getTextContent()).contains("No telemetry available");
	}
	
	@Test
	void testShowTopicPageWhenTelemetryAvailable() throws Exception {
		Topic toShow = createTestTopic("testId", "test/path", "1.1", "2.3");
		when(topicService.getTopicById("testId"))
			.thenReturn(toShow);
		
		HtmlPage page = webClient.getPage("/show/testId");
		
		HtmlTable telemetryTable = page.getHtmlElementById("telemetry_table");
		assertThat(telemetryTable.asText())
			.isEqualTo("Timestamp	Value\n" +
					   toShow.getTelemetry().get(0).getTimestamp() + "	1.1\n"+
					   toShow.getTelemetry().get(1).getTimestamp() + "	2.3");
	}
	
	@Test
	void testShowTopicPageLinkToHomePage() throws Exception {
		Topic toShow = createTestTopic("testId", "test/path");
		when(topicService.getTopicById("testId"))
			.thenReturn(toShow);
		
		HtmlPage page = webClient.getPage("/show/testId");
		
		assertThat(page.getAnchorByText("Home").getHrefAttribute()).isEqualTo("/");
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
