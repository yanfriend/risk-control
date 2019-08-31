package common;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
 
public class Email {
	public static void sendEmail(String toEmail, String subject, String content) {
		if (toEmail.equals("")) toEmail = "3475158568@messaging.sprintpcs.com";
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
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
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse("baifriend@gmail.com"));
			message.setSubject(subject);
			message.setText(content);
 
			Transport.send(message);
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("baifriend@g"));
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	public static void main(String[] args) {
        sendEmail("", "subject anything", "this is the content line 1. \n \n this line 2");
		System.out.println("Done");
	}
}