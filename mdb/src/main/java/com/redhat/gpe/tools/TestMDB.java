package com.redhat.gpe.tools;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.BytesMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(name = "LabMDB", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/GPE.SEND"),
    @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1")
})
public class TestMDB implements MessageListener {

    public final static String SLEEP_TIME_MILLIS = "SLEEP_TIME_MILLIS";

    private Logger log = LoggerFactory.getLogger("LabMDB");
    private int sleepTime = 0;

    public TestMDB() {
        sleepTime = Integer.parseInt(System.getProperty(SLEEP_TIME_MILLIS, "0"));
    }

    public void onMessage(final Message message) {
        try{
            String payload = ((BytesMessage)message).readUTF();
            log.info("onMessage() payload = "+payload+" : sleeping for following millis: "+sleepTime);
            Thread.sleep(sleepTime);
        }catch(Exception x) {
            x.printStackTrace();
        }
    }
}
