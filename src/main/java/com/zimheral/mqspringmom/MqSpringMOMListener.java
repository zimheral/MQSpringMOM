package com.zimheral.mqspringmom;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import com.ibm.jms.JMSTextMessage;

/**
 * Listener to be injected in the container. It manages incoming messages and
 * routes them accordingly.
 * 
 * 			@Scope("prototype") Must be added for multiple instances of this
 *          class Ex: listener queues>1
 */

@Component
public class MqSpringMOMListener implements MessageListener {

	@Value("${project.activeMQ.activeQueues}")
	private String[] activeQueues;

	@Autowired
	@Qualifier("jmsOperations")
	private JmsOperations jmsOperations;

	@Autowired
	Logger mqspringmomLog;

	@Value("${project.mq.mqQueue}")
	private String mqQueue;

	private Random random = new Random(System.currentTimeMillis());
	private int randomQueue;
	private final String CLASSTAG = "[" + this.getClass().getSimpleName() + "] ";

	@Override
	public void onMessage(Message message) {
		
		/*Listener ID
		 * String threadID = Thread.currentThread().getName()
				.substring(Thread.currentThread().getName().indexOf("-") + 1); */ 
				
		try {
			
			MQMessage msgMQ = new MQMessage();
			String msgText;
			if (message instanceof JMSTextMessage) {
				msgMQ = new MQMessage();
				JMSTextMessage msg = (JMSTextMessage) message;
				msgText = msg.getText();
				
				mqspringmomLog.info(CLASSTAG + "new JMSTextMessage with text: " + "'" + msgText + "'" + " received from queue: " + "'" + mqQueue + "'" +
						" - JMSCorrelationID: "+ "'" + msg.getJMSMessageID()+ "'");

				msgMQ.setMessage(msgText);
				msgMQ.setId(msg.getJMSMessageID());
				sendMsg(msgMQ);
			}
			
			else if (message instanceof BytesMessage) {

				msgMQ = new MQMessage();
				BytesMessage msg = (BytesMessage) message;
				long TEXT_LENGTH = new Long(msg.getBodyLength());
				byte[] textBytes = new byte[(int) TEXT_LENGTH];
				msg.readBytes(textBytes);
				msgText = new String(textBytes, "Cp284");
				
				//Assuming a message with parameter values separated by "|". Change accordingly unless you like Exceptions
				List<String> params = new LinkedList<>(Arrays.asList(msgText.split("\\|"))); 

				mqspringmomLog.info(CLASSTAG + "new BytesMessage with text: " + "'" + msgText + "'" + " received from queue: " + "'" + mqQueue + "'" + 
						" - JMSCorrelationID: "+ "'" + msg.getJMSMessageID()+"'" );

				msgMQ.setMessage(params.remove(0));
				msgMQ.setId(msg.getJMSMessageID());
				msgMQ.setParams(params);

				sendMsg(msgMQ);
				
			}else {
				mqspringmomLog.info("Message of type: "+message.getClass()+" not handled");
			}

		}catch (Exception e) {
			mqspringmomLog.error(CLASSTAG + e.getMessage(), e);
		}
	}		
	
	public void sendMsg(MQMessage msgMQ) {

		randomQueue = (int) (random.nextDouble() * activeQueues.length);
				
		jmsOperations.convertAndSend(activeQueues[randomQueue], msgMQ, new CustomPostProcessor(msgMQ.getId()));
		mqspringmomLog.info(CLASSTAG + "'"+ msgMQ.getId() + "'"+ " routed to activeMQ Queue name ='" + activeQueues[randomQueue] + "'");
	}	
	
	private class CustomPostProcessor implements MessagePostProcessor {
		private final String jmsCorrelationId;

		public CustomPostProcessor(final String jmsCorrelationId) {
			this.jmsCorrelationId = jmsCorrelationId;
		}

		@Override
		public Message postProcessMessage(final Message msg)
				throws JMSException {
			msg.setJMSCorrelationID(jmsCorrelationId);
			return msg;
		}
	}
}
