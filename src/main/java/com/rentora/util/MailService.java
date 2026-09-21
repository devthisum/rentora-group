package com.rentora.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.security.SecureRandom;
import java.util.Properties;

/**
 * Singleton Pattern.
 * Sends real emails over SMTP (used for the forgot-password OTP).
 *
 * Configure via JVM system properties, the same way DBConnectionManager
 * is configured (see -Drentora.db.* ) — e.g. on the Tomcat startup line:
 *
 *   -Drentora.mail.host=smtp.gmail.com
 *   -Drentora.mail.port=587
 *   -Drentora.mail.username=yourshop@gmail.com
 *   -Drentora.mail.password=your16charAppPassword   (Gmail: use an "App Password", not your real password)
 *   -Drentora.mail.from=yourshop@gmail.com
 *
 * If these aren't set, the service falls back to logging the OTP to the
 * console instead of throwing — so login/registration/local dev still
 * works without SMTP configured.
 */
public final class MailService {

    private static volatile MailService instance;

    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String from;
    private final boolean configured;

    private MailService() {
        this.host = System.getProperty("rentora.mail.host", "smtp.gmail.com");
        this.port = System.getProperty("rentora.mail.port", "587");
        this.username = System.getProperty("rentora.mail.username", "");
        this.password = System.getProperty("rentora.mail.password", "");
        this.from = System.getProperty("rentora.mail.from", this.username);
        this.configured = !username.isBlank() && !password.isBlank();
    }

    public static MailService getInstance() {
        if (instance == null) {
            synchronized (MailService.class) {
                if (instance == null) instance = new MailService();
            }
        }
        return instance;
    }

    /** Generates a 6-digit numeric OTP, e.g. "042917". */
    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }

    /** Sends the OTP email. Falls back to a console log if SMTP isn't configured (see class doc). */
    public void sendPasswordResetOtp(String toEmail, String recipientName, String otpCode) throws MessagingException {
        String subject = "Your Rentora password reset code";
        String body =
                "Hi " + recipientName + ",\n\n" +
                "Your one-time code to reset your Rentora password is:\n\n" +
                "    " + otpCode + "\n\n" +
                "This code expires in 10 minutes. If you didn't request a password reset, " +
                "you can safely ignore this email.\n\n" +
                "- Rentora";

        if (!configured) {
            // No SMTP credentials set — this keeps local dev/grading runnable without email setup.
            System.out.println("[MAIL - SMTP NOT CONFIGURED] To: " + toEmail + " | OTP: " + otpCode);
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from, false));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
