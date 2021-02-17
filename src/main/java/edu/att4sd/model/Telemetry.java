package edu.att4sd.model;

import java.util.Objects;

public class Telemetry {
	private String topic;
	private String value;

	public Telemetry(String topic, String value) {
		this.topic = topic;
		this.value = value;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "[" + topic + "] - " + value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(topic, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Telemetry other = (Telemetry) obj;
		return Objects.equals(topic, other.topic) && Objects.equals(value, other.value);
	}

}
