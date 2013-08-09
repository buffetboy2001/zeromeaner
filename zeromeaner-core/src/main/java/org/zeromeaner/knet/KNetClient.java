package org.zeromeaner.knet;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.swing.event.EventListenerList;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.zeromeaner.jms.JMSSessionUtil;

import com.esotericsoftware.kryo.Kryo;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetClient {
	protected String type;
	protected String host;
	protected int port;

	protected Kryo kryo;

	protected JMSSessionUtil tcp;
	protected JMSSessionUtil udp;

	protected KNetEventSource source;

	protected EventListenerList listenerList = new EventListenerList();

	public KNetClient(String host, int port) throws JMSException {
		this("Unknown", host, port);
	}

	public KNetClient(String type, String host, int port) throws JMSException {
		this.type = type;
		this.host = host;
		this.port = port;
		kryo = new Kryo();
		KNetKryo.configure(kryo);
	}

	public KNetClient start() throws JMSException {
		Connection c;
		Session s;

		c = new ActiveMQConnectionFactory("tcp://" + host + ":" + port).createConnection();
		c.start();
		s = c.createSession(true, Session.AUTO_ACKNOWLEDGE);
		tcp = new JMSSessionUtil(s, kryo);

		c = new ActiveMQConnectionFactory("udp://" + host + ":" + port).createConnection();
		c.start();
		s = c.createSession(true, Session.AUTO_ACKNOWLEDGE);
		tcp = new JMSSessionUtil(s, kryo);

		return this;
	}

	public void stop() throws JMSException {
		tcp.getSession().close();
		udp.getSession().close();
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
		KNetEvent resp = event(e.getSource().asTopic(), args);
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
			tcp.sendObject(e.getTopic(), e);
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
			udp.sendObject(e.getTopic(), e);
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
}
