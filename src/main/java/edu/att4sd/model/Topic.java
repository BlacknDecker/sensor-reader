package edu.att4sd.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "topic")
public class Topic {

	@Id
	private String id;
	private String path;
	private List<TelemetryValue> telemetry;

	public Topic(String path, List<TelemetryValue> telemetry) {
		this.path = path;
		this.telemetry = telemetry;
	}

	public String getId() {
		return id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<TelemetryValue> getTelemetry() {
		return telemetry;
	}

	public void setTelemetry(List<TelemetryValue> telemetry) {
		this.telemetry = telemetry;
	}

	@Override
	public String toString() {
		return "[" + path + "]:{" +
				telemetry.stream().map(Object::toString).collect(Collectors.joining(", ")) + "}";
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, telemetry);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Topic other = (Topic) obj;
		return Objects.equals(path, other.path) &&
				Objects.equals(telemetry, other.telemetry);
	}

}