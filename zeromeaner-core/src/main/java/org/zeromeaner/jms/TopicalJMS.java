package org.zeromeaner.jms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class TopicalJMS {
	public static enum Transport {
		TCP("tcp://%h:%p", ".tcp"),
		UDP("udp://%h:%p", ".udp"),
		;
		
		private final String urlFormat;
		private final String topicSuffix;
		
		private Transport(String urlFormat, String topicSuffix) {
			this.urlFormat = urlFormat;
			this.topicSuffix = topicSuffix;
		}
		
		private String url(String host, int port) {
			return urlFormat.replace("%h", host).replace("%p", "" + port);
		}
		
		private String topicName(String topicBody) {
			return topicBody + topicSuffix;
		}
		
		private String topicBody(String topicName) {
			return topicName.substring(0, topicName.length() - topicSuffix.length());
		}
	}
	
	public static interface TopicalSender {
		public void send(Transport transport, Object object) throws JMSException;
	}
	
	public static interface TopicalReceiver {
		public void receive(Transport transport, String topicBody, Object object);
	}

	private class TransportContext {
		private Transport transport;
		private Connection connection;
		private Session session;
		private Map<String, MessageConsumer> consumers = new ConcurrentHashMap<>();
		private Map<String, MessageProducer> producers = new ConcurrentHashMap<>();
		private Map<String, List<MessageListener>> topicListeners = new ConcurrentHashMap<>();
		
		public TransportContext(Transport transport, Connection connection, Session session) {
			this.transport = transport;
			this.connection = connection;
			this.session = session;
		}
		
		public void subscribe(String topicBody, MessageListener l) throws JMSException {
			String topicName = transport.topicName(topicBody);
			synchronized(topicListeners) {
				if(!topicListeners.containsKey(topicName))
					topicListeners.put(topicName, new CopyOnWriteArrayList<MessageListener>());
			}
			List<MessageListener> ll = topicListeners.get(topicName);
			synchronized(ll) {
				ll.add(0, l);
				ensureDispatch(topicName);
			}
		}
		
		public void unsubscribe(String topicBody, MessageListener l) throws JMSException {
			String topicName = transport.topicName(topicBody);
			synchronized(topicListeners) {
				if(!topicListeners.containsKey(topicName))
					return;
			}
			List<MessageListener> ll = topicListeners.get(topicName);
			synchronized(ll) {
				ll.remove(l);
				if(ll.size() == 0)
					endDispatch(topicName);
			}
		}
		
		public void send(String topicBody, Message message) throws JMSException {
			String topicName = transport.topicName(topicBody);
			ensureProducer(topicName);
			producers.get(topicName).send(message);
		}
		
		public BytesMessage createBytesMessage() throws JMSException {
			return session.createBytesMessage();
		}
		
		public void close() throws JMSException {
			connection.close();
		}
		
		private void ensureDispatch(String topicName) throws JMSException {
			synchronized(consumers) {
				if(consumers.containsKey(topicName))
					return;
				MessageConsumer mc = session.createConsumer(session.createTopic(topicName));
				mc.setMessageListener(new MessageDispatcher(topicName));
				consumers.put(topicName, mc);
			}
		}
		
		private void endDispatch(String topicName) throws JMSException {
			synchronized(consumers) {
				if(!consumers.containsKey(topicName))
					return;
				MessageConsumer mc = consumers.get(topicName);
				mc.close();
				consumers.remove(topicName);
			}
		}
		
		private void ensureProducer(String topicName) throws JMSException {
			synchronized(producers) {
				if(producers.containsKey(topicName))
					return;
				MessageProducer mp = session.createProducer(session.createTopic(topicName));
				producers.put(topicName, mp);
			}
		}
		
		private class MessageDispatcher implements MessageListener {
			private String topicName;
			
			public MessageDispatcher(String topicName) {
				this.topicName = topicName;
			}

			@Override
			public void onMessage(Message message) {
				List<MessageListener> ll = topicListeners.get(topicName);
				if(ll == null)
					return;
				for(MessageListener l : ll)
					l.onMessage(message);
			}
			
		}
	}
	
	private TransportContext[] contexts;
	
	public TopicalJMS(String host, int port) throws JMSException {
		contexts = new TransportContext[Transport.values().length];
		
		for(Transport t : Transport.values()) {
			ConnectionFactory cf = new ActiveMQConnectionFactory(t.url(host, port));
			Connection c = cf.createConnection();
			c.start();
			contexts[t.ordinal()] = new TransportContext(t, c, c.createSession(false, Session.AUTO_ACKNOWLEDGE));
		}
		
	}
	
	private TransportContext context(Transport transport) {
		return contexts[transport.ordinal()];
	}
	
	public void subscribe(Transport transport, String topicBody, MessageListener l) throws JMSException {
		context(transport).subscribe(topicBody, l);
	}
	
	public void unsubscribe(Transport transport, String topicBody, MessageListener l) throws JMSException {
		context(transport).unsubscribe(topicBody, l);
	}
	
	public TopicalSender createSender(final String topicBody, final Kryo kryo) {
		return new TopicalSender() {
			@Override
			public void send(Transport transport, Object object) throws JMSException {
				BytesMessage message = toBytesMessage(transport, kryo, object);
				TopicalJMS.this.send(transport, topicBody, message);
			}
		};
	}
	
	public void subscribe(final String topicBody, final Kryo kryo, final TopicalReceiver receiver) throws JMSException {
		for(final Transport transport : Transport.values()) {
			MessageListener l = new MessageListener() {
				@Override
				public void onMessage(Message message) {
					if(!(message instanceof BytesMessage))
						return;
					try {
						Object object = fromBytesMessage((BytesMessage) message, kryo);
						receiver.receive(transport, topicBody, object);
					} catch(JMSException je) {
						throw new RuntimeException(je);
					}
				}
			};
			subscribe(transport, topicBody, l);
		}
	}
	
	public BytesMessage toBytesMessage(Transport transport, Kryo kryo, Object object) throws JMSException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Output output = new Output(bout);
		kryo.writeClassAndObject(output, object);
		output.flush();
		
		BytesMessage m = context(transport).createBytesMessage();
		m.writeBytes(bout.toByteArray());
		return m;
	}
	
	public Object fromBytesMessage(BytesMessage message, Kryo kryo) throws JMSException {
		byte[] buf = new byte[(int) message.getBodyLength()];
		message.readBytes(buf);
		
		Input input = new Input(buf);
		return kryo.readClassAndObject(input);
	}
	
	public void send(Transport transport, String topicBody, Message message) throws JMSException {
		context(transport).send(topicBody, message);
	}
	
	public void close() throws JMSException {
		for(Transport transport : Transport.values())
			context(transport).close();
	}
}
