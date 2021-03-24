package edu.att4sd.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import edu.att4sd.services.TopicService;

@Controller
public class TopicWebController {
	
	@Autowired
	private TopicService topicService;

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("topics", topicService.getAllTopics());
		return "index";
	}

}
