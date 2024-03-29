import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMailSSL {
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
 
		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("baifriend","ilyvmUSD");
				}
			});
 
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("from@no-spam.com"));
			
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("baifriend@yahoo.com"));
			message.addRecipients(Message.RecipientType.CC, InternetAddress.parse("baifriend@gmail.com")); 
			message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse("yanfriend@163.com")); 
			
			message.setSubject("Testing Internet");
			message.setText("Dear Bai," +
					"\n\n The internet is still alive. Thanks. ");
 
			Transport.send(message);
 
			System.out.println("Done");
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}