/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.email;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending email notifications.
 */
@Stateless
public class EmailService {
    
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    
    @Inject
    private EmailConfigRepository emailConfigRepository;
    
    private Session mailSession;
    
    @PostConstruct
    public void init() {
        refreshMailSession();
    }
    
    /**
     * Refresh the mail session with the latest active configuration.
     */
    public void refreshMailSession() {
        Optional<EmailConfig> configOpt = emailConfigRepository.findActiveConfig();
        if (configOpt.isPresent()) {
            EmailConfig config = configOpt.get();
            Properties props = new Properties();
            
            props.put("mail.smtp.host", config.getSmtpHost());
            props.put("mail.smtp.port", String.valueOf(config.getSmtpPort()));
            props.put("mail.smtp.auth", String.valueOf(config.isRequireAuth()));
            props.put("mail.smtp.starttls.enable", String.valueOf(config.isUseTls()));
            props.put("mail.smtp.ssl.enable", String.valueOf(config.isUseSsl()));
            props.put("mail.smtp.ssl.trust", config.getSmtpHost());
            
            if (config.isRequireAuth() && config.getUsername() != null && config.getPassword() != null) {
                mailSession = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(config.getUsername(), config.getPassword());
                    }
                });
            } else {
                mailSession = Session.getInstance(props);
            }
            
            LOGGER.info("Mail session initialized with SMTP host: " + config.getSmtpHost());
        } else {
            LOGGER.warning("No active email configuration found. Email notifications will not be sent.");
        }
    }
    
    /**
     * Send an email asynchronously.
     */
    @Asynchronous
    public void sendEmailAsync(String to, String subject, String body) {
        sendEmail(to, subject, body);
    }
    
    /**
     * Send an email synchronously.
     */
    public boolean sendEmail(String to, String subject, String body) {
        if (mailSession == null) {
            LOGGER.warning("Mail session not initialized. Cannot send email.");
            return false;
        }
        
        Optional<EmailConfig> configOpt = emailConfigRepository.findActiveConfig();
        if (configOpt.isEmpty()) {
            LOGGER.warning("No active email configuration found. Cannot send email.");
            return false;
        }
        
        EmailConfig config = configOpt.get();
        
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(config.getSenderEmail(), 
                config.getSenderName() != null ? config.getSenderName() : "Shelfinity Library"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            
            Transport.send(message);
            
            LOGGER.info("Email sent successfully to: " + to);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send email to: " + to, e);
            return false;
        }
    }
    
    /**
     * Send registration confirmation email.
     */
    @Asynchronous
    public void sendRegistrationConfirmation(String to, String userName) {
        String subject = "Welcome to Shelfinity Library!";
        String body = String.format(
            "Dear %s,\n\n" +
            "Thank you for registering with Shelfinity Library. Your account has been successfully created.\n\n" +
            "You can now log in using your email address and password to explore our collection of books.\n\n" +
            "If you did not register for an account, please contact us immediately.\n\n" +
            "Best regards,\n" +
            "Shelfinity Library Team",
            userName
        );
        sendEmail(to, subject, body);
    }
    
    /**
     * Send borrow request acknowledgment email.
     */
    @Asynchronous
    public void sendBorrowRequestAcknowledgment(String to, String userName, String bookTitle) {
        String subject = "Borrow Request Received";
        String body = String.format(
            "Dear %s,\n\n" +
            "We have received your borrow request for the book titled \"%s\". " +
            "Your request is being processed.\n\n" +
            "You will be notified once your borrow request has been reviewed and approved or declined by the admin.\n\n" +
            "Thank you for using Shelfinity Library.\n\n" +
            "Best regards,\n" +
            "Shelfinity Library Team",
            userName, bookTitle
        );
        sendEmail(to, subject, body);
    }
    
    /**
     * Send borrow request approval email.
     */
    @Asynchronous
    public void sendBorrowRequestApproval(String to, String userName, String bookTitle) {
        String subject = "Borrow Request Approved - " + bookTitle;
        String body = String.format(
            "Dear %s,\n\n" +
            "We are pleased to inform you that your borrow request for the book \"%s\" has been approved. " +
            "You can now pick up the book at your convenience.\n\n" +
            "Thank you for using Shelfinity Library. We hope you enjoy reading!\n\n" +
            "Best regards,\n" +
            "Shelfinity Library Team",
            userName, bookTitle
        );
        sendEmail(to, subject, body);
    }
    
    /**
     * Send borrow request decline email.
     */
    @Asynchronous
    public void sendBorrowRequestDecline(String to, String userName, String bookTitle, String reason) {
        String subject = "Borrow Request Declined - " + bookTitle;
        String body = String.format(
            "Dear %s,\n\n" +
            "We regret to inform you that your borrow request for the book \"%s\" has been declined.\n\n" +
            "Reason: %s\n\n" +
            "Please feel free to browse other books available in our collection or make a new borrow request.\n\n" +
            "Best regards,\n" +
            "Shelfinity Library Team",
            userName, bookTitle, reason != null ? reason : "Insufficient stock or book unavailable"
        );
        sendEmail(to, subject, body);
    }
    
    /**
     * Send book return confirmation email.
     */
    @Asynchronous
    public void sendReturnConfirmation(String to, String userName, String bookTitle) {
        String subject = "Book Return Successful";
        String body = String.format(
            "Dear %s,\n\n" +
            "We have successfully received the return of the book \"%s\". " +
            "Thank you for returning it on time.\n\n" +
            "We hope you enjoyed reading it. Feel free to explore our other collections.\n\n" +
            "Best regards,\n" +
            "Shelfinity Library Team",
            userName, bookTitle
        );
        sendEmail(to, subject, body);
    }
    
    /**
     * Send overdue reminder email.
     */
    @Asynchronous
    public void sendOverdueReminder(String to, String userName, String bookTitle, int daysOverdue) {
        String subject = "Reminder: Your Book is Overdue";
        String body = String.format(
            "Dear %s,\n\n" +
            "This is a reminder that the book \"%s\" is overdue by %d day(s). " +
            "Please return the book as soon as possible to avoid any late fees.\n\n" +
            "You can return the book at your convenience or contact us for any assistance.\n\n" +
            "Best regards,\n" +
            "Shelfinity Library Team",
            userName, bookTitle, daysOverdue
        );
        sendEmail(to, subject, body);
    }
    /**
     * Send overdue notification email with due date.
     */
    @Asynchronous
    public void sendOverdueNotification(String to, String userName, String bookTitle, 
                                       java.time.LocalDateTime dueDate, int daysOverdue) {
        String subject = "Reminder: Your Book is Overdue";
        String body = String.format(
            "Dear %s,\n\n" +
            "This is a reminder that the book \"%s\" was due on %s and is now overdue by %d day(s). " +
            "Please return the book as soon as possible to avoid any late fees.\n\n" +
            "You can return the book at your convenience or contact us for any assistance.\n\n" +
            "Best regards,\n" +
            "Shelfinity Library Team",
            userName, bookTitle, dueDate.toLocalDate().toString(), daysOverdue
        );
        sendEmail(to, subject, body);
    }
    
    
    /**
     * Send admin alert for new request.
     */
    @Asynchronous
    public void sendAdminRequestAlert(String adminEmail, String requestType, String userName, String bookTitle) {
        String subject = "New " + requestType + " Request Pending";
        String body = String.format(
            "Dear Admin,\n\n" +
            "A new %s request has been submitted by a user. Please review the request in the admin panel.\n\n" +
            "Request Details:\n" +
            "- Request Type: %s\n" +
            "- User: %s\n" +
            "- Book: %s\n\n" +
            "Please approve or decline the request as needed.\n\n" +
            "Best regards,\n" +
            "Shelfinity Library System",
            requestType, requestType, userName, bookTitle
        );
        sendEmail(adminEmail, subject, body);
    }
    
    /**
     * Send profile update notification.
     */
    @Asynchronous
    public void sendProfileUpdateNotification(String to, String userName) {
        String subject = "Your Profile has been Updated";
        String body = String.format(
            "Dear %s,\n\n" +
            "We have successfully updated your profile information. " +
            "If you did not make this change, please contact us immediately.\n\n" +
            "Thank you for using Shelfinity Library.\n\n" +
            "Best regards,\n" +
            "Shelfinity Library Team",
            userName
        );
        sendEmail(to, subject, body);
    }
    
    /**
     * Send password change notification.
     */
    @Asynchronous
    public void sendPasswordChangeNotification(String to, String userName) {
        String subject = "Your Password Has Been Updated";
        String body = String.format(
            "Dear %s,\n\n" +
            "We have successfully updated your account password. " +
            "If you did not make this change, please contact us immediately.\n\n" +
            "Thank you for using Shelfinity Library.\n\n" +
            "Best regards,\n" +
            "Shelfinity Library Team",
            userName
        );
        sendEmail(to, subject, body);
    }
    
    /**
     * Send book reservation confirmation.
     */
    @Asynchronous
    public void sendReservationConfirmation(String to, String userName, String bookTitle) {
        String subject = "Book Reservation Confirmed";
        String body = String.format(
            "Dear %s,\n\n" +
            "Your reservation for the book \"%s\" has been confirmed. " +
            "You will be notified when the book becomes available.\n\n" +
            "Thank you for using Shelfinity Library.\n\n" +
            "Best regards,\n" +
            "Shelfinity Library Team",
            userName, bookTitle
        );
        sendEmail(to, subject, body);
    }
    
    /**
     * Send book availability notification.
     */
    @Asynchronous
    public void sendBookAvailabilityNotification(String to, String userName, String bookTitle) {
        String subject = "Reserved Book Now Available - " + bookTitle;
        String body = String.format(
            "Dear %s,\n\n" +
            "Good news! The book \"%s\" that you reserved is now available. " +
            "Please submit a borrow request to check out the book.\n\n" +
            "Thank you for using Shelfinity Library.\n\n" +
            "Best regards,\n" +
            "Shelfinity Library Team",
            userName, bookTitle
        );
        sendEmail(to, subject, body);
    }
}

// Made with Bob
