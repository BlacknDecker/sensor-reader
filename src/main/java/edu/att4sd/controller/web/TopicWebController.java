package edu.att4sd.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import edu.att4sd.dto.TopicDto;
import edu.att4sd.exceptions.EmptyTopicPathException;
import edu.att4sd.exceptions.TopicNotFoundViewException;
import edu.att4sd.model.Topic;
import edu.att4sd.services.TopicService;

@Controller
public class TopicWebController {
	
	private static final String BROKER_ATTRIBUTE = "broker";
	private static final String MESSAGE_ATTRIBUTE = "message";
	private static final String TOPICS_ATTRIBUTE = "topics";
	private static final String TOPIC_ATTRIBUTE = "topic";
	private static final String TOPIC_PATH_ATTRIBUTE = "topicPath";

	@Autowired
	private TopicService topicService;
	
	@Value("${broker:tcp://localhost}")
	private String brokerUrl;

	@GetMapping("/")
	public String index(Model model) {
		List<Topic> allTopics = topicService.getAllTopics();
		model.addAttribute(TOPICS_ATTRIBUTE, allTopics);
		model.addAttribute(MESSAGE_ATTRIBUTE, allTopics.isEmpty() ? "No topics" : "");
		model.addAttribute(BROKER_ATTRIBUTE, brokerUrl);
		return "index";
	}
	
	@GetMapping("/new")
	public String newTopic(Model model) {
		model.addAttribute(TOPIC_PATH_ATTRIBUTE, new TopicDto());
		return "new";
	}
	
	@PostMapping("/save")
	public String saveTopic(@ModelAttribute(TOPIC_PATH_ATTRIBUTE) TopicDto topicDto, Model model) {
		if(topicDto.getPath().isEmpty()) {
			throw new EmptyTopicPathException();
		}
		Topic newTopic = dtoToTopic(topicDto);
		topicService.insertNewTopic(newTopic);
		return "redirect:/";
	}
		
	@GetMapping("/show/{id}")
	public String showTopic(@PathVariable String id, Model model) {
		Topic topic = topicService.getTopicById(id);
		if(topic == null) {
			throw new TopicNotFoundViewException();
		}
		model.addAttribute(TOPIC_ATTRIBUTE, topic);
		model.addAttribute(MESSAGE_ATTRIBUTE, topic.getTelemetry().isEmpty() ? "No telemetry available" : "");
		return "show";
	}
	
	@GetMapping("/delete/{id}")
	public String deleteTopic(@PathVariable String id, Model model) {
		if(topicService.deleteTopicById(id)) {
			return "redirect:/";
		}
		throw new TopicNotFoundViewException(); 
	}

	private Topic dtoToTopic(TopicDto topicDto) {
		return new Topic(topicDto.getPath(), new ArrayList<>());
	}

}
