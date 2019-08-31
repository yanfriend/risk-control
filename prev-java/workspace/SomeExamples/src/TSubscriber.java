import java.io.*;
import javax.jms.*;
import javax.naming.*;

public class TSubscriber implements MessageListener {

    private boolean stop = false;

    public static void main(String[] args) {

        new TSubscriber().subscribe();
    }

    public void subscribe() {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            //Prompt for JNDI names
            System.out.println("Enter TopicConnectionFactory name:");
            String factoryName = reader.readLine();
            System.out.println("Enter Topic name:");
            String topicName = reader.readLine();
            reader.close();

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
            TopicSubscriber subscriber = session.createSubscriber(topic);
            subscriber.setMessageListener(this);
            connection.start();

            //Wait for stop
            while (!stop) {
                Thread.sleep(1000);
            }

            //Exit
            System.out.println("Exiting...");
            connection.close();
            System.out.println("Goodbye!");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void onMessage(Message message) {

        try {
            String msgText = ((TextMessage) message).getText();
            System.out.println(msgText);
            if ("stop".equals(msgText))
                stop = true;
        } catch (JMSException e) {
            e.printStackTrace();
            stop = true;
        }
    }
}