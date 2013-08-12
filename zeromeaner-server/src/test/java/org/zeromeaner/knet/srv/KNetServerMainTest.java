package org.zeromeaner.knet.srv;

import org.slf4j.impl.SimpleLogger;

public class KNetServerMainTest {
	public static void main(String[] args) {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		KNetServerMain.main(args);
	}
}
