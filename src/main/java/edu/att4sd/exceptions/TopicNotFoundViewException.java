package edu.att4sd.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Topic not found")
public class TopicNotFoundViewException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
}
