package org.zeromeaner.knet.srv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventArgs;
import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.KNetKryo;

import com.esotericsoftware.kryo.Kryo;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetServer {
	private static final Logger log = Logger.getLogger(KNetServer.class);
	
	public static final int DEFAULT_PORT = 61897;
	
	protected int port;
	
	
	protected Map<Integer, KNetEventSource> sourcesByConnectionId = new HashMap<Integer, KNetEventSource>();
	protected Map<KNetEventSource, Integer> connectionIds = new HashMap<KNetEventSource, Integer>();
//	protected Map<Integer, ExecutorService> senders = new HashMap<Integer, ExecutorService>();
	
	protected KNetEventSource source;

	protected KNetChannelManager chanman;
	protected KNetUserManager uman;
	
	public KNetServer(int port) throws JMSException {
		this.port = port;
		Kryo kryo = new Kryo();
		KNetKryo.configure(kryo);
		chanman = new KNetChannelManager(port);
		chanman.start();
		uman = new KNetUserManager(port);
		uman.start();
	}
	
	public void stop() {
		chanman.stop();
		uman.stop();
	}
	
	public int getPort() {
		return port;
	}
}
