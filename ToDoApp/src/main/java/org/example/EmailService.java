package org.example;

import io.vertx.core.Vertx;
import io.vertx.ext.mail.StartTLSOptions;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;

public class EmailService {
    private final MailClient mailClient;

    public EmailService(Vertx vertx) {
        MailConfig config = new MailConfig()
                .setHostname("smtp.gmail.com")
                .setPort(587)
                .setStarttls(StartTLSOptions.REQUIRED)
                .setUsername("23DTSA03@kristujayanti.com")        // 🔴 Use your Gmail ID
                .setPassword("ciiu ozuc mbss jrcf");             // 🔴 App Password

        this.mailClient = MailClient.createShared(vertx, config);
    }

    public void sendEmail(String to, String subject, String text) {
        MailMessage message = new MailMessage()
                .setFrom("23dtsa03@kristujayanti.com")
                .setTo(to)
                .setSubject(subject)
                .setText(text);

        mailClient.sendMail(message, result -> {
            if (result.succeeded()) {
                System.out.println("✅ Email sent to " + to);
            } else {
                result.cause().printStackTrace();
            }
        });
    }

    // ✉️ Send Task Reminder Email
    public void sendReminderEmail(String to, String name, String taskTitle) {
        String subject = "🔔 Task Reminder: " + taskTitle;
        String message = "Hi " + name + ",\n\n"
                + "This is a friendly reminder that your task: \"" + taskTitle + "\" is still pending.\n"
                + "Don’t forget to complete it!\n\n"
                + "Regards,\nYash's ToDo App";

        sendEmail(to, subject, message);
    }
}
