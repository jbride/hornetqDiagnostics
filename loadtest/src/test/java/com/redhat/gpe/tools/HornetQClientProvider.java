package com.redhat.gpe.tools;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.client.HornetQQueue;

import org.apache.log4j.Logger;

public class HornetQClientProvider implements IJMSClientProvider {
    
    private static final String USER = "hornetq.user";
    private static final String PASSWORD = "hornetq.password";
    private static final String HOST_ADDR = "hornetq.host.addr";
    private static final String PORT = "hornetq.port";
    
    private static String userId = "guest";                   // as per EAP messaging sub-system
    private static String passwd = "guestp.1";                // as per EAP messaging sub-system
    private static String hostAddr = "localhost";              // ipAddress/dnsName that messaging broker is bound to
    private static int port = 5445;                           // tcp port that messaging broker is listening on
    private static HornetQConnectionFactory cFactory = null;
    private static Object lockObj = new Object();
    private static Logger log = Logger.getLogger("HornetQClientProvider");
    
    static{
        userId = System.getProperty(USER, userId);
        passwd = System.getProperty(PASSWORD, passwd);
        hostAddr = System.getProperty(HOST_ADDR,hostAddr);
        port = Integer.parseInt(System.getProperty(PORT, Integer.toString(port)));
        
        StringBuffer sBuffer = new StringBuffer("static{} sending message using following properties : ");
        sBuffer.append("\n\tuserId = ");
        sBuffer.append(userId);
        sBuffer.append("\n\t : passwd=");
        sBuffer.append(passwd);
        sBuffer.append("\n\t : hostAddr=");
        sBuffer.append(hostAddr);
        sBuffer.append("\n\t : port=");
        sBuffer.append(port);
        log.info(sBuffer.toString());
    }
    public HornetQClientProvider(){}

    public Connection getConnection() throws JMSException {
        if(cFactory == null){
            synchronized(lockObj){
                if(cFactory != null)
                    return cFactory.createConnection();

                Map<String, Object> params = new HashMap<String,Object>();
                params.put(TransportConstants.HOST_PROP_NAME, hostAddr);
                params.put(TransportConstants.PORT_PROP_NAME, port);
                cFactory = new HornetQConnectionFactory(false, new TransportConfiguration(NettyConnectorFactory.class.getName(), params));
            }
        }
        Connection connectObj =  cFactory.createConnection(userId, passwd);
        connectObj.start();
        return connectObj;
    }

    public Queue getQueue(String name) throws JMSException {
        return new HornetQQueue(name);
    }

}
