package edu.att4sd.controller.web;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import edu.att4sd.dto.TopicDto;
import edu.att4sd.model.TelemetryValue;
import edu.att4sd.model.Topic;
import edu.att4sd.services.TopicService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = TopicWebController.class)
class TopicWebControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private TopicService topicService;
	
	@Value("${broker:tcp://localhost}")
	private String brokerUrl;

	private static final String MESSAGE_ATTRIBUTE = "message";
	private static final String TOPICS_ATTRIBUTE = "topics";
	private static final String TOPIC_ATTRIBUTE = "topic";
	private static final String TOPIC_PATH_ATTRIBUTE = "topicPath";

	private static final String TOPIC_ID = "qwerty";
	private static final String TOPIC_PATH = "test/path";

	
	@Test
	void testStatus200() throws Exception {
		mvc.perform(get("/")).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	void testIndexViewShowsTopics() throws Exception {
		List<Topic> topics = asList(new Topic(TOPIC_PATH, new ArrayList<>()));
		
		when(topicService.getAllTopics()).thenReturn(topics);
		
		mvc.perform(get("/"))
			.andExpect(view().name("index"))
			.andExpect(model().attribute(TOPICS_ATTRIBUTE, topics))
			.andExpect(model().attribute(MESSAGE_ATTRIBUTE, ""));
	}
	
	@Test
	void testIndexViewShowsMessageWhenThereAreNoTopics() throws Exception {
		when(topicService.getAllTopics()).thenReturn(Collections.emptyList());
		
		mvc.perform(get("/"))
			.andExpect(view().name("index"))
			.andExpect(model().attribute(TOPICS_ATTRIBUTE, Collections.emptyList()))
			.andExpect(model().attribute(MESSAGE_ATTRIBUTE, "No topics"));
	}
	
	@Test
	void testIndexViewShowsBroker() throws Exception {
		mvc.perform(get("/"))
		.andExpect(model().attribute("broker", brokerUrl));
	}
	
	@Test
	void testNewTopicView() throws Exception {
		mvc.perform(get("/new"))
			.andExpect(view().name("new"))
			.andExpect(model().attribute(TOPIC_PATH_ATTRIBUTE, any(TopicDto.class)));
		verifyNoInteractions(topicService);
	}
	
	@Test
	void testSaveNewTopic() throws Exception {
		mvc.perform(post("/save")
						.param("path", TOPIC_PATH))
			.andExpect(view().name("redirect:/"));
		
		verify(topicService).insertNewTopic(new Topic(TOPIC_PATH, new ArrayList<>()));
	}
	
	@Test
	void testSaveNewTopicWhenPathIsEmptyShouldThrow() throws Exception {
		mvc.perform(post("/save")
				.param("path", ""))
			.andExpect(status().is(400));
		
	}
	
	@Test
	void testShowTopicViewWhenTopicHasTelemetry() throws Exception {
		Topic testTopic = createTestTopic(TOPIC_ID, TOPIC_PATH, "1.0", "1.2", "0.3");
		when(topicService.getTopicById(TOPIC_ID)).thenReturn(testTopic);
		
		mvc.perform(get("/show/qwerty"))
			.andExpect(view().name("show"))
			.andExpect(model().attribute(TOPIC_ATTRIBUTE, testTopic))
			.andExpect(model().attribute(MESSAGE_ATTRIBUTE, ""));
	}
	
	@Test
	void testShowTopicViewWhenTopicHasNoTelemetry() throws Exception {
		Topic testTopic = createTestTopic(TOPIC_ID, TOPIC_PATH);
		when(topicService.getTopicById(TOPIC_ID)).thenReturn(testTopic);
		
		mvc.perform(get("/show/qwerty"))
			.andExpect(view().name("show"))
			.andExpect(model().attribute(TOPIC_ATTRIBUTE, testTopic))
			.andExpect(model().attribute(MESSAGE_ATTRIBUTE, "No telemetry available"));
	}
	
	@Test
	void testShowTopicViewWhenTopicNotFoundShouldThrow404() throws Exception {
		when(topicService.getTopicById(TOPIC_ID)).thenReturn(null);
		
		mvc.perform(get("/show/qwerty"))
			.andExpect(status().is(404));
	}
	
	@Test
	void testDeleteTopicView() throws Exception {
		when(topicService.deleteTopicById(TOPIC_ID)).thenReturn(true);
		
		mvc.perform(get("/delete/qwerty"))
			.andExpect(view().name("redirect:/"));
	}
	
	@Test
	void testDeleteTopicViewWhenTopicNotFoundShouldThrow404() throws Exception {
		when(topicService.deleteTopicById(TOPIC_ID)).thenReturn(false);
		
		mvc.perform(get("/delete/qwerty"))
			.andExpect(status().is(404));
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
