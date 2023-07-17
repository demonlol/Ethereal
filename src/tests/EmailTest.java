package tests;

import java.util.Properties;
import javax.mail.*;

import com.sun.mail.imap.IMAPStore;

public class EmailTest {

    public static void receiveEmail(String user, String password) {
        try {
            //1) get the session object
            Properties properties = new Properties();
//            properties.put("mail.pop3.host", pop3Host);
            properties.setProperty("mail.store.protocol", "imaps");
            //Set host address
            //hccnet.heartland.edu\tcheek02
            properties.setProperty("mail.imaps.host", "imap.gmail.com");
            properties.setProperty("mail.smtp.auth", "true");
            properties.setProperty("mail.debug.auth", "true");
            //Set specified port
            properties.setProperty("mail.imaps.port", "993"); //587 tls
            //Using SSL
            properties.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.setProperty("mail.imaps.socketFactory.fallback", "false");
            Authenticator auth = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            };
            Session emailSession = Session.getDefaultInstance(properties, auth);

            //2) create the POP3 store object and connect with the pop server
            IMAPStore emailStore = (IMAPStore) emailSession.getStore("imaps");
            emailStore.connect(user, password);

            //3) create the folder object and open it
            Folder emailFolder = emailStore.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            //4) retrieve the messages from the folder in an array and print it
//            List<EmailMessage> messages = Arrays.stream(emailFolder.getMessages()).sorted((obj) -> {
//                EmailMessage em = (EmailMessage) obj;
//                return Comparator.comparingLong(em::getSentDateLong);
//            }).collect(Collectors.toList());
//            for (int i = 0; i < messages.length; i++) {
//                Message message = messages[i];
//                if(!message.getFrom()[0].toString().endsWith("heartland.edu")) continue;
//
//                System.out.println("---------------------------------");
//                System.out.println("Email Number " + (i + 1));
//                System.out.println("Subject: " + message.getSubject());
//                System.out.println("From: " + message.getFrom()[0]);
//                System.out.println("Text: " + message.getContent().toString());
//            }

            //5) close the store and folder objects

            emailFolder.close(false);
            emailStore.close();

        } catch (NoSuchProviderException e) {e.printStackTrace();}
        catch (MessagingException e) {e.printStackTrace();}
//        catch (IOException e) {e.printStackTrace();}
    }

    public static void main(String[] args) {
        String username= "tcheek02@my.heartland.edu";
        String password= "agecmkeipxfntdxp";//change accordingly

        receiveEmail(username, password);
    }

}
