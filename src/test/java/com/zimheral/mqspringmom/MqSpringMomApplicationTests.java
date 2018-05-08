package com.zimheral.mqspringmom;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableJms
public class MqSpringMomApplicationTests {
			
	@Autowired
	@Qualifier("mqQueueConnectionFactory")
	private ConnectionFactory mqQueueConnectionFactory;
	
	@Autowired
	private Logger mqspringmomLog;
	
	@Test
	public void contextLoads() {
	}
	
	@Test
    public void sendMsg() {
		
		JmsTemplate jmsTemplateTest = new JmsTemplate(mqQueueConnectionFactory);
        
		mqspringmomLog.info("Test Start");
        new Thread() {
            public void run() {
                for(int i=0;i<1000;i++) {
                    final String count = (i+1)+" message!";
                    MessageCreator messageCreator = new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                        	BytesMessage message = session.createBytesMessage();
                            try {
            					message.writeBytes(("Send test: " +count+"|param1|param2")
            							.getBytes("Cp284"));
            				} catch (UnsupportedEncodingException e) {
            					e.printStackTrace();
            				}               
                            return message;
                        }
                    };
                    jmsTemplateTest.send("MQ.QUEUE", messageCreator);                    
                }
            }
        }.start();
        
        try {
        	TimeUnit.SECONDS.sleep(20);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
                      
        mqspringmomLog.info("Test finished");
    }
}
