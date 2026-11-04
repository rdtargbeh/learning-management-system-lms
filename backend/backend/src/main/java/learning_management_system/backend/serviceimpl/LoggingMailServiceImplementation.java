package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Development mail: messages go to the log (search for "MAIL to="). Replace with a real sender for production. */
@Service
public class LoggingMailServiceImplementation implements MailService {
    private static final Logger log = LoggerFactory.getLogger(LoggingMailServiceImplementation.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("MAIL to={} subject=\"{}\" body=\"{}\"", to, subject, body.replace("\n", " | "));
    }
}
