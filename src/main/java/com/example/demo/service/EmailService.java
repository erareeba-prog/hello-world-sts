package com.example.demo.service;

import com.example.demo.model.Donation;
import com.example.demo.model.Fulfillment;
import com.example.demo.model.Ngo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@ngoplatform.com}")
    private String fromEmail;

    // ✅ Send email helper
    private void sendEmail(String to,
                            String subject,
                            String htmlBody) {
        try {
            MimeMessage message =
                mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            System.out.println(
                "✅ Email sent to: " + to +
                " | Subject: " + subject);
        } catch (Exception e) {
            System.err.println(
                "❌ Email failed to " + to +
                ": " + e.getMessage());
        }
    }

    // ✅ Welcome email
    @Async
    public void sendWelcomeEmail(String to,
                                  String name) {
        String html = """
            <div style="font-family:Arial;max-width:600px;
                margin:auto;padding:30px;
                border-radius:12px;
                background:#f8f9fa;">
                <div style="background:linear-gradient(
                    135deg,#1a237e,#1565c0);
                    padding:30px;border-radius:12px 12px 0 0;
                    text-align:center;">
                    <h1 style="color:white;margin:0;">
                        🏛️ NGO Platform
                    </h1>
                </div>
                <div style="background:white;padding:30px;
                    border-radius:0 0 12px 12px;">
                    <h2>Welcome, %s! 🎉</h2>
                    <p>Thank you for joining the NGO
                    Donation Platform.</p>
                    <p>You can now:</p>
                    <ul>
                        <li>Browse NGOs and their needs</li>
                        <li>Make donations</li>
                        <li>Track your impact</li>
                    </ul>
                    <a href="http://localhost:8080/dashboard.html"
                        style="background:#1565c0;color:white;
                        padding:12px 24px;border-radius:8px;
                        text-decoration:none;display:inline-block;
                        margin-top:15px;">
                        Go to Dashboard →
                    </a>
                </div>
            </div>
            """.formatted(name);
        sendEmail(to, "Welcome to NGO Platform! 🎉", html);
    }

    // ✅ Donation confirmation email
    @Async
    public void sendDonationConfirmation(String to,
                                          String name,
                                          Donation d) {
        String html = """
            <div style="font-family:Arial;max-width:600px;
                margin:auto;padding:30px;">
                <div style="background:linear-gradient(
                    135deg,#1a237e,#1565c0);
                    padding:20px;border-radius:12px 12px 0 0;
                    text-align:center;">
                    <h2 style="color:white;margin:0;">
                        💰 Donation Confirmed
                    </h2>
                </div>
                <div style="background:white;padding:30px;
                    border-radius:0 0 12px 12px;
                    box-shadow:0 4px 15px rgba(0,0,0,0.1);">
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Your donation has been received!</p>
                    <table style="width:100%%;
                        border-collapse:collapse;
                        margin:20px 0;">
                        <tr style="background:#f8f9fa;">
                            <td style="padding:10px;
                                font-weight:bold;">
                                Amount
                            </td>
                            <td style="padding:10px;">
                                %s %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                font-weight:bold;">
                                Status
                            </td>
                            <td style="padding:10px;">
                                <span style="background:#ffc107;
                                    padding:3px 10px;
                                    border-radius:12px;
                                    font-size:12px;">
                                    %s
                                </span>
                            </td>
                        </tr>
                        <tr style="background:#f8f9fa;">
                            <td style="padding:10px;
                                font-weight:bold;">
                                Receipt ID
                            </td>
                            <td style="padding:10px;
                                font-family:monospace;
                                font-size:12px;">
                                %s
                            </td>
                        </tr>
                    </table>
                    <p style="color:#666;font-size:13px;">
                        Thank you for your generosity! 🙏
                    </p>
                </div>
            </div>
            """.formatted(
                name,
                d.getCurrency(), d.getAmountOrQty(),
                d.getStatus(),
                d.getReceiptId() != null
                    ? d.getReceiptId() : "N/A"
            );
        sendEmail(to,
            "Donation Confirmed - ₹" + d.getAmountOrQty(),
            html);
    }

    // ✅ NGO approval email
    @Async
    public void sendNgoApprovalEmail(String to, Ngo ngo) {
        String html = """
            <div style="font-family:Arial;max-width:600px;
                margin:auto;padding:30px;">
                <div style="background:linear-gradient(
                    135deg,#2e7d32,#43a047);
                    padding:20px;border-radius:12px 12px 0 0;
                    text-align:center;">
                    <h2 style="color:white;margin:0;">
                        🎉 NGO Approved!
                    </h2>
                </div>
                <div style="background:white;padding:30px;
                    border-radius:0 0 12px 12px;
                    box-shadow:0 4px 15px rgba(0,0,0,0.1);">
                    <p>Congratulations!</p>
                    <p>Your NGO <strong>%s</strong> has been
                    approved and is now active on the platform.
                    </p>
                    <p>You can now:</p>
                    <ul>
                        <li>Post needs for donations</li>
                        <li>Manage fulfillments</li>
                        <li>Add NGO members</li>
                    </ul>
                    <a href="http://localhost:8080/dashboard.html"
                        style="background:#2e7d32;color:white;
                        padding:12px 24px;border-radius:8px;
                        text-decoration:none;display:inline-block;
                        margin-top:15px;">
                        Go to Dashboard →
                    </a>
                </div>
            </div>
            """.formatted(ngo.getName());
        sendEmail(to, "NGO Approved - " + ngo.getName(),
            html);
    }

    // ✅ Fulfillment update email
    @Async
    public void sendFulfillmentUpdateEmail(String to,
                                            Fulfillment f) {
        String html = """
            <div style="font-family:Arial;max-width:600px;
                margin:auto;padding:30px;">
                <div style="background:linear-gradient(
                    135deg,#1565c0,#0d47a1);
                    padding:20px;border-radius:12px 12px 0 0;
                    text-align:center;">
                    <h2 style="color:white;margin:0;">
                        ✅ Impact Update!
                    </h2>
                </div>
                <div style="background:white;padding:30px;
                    border-radius:0 0 12px 12px;
                    box-shadow:0 4px 15px rgba(0,0,0,0.1);">
                    <p>Great news!</p>
                    <p>A need has been fulfilled through
                    your donation.</p>
                    <table style="width:100%%;
                        border-collapse:collapse;
                        margin:20px 0;">
                        <tr style="background:#f8f9fa;">
                            <td style="padding:10px;
                                font-weight:bold;">
                                Qty Applied
                            </td>
                            <td style="padding:10px;">
                                %s
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:10px;
                                font-weight:bold;">
                                Status
                            </td>
                            <td style="padding:10px;">
                                <span style="background:#28a745;
                                    color:white;padding:3px 10px;
                                    border-radius:12px;
                                    font-size:12px;">
                                    CONFIRMED
                                </span>
                            </td>
                        </tr>
                    </table>
                    <p style="color:#666;font-size:13px;">
                        Thank you for making a difference! 💙
                    </p>
                </div>
            </div>
            """.formatted(f.getQtyApplied());
        sendEmail(to, "Your Donation Made an Impact! ✅",
            html);
    }

    // ✅ OTP email
    @Async
    public void sendOtpEmail(String to,
                              String name,
                              String otp) {
        String html = """
            <div style="font-family:Arial;max-width:600px;
                margin:auto;padding:30px;">
                <div style="background:linear-gradient(
                    135deg,#1a237e,#1565c0);
                    padding:20px;border-radius:12px 12px 0 0;
                    text-align:center;">
                    <h2 style="color:white;margin:0;">
                        📱 Phone Verification OTP
                    </h2>
                </div>
                <div style="background:white;padding:30px;
                    border-radius:0 0 12px 12px;
                    box-shadow:0 4px 15px rgba(0,0,0,0.1);
                    text-align:center;">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>Your OTP is:</p>
                    <div style="font-size:40px;
                        font-weight:bold;
                        letter-spacing:12px;
                        color:#1565c0;
                        background:#e3f2fd;
                        padding:20px;
                        border-radius:12px;
                        margin:20px 0;">
                        %s
                    </div>
                    <p style="color:#666;font-size:13px;">
                        This OTP expires in 5 minutes.
                        Do not share it with anyone.
                    </p>
                </div>
            </div>
            """.formatted(name, otp);
        sendEmail(to, "Your OTP - NGO Platform", html);
    }
}