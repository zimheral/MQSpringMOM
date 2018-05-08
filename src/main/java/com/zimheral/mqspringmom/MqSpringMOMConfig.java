package com.zimheral.mqspringmom;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
* Configuration class containing all the beans following Spring standards.
* Includes factories to establish the connection to the IBM MQ and ActiveMQ brokers and 
* a container to manage the bridge between the connections and the listener.
* @version 1.0
*/

@Configuration
public class MqSpringMOMConfig {	
	
	@Value("${project.version}")
	private String version;		
	@Value("${project.mq.host}")
	private String host;
	@Value("${project.mq.port}")
	private Integer port;
	@Value("${project.mq.queue-manager}")
	private String queueManager;
	@Value("${project.mq.channel}")
	private String channel;
	@Value("${project.mq.username}")
	private String username;
	@Value("${project.mq.password}")
	private String password;	
	@Value("${project.mq.mqQueue}")
	private String mqQueue;	
	@Value("${project.mq.receive-timeout}")
	private long receiveTimeoutMQ;
	@Value("${project.activeMQ.receive-timeout}")
	private long receiveTimeout;
	@Value("${project.mq.concurrentConsumers}")
	private int concurrentConsumers;
	@Value("${project.activeMQ.activeQueues}")
	private String[] activeQueues;	
	@Value("${project.activeMQ.url}")
	private String activeMQ_url;	

	@Autowired
	MqSpringMOMListener mqspringmomListener;
	
	@Bean
	public Logger mqspringmomLog() {
		return LoggerFactory.getLogger(this.getClass());
	}

	@Primary
	@Bean(name = "mqQueueConnectionFactory")
	public MQQueueConnectionFactory  mqQueueConnectionFactory() throws JMSException  {
		
		MQEnvironment.sharingConversations = new Integer(1);
		
		MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
		mqQueueConnectionFactory.setConnectionNameList(host+"("+port+")");
		mqQueueConnectionFactory.setQueueManager(queueManager);
		mqQueueConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
		mqQueueConnectionFactory.setChannel(channel);
		mqQueueConnectionFactory.setClientReconnectOptions(WMQConstants.WMQ_CLIENT_RECONNECT);
					
		return mqQueueConnectionFactory;
	}
	
	@Bean(name = "mqCredentials")
	UserCredentialsConnectionFactoryAdapter mqCredentials(
			@Qualifier("mqQueueConnectionFactory") MQQueueConnectionFactory mqQueueConnectionFactory) {
		UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
		userCredentialsConnectionFactoryAdapter.setUsername(username);
		userCredentialsConnectionFactoryAdapter.setPassword(password);
		userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqQueueConnectionFactory);
		return userCredentialsConnectionFactoryAdapter;
	}
	
	@Bean
	DefaultMessageListenerContainer btcListenerContainer(@Qualifier("mqCredentials") UserCredentialsConnectionFactoryAdapter mqFactory) throws Exception {
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setMessageListener(mqspringmomListener);
		container.setConnectionFactory(mqFactory);
		container.setDestinationName(mqQueue);
		container.setConcurrentConsumers(concurrentConsumers);
		container.setReceiveTimeout(receiveTimeoutMQ);
		container.setCacheLevel(DefaultMessageListenerContainer.CACHE_SESSION);
		
		return container;
	}
		
	@Bean(name = "activeQueueConnectionFactory")
	public ActiveMQConnectionFactory activeQueueConnectionFactory() {
		return new ActiveMQConnectionFactory(activeMQ_url);
	}
	
	@Bean(name = "jmsOperations")
	public JmsOperations jmsOperations(@Qualifier("activeQueueConnectionFactory") ActiveMQConnectionFactory activeQueueConnectionFactory) {
		return new JmsTemplate(activeQueueConnectionFactory);
	}
		
	@PostConstruct
	  public void loadStartupLogs(){
		mqspringmomLog().info("MQSpringMOM Application v"+version+" - Spring");
		mqspringmomLog().info("mqQueue: "+mqQueue);
		mqspringmomLog().info("activeMQ Queues: "+Arrays.toString(activeQueues));
		mqspringmomLog().info("Configured "+concurrentConsumers+" connections");  	
	  }

	/**
	 * Code for reference just in case of multiple mqQueues. Class MqSpringMOMListener must implement
	 * JmsListenerConfigurer (multiple listener queues)
	 * 
	 * @Autowired private ApplicationContext applicationContext;
	 * 
	 * @Override public void configureJmsListeners(JmsListenerEndpointRegistrar
	 *           registrar) {
	 * 
	 *           for (int i = 0; i < BCOutQueues.length; i++) {
	 * 
	 *           SimpleJmsListenerEndpoint endpoint = new
	 *           SimpleJmsListenerEndpoint(); DefaultMessageListenerContainer
	 *           container = new DefaultMessageListenerContainer();
	 *           container.setConnectionFactory(mqQueueConnectionFactory());
	 *             
	 *           endpoint.setMessageListener(adapterMQ(mqQueues[i]));
	 *           endpoint.setupListenerContainer(container);
	 *           endpoint.setConcurrency("200-200");
	 * 
	 *           endpoint.setDestination(mqQueues[i]);
	 *           endpoint.setId("jmsEndpoint" + i);
	 *           bicentenarioLog().info(endpoint.getId());
	 *           registrar.registerEndpoint(endpoint);
	 * 
	 *           }
	 * 
	 *           } MessageListenerAdapter adapterBC(String queues) {
	 *           MqSpringMOMListener container = applicationContext.getBean(MqSpringMOMListener.class);
	 *           container.setQueueListener(queues); MessageListenerAdapter
	 *           messageListener = new MessageListenerAdapter(container);
	 *           messageListener.setDefaultListenerMethod("receiveMessage");
	 *           messageListener.setMessageConverter(null); return messageListener;
	 *           }
	 *
	 */
	 
}
