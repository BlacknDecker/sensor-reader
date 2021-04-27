package edu.att4sd.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Topic path is empty")
public class EmptyTopicPathException extends RuntimeException {

	private static final long serialVersionUID = 1L;

}
