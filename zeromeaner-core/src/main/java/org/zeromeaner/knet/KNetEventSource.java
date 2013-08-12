package org.zeromeaner.knet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Objects;

public class KNetEventSource implements KryoSerializable {
	protected String topic;
	protected String type;
	protected String name;
	
	@Deprecated
	public KNetEventSource() {}
	
	public KNetEventSource(String topic) {
		this.topic = topic;
	}
	
	public void updateFrom(KNetEventSource source) {
		this.type = source.getType();
		this.name = source.getName();
	}
	
	public String getTopic() {
		return topic;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "(" + topic + "," + type + "," + name + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof KNetEventSource) {
			return Objects.equal(topic, ((KNetEventSource) obj).topic);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return topic.hashCode();
	}
	
	public KNetEvent event(String topic, Object... args) {
		return new KNetEvent(this, topic, args);
	}
	
	@Override
	public void write(Kryo kryo, Output output) {
		output.writeString(topic);
		output.writeString(type);
		output.writeString(name);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		topic = input.readString();
		type = input.readString();
		name = input.readString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
