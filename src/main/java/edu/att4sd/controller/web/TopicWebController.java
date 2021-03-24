package edu.att4sd.controller.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

}
