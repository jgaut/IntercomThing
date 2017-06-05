package com.example.jeremy.intercomthing;


import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by jeremy on 05/06/2017.
 */

public class MyEmail extends AsyncTask<String, String, Boolean> {

    public void sendEmail(String subject, String body) {


        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(MyAppProperties.getProperty("MyEmail.username"), MyAppProperties.getProperty("MyEmail.password"));
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MyAppProperties.getProperty("MyEmail.username")));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(MyAppProperties.getProperty("MyEmail.usernameTo")));
            message.setSubject(subject);
            message.setText(body);

           /* MimeBodyPart messageBodyPart = new MimeBodyPart();

            Multipart multipart = new MimeMultipart();

            messageBodyPart = new MimeBodyPart();
            String file = "path of file to be attached";
            String fileName = "attachmentName";
            DataSource source = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);*/

            Transport.send(message);

            MyLog.logEvent("Email sent");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    protected Boolean doInBackground(String... strings) {
        Log.i("email", "sending...");
        this.sendEmail(strings[0], strings[1]);
        return new Boolean(true);
    }

    protected void onPostExecute() {
        MyLog.logEvent("Email sent");
    }

}
