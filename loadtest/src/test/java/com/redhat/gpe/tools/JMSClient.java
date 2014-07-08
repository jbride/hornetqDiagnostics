package com.redhat.gpe.tools;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.*;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.Logger;

public final class JMSClient extends AbstractJavaSamplerClient {

    private static final String PATH_TO_LOG4J_CONFIG = "path.to.log4j.xml";
    private static final String SUCCESS_MESSAGE = "**success**";

    private static final String SEND_QUEUE_NAME = "send.queue.name";
    private static final String RESPONSE_QUEUE_NAME = "response.queue.name";

    private static final long QUALITY_OF_SERVICE_THRESHOLD_MS = 5 * 1000;
    private static final String payloadStart = "{ \"id\" : ";
    private static final String payloadEnd = " }";

    private static IJMSClientProvider jmsProvider = new HornetQClientProvider();
    private static Logger log = Logger.getLogger("JMSClient");
    private static Connection connection = null;
    private static Queue sendQueue = null;
    private static Queue responseQueue = null;
    private static AtomicInteger count = new AtomicInteger();
    private static boolean waitForResponse = false;

    private String testName;

    // obviously gets invoked a single time per JVM
    static{
        String pathToLog4jConfig = System.getProperty(PATH_TO_LOG4J_CONFIG);
        if(pathToLog4jConfig != null && !pathToLog4jConfig.equals("")) {
            DOMConfigurator.configure(pathToLog4jConfig);
        }
        try {
            connection = jmsProvider.getConnection();
            String sendQueueName = System.getProperty(SEND_QUEUE_NAME, "GPE.SEND");
            String responseQueueName = System.getProperty(RESPONSE_QUEUE_NAME, "GPE.RESPONSE");
            sendQueue = jmsProvider.getQueue(sendQueueName);
            responseQueue = jmsProvider.getQueue(responseQueueName);

        }catch(Exception x) {
            throw new RuntimeException(x);
        }
    }

    // gets invoked a single time for each concurrent client
    public void setupTest(JavaSamplerContext context){
        testName = context.getParameter(TestElement.NAME);

        StringBuilder sBuilder = new StringBuilder("system properties =");
        log.info(sBuilder.toString());
    }

    public SampleResult runTest(JavaSamplerContext context){
        SampleResult result = new SampleResult();
        result.setSampleLabel(testName);
        try {
            result.sampleStart();

            sendAndConsume();
            result.setResponseMessage(SUCCESS_MESSAGE);
            result.setSuccessful(true);
            result.setResponseCodeOK();

        }catch(Throwable x){
            StringWriter sw = new StringWriter();
            x.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            log.error("runTest() stackTrace = "+stackTrace);
            result.setResponseMessage(stackTrace);
            result.setSuccessful(false);
        }
        return result;
    }
    
    private void sendAndConsume() throws Exception {
        Session session = null;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final MessageProducer producer = session.createProducer(sendQueue);
            String corrId = UUID.randomUUID().toString();

            BytesMessage msg = session.createBytesMessage();
            msg.setJMSCorrelationID(corrId);

            String payload =  payloadStart +count.incrementAndGet() + payloadEnd ;
            msg.writeUTF(payload);

            producer.send(msg);

            if(waitForResponse) {
                String selector = "JMSCorrelationID = '" + corrId + "'";
                MessageConsumer consumer = session.createConsumer(responseQueue, selector);
                Message response = consumer.receive(QUALITY_OF_SERVICE_THRESHOLD_MS);
                log.info("sendAndConsume() just received:  "+response);
            }

        } finally {
            if(session != null)
                session.close();
        }
    }

}
