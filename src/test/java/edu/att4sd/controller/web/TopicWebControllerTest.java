package edu.att4sd.controller.web;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import edu.att4sd.model.Topic;
import edu.att4sd.services.TopicService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = TopicWebController.class)
class TopicWebControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private TopicService topicService;

	@Test
	public void testStatus200() throws Exception {
		mvc.perform(get("/")).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	void testIndexViewShowsTopics() throws Exception {
		List<Topic> topics = asList(new Topic("test/path", new ArrayList<>()));
		
		when(topicService.getAllTopics()).thenReturn(topics);
		
		mvc.perform(get("/"))
			.andExpect(view().name("index"))
			.andExpect(model().attribute("topics", topics));
	}

}
