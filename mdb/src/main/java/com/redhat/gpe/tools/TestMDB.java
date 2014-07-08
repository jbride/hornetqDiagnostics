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
})
public class TestMDB implements MessageListener {

    private Logger log = LoggerFactory.getLogger("LabMDB");

    public void onMessage(final Message message) {
        try{
            String payload = ((BytesMessage)message).readUTF();
            log.info("onMessage() payload = "+payload);
        }catch(Exception x) {
            x.printStackTrace();
        }
    }
}
