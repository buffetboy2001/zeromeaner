package org.zeromeaner.knet;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.swing.event.EventListenerList;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.zeromeaner.jms.TopicalJMS;
import org.zeromeaner.jms.TopicalJMS.TopicalReceiver;
import org.zeromeaner.jms.TopicalJMS.Transport;
import org.zeromeaner.jms.Topics;

import com.esotericsoftware.kryo.Kryo;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetClient implements TopicalReceiver {
	protected final String type;
	protected String host;
	protected int port;
	protected String prefix;

	protected Kryo kryo;

	protected TopicalJMS jms;

	protected KNetEventSource source;

	protected EventListenerList listenerList = new EventListenerList();

	public KNetClient(String host, int port, String prefix) {
		this("Unknown", host, port, prefix);
	}

	public KNetClient(String type, String host, int port, String prefix) {
		this.type = type;
		this.host = host;
		this.port = port;
		this.prefix = prefix;
		kryo = new Kryo();
		KNetKryo.configure(kryo);
	}

	public KNetClient start() throws JMSException {
		jms = new TopicalJMS(host, port, prefix);
		source = new KNetEventSource(Topics.CLIENTS + "." + type + "." + UUID.randomUUID());
		
		jms.subscribe(source.getTopic(), kryo, this);
		
		fireTCP(Topics.CLIENTS, CONNECTED);

		return this;
	}

	public void stop() {
		try {
			jms.close();
		} catch(JMSException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected void issue(KNetEvent e) {
		try {
			Object[] ll = listenerList.getListenerList();
			for(int i = ll.length - 2; i >= 0; i -= 2) {
				if(ll[i] == KNetListener.class) {
					((KNetListener) ll[i+1]).knetEvented(this, e);
				}
			}
		} catch(RuntimeException re) {
			re.printStackTrace();
			throw re;
		} catch(Error er) {
			er.printStackTrace();
			throw er;
		}
	}

	protected KNetEvent process(KNetEvent e) {
		return e;
	}

	public KNetEventSource getSource() {
		return source;
	}

	public KNetEvent event(String topic, Object... args) {
		return getSource().event(topic, args);
	}

	public void addKNetListener(KNetListener l) {
		listenerList.add(KNetListener.class, l);
	}

	public void removeKNetListener(KNetListener l) {
		listenerList.remove(KNetListener.class, l);
	}

	public boolean isExternal(KNetEvent e) {
		return !getSource().equals(e.getSource());
	}

	public boolean isLocal(KNetEvent e) {
		return getSource().equals(e.getSource());
	}

	public boolean isMine(KNetEvent e) {
		return !isLocal(e) && !e.is(ADDRESS) || getSource().equals(e.get(ADDRESS));
	}

	public void reply(KNetEvent e, Object... args) {
		KNetEvent resp = event(e.getSource().getTopic(), args);
		resp.set(ADDRESS, e.getSource());
		resp.set(IN_REPLY_TO, e);
		fire(resp);
	}

	public void fire(String topic, Object... args) {
		fire(event(topic, args));
	}

	public void fire(KNetEvent e) {
		if(e.is(UDP))
			fireUDP(e);
		else
			fireTCP(e);
	}

	public void fireTCP(String topic, Object... args) {
		System.err.println(Arrays.asList(args));
		fireTCP(event(topic, args));
	}

	public void fireTCP(KNetEvent e) {
		e = process(e);
		e.getArgs().remove(UDP);
		issue(e);

		try {
//			tcp.sendObject(e.getTopic(), e);
			jms.createSender(e.getTopic(), kryo).send(Transport.TCP, e);
		} catch(JMSException je) {
			throw new RuntimeException(je);
		}
	}

	public void fireUDP(String topic, Object... args) {
		fireUDP(event(topic, args));
	}

	public void fireUDP(KNetEvent e) {
		e = process(e);
		e.getArgs().put(UDP, true);
		issue(e);

		try {
//			udp.sendObject(e.getTopic(), e);
			jms.createSender(e.getTopic(), kryo).send(Transport.UDP, e);
		} catch(JMSException je) {
			throw new RuntimeException(je);
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@Override
	public void receive(Transport transport, String topicBody, Object object) {
		if(object instanceof KNetEvent) {
			issue((KNetEvent) object);
		}
	}
}
