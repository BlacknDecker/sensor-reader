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
import edu.att4sd.exceptions.TopicNotFoundViewException;
import edu.att4sd.model.Topic;
import edu.att4sd.services.TopicService;

@Controller
public class TopicWebController {
	
	@Autowired
	private TopicService topicService;
	
	@Value("${broker:tcp://localhost}")
	private String brokerUrl;

	@GetMapping("/")
	public String index(Model model) {
		List<Topic> allTopics = topicService.getAllTopics();
		model.addAttribute("topics", allTopics);
		model.addAttribute("message", allTopics.isEmpty() ? "No topics" : "");
		model.addAttribute("broker", brokerUrl);
		return "index";
	}
	
	@GetMapping("/new")
	public String newTopic(Model model) {
		model.addAttribute("topic", new Topic());
		return "new";
	}
	
	@PostMapping("/save")
	public String saveTopic(@ModelAttribute("newtopic") TopicDto topicDto, Model model) {
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
		model.addAttribute("topic", topic);
		model.addAttribute("message", topic.getTelemetry().isEmpty() ? "No telemetry available" : "");
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
