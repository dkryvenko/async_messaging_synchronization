package samples.synchronization;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ClusterRunner {

	public static class Node implements Runnable {
		private String name;
		private String state = "initial";

		public Node(String name) {
			this.name = name;
		}

		public void run() {
			try {
				ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
				Connection connection = factory.createConnection();
				connection.start();

				Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				Destination destination = session.createTopic("state_synch");
				
				MessageConsumer consumer = session.createConsumer(destination);
				consumer.setMessageListener(new MessageListener() {
					public void onMessage(Message message) {
						try {
							state = ((TextMessage) message).getText();
							System.out.println("Node [name=" + name + ", state=" + state + "]");
						} catch (JMSException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}

	public static void main(String[] args) {
		new Thread(new Node("node #1")).start();
		new Thread(new Node("node #2")).start();
		
		System.out.println("demo cluster is up and running ...");
	}

}
