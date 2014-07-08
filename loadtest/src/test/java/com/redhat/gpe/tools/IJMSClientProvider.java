package com.redhat.gpe.tools;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;

public interface IJMSClientProvider {
	
    Connection getConnection() throws JMSException;
    Queue getQueue(String name) throws JMSException;
	
}
