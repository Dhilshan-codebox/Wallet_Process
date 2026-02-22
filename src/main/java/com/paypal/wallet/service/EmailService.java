package com.paypal.wallet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendTransactionEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("your-email@gmail.com");
            
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }
    }
    
    public void sendTransactionReceipt(String senderEmail, String receiverEmail, 
                                      Double amount, String description) {
        String senderSubject = "Payment Sent - $" + amount;
        String senderBody = "You have successfully sent $" + amount + 
                          "\nDescription: " + description +
                          "\nTo: " + receiverEmail;
        sendTransactionEmail(senderEmail, senderSubject, senderBody);
        
        String receiverSubject = "Payment Received - $" + amount;
        String receiverBody = "You have received $" + amount + 
                            "\nDescription: " + description +
                            "\nFrom: " + senderEmail;
        sendTransactionEmail(receiverEmail, receiverSubject, receiverBody);
    }
}