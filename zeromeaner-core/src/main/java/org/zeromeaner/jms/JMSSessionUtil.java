package org.zeromeaner.jms;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class JMSSessionUtil {
	protected Session session;
	protected Kryo kryo;
	
	protected Map<String, MessageConsumer> consumers = new ConcurrentHashMap<>();
	protected Map<String, MessageProducer> producers = new ConcurrentHashMap<>();
	
	public JMSSessionUtil(Session session, Kryo kryo) {
		this.session = session;
		this.kryo = kryo;
	}
	
	public Session getSession() {
		return session;
	}
	
	protected MessageConsumer consumerFor(String topicName) throws JMSException {
		synchronized(consumers) {
			Topic topic = session.createTopic(topicName);
			if(!consumers.containsKey(topicName))
				consumers.put(topicName, session.createConsumer(topic));
			return consumers.get(topicName);
		}
	}

	protected MessageProducer producerFor(String topicName) throws JMSException {
		synchronized(producers) {
			Topic topic = session.createTopic(topicName);
			if(!producers.containsKey(topicName))
				producers.put(topicName, session.createProducer(topic));
			return producers.get(topicName);
		}
	}
	
	public void sendMessage(String topicName, Message message) throws JMSException {
		MessageProducer producer = producerFor(topicName);
		producer.send(message);
	}
	
	public Message receiveMessage(String topicName) throws JMSException {
		MessageConsumer consumer = consumerFor(topicName);
		return consumer.receive();
	}
	
	public void sendObject(String topicName, Object object) throws JMSException {
		sendMessage(topicName, toJMS(object));
	}
	
	public Object receiveObject(String topicName) throws JMSException {
		return fromJMS((BytesMessage) receiveMessage(topicName));
	}
	
	public BytesMessage toJMS(Object obj) throws JMSException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Output output = new Output(bout);
		kryo.writeClassAndObject(output, obj);
		output.flush();
		
		BytesMessage m = session.createBytesMessage();
		m.writeBytes(bout.toByteArray());
		return m;
	}
	
	public Object fromJMS(BytesMessage m) throws JMSException {
		byte[] buf = new byte[(int) m.getBodyLength()];
		m.readBytes(buf);
		
		Input input = new Input(buf);
		return kryo.readClassAndObject(input);
	}
}
