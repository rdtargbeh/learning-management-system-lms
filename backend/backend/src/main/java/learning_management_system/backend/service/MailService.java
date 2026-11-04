package learning_management_system.backend.service;

/**
 * Sends email (password resets, address verification). The built-in implementation only writes the message to the
 * application log; plug in SMTP or an email API for production.
 */
public interface MailService {

    void send(String to, String subject, String body);
}
