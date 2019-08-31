import java.io.*;
import java.util.Hashtable;

import javax.jms.*;
import javax.naming.*;

public class TPublisher {

    @SuppressWarnings("unchecked")
	public static void main(String[] args) throws NamingException {

    	Hashtable env=new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"weblogic.jndi.WLInitialContextFactory");
        env.put(Context.PROVIDER_URL,"t3://localhost:7001");

        Context ctx = new InitialContext(env);
            
        new TPublisher().publish();
    }
    
    public void publish() {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            //Prompt for JNDI names
            System.out.println("Enter TopicConnectionFactory name:");
            String factoryName = reader.readLine();
            System.out.println("Enter Topic name:");
            String topicName = reader.readLine();

            //Look up administered objects
            InitialContext initContext = new InitialContext();
            TopicConnectionFactory factory =
                (TopicConnectionFactory) initContext.lookup(factoryName);
            Topic topic = (Topic) initContext.lookup(topicName);
            initContext.close();

            //Create JMS objects
            TopicConnection connection = factory.createTopicConnection();
            TopicSession session =
                connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            TopicPublisher publisher = session.createPublisher(topic);

            //Send messages
            String messageText = null;
            while (true) {
                System.out.println("Enter message to send or 'quit':");
                messageText = reader.readLine();
                if ("quit".equals(messageText))
                    break;
                TextMessage message = session.createTextMessage(messageText);
                publisher.publish(message);
            }

            //Exit
            System.out.println("Exiting...");
            reader.close();
            connection.close();
            System.out.println("Goodbye!");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}