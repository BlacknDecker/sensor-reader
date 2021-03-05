package edu.att4sd.model;

import java.time.Instant;
import java.util.Objects;

public class TelemetryValue {
	
	private Instant timestamp;
	private String value;

	public TelemetryValue(Instant timestamp, String value) {
		this.timestamp = timestamp;
		this.value = value;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return timestamp.toString() + " - " + value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(timestamp, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TelemetryValue other = (TelemetryValue) obj;
		return Objects.equals(timestamp, other.timestamp) && 
				Objects.equals(value, other.value);
	}
}
