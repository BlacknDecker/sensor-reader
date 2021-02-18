package edu.att4sd.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DummyWebController {
	
	@RequestMapping("/")
	public String indexpage() {
		return "index";
	}

}
