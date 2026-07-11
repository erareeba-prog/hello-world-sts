package com.example.demo.event;

import com.example.demo.model.Notification;
import com.example.demo.service.EmailService;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    // ✅ Donation Created
    @Async
    @EventListener
    public void onDonationCreated(
            DonationCreatedEvent event) {

        // Save in-app notification
        notificationService.createNotification(
            // donorId is UUID in Donation but we need Long
            // Use a placeholder or fix the model
            1L, // TODO: fix when donor userId is Long
            Notification.TYPE_DONATION_CREATED,
            "Donation Created! 💰",
            "Your donation of " +
                event.getDonation().getAmountOrQty() +
                " " + event.getDonation().getCurrency() +
                " has been received.",
            event.getDonation().getDonationId().toString()
        );

        // Send email
        emailService.sendDonationConfirmation(
            event.getDonorEmail(),
            event.getDonorName(),
            event.getDonation()
        );
    }

    // ✅ NGO Approved
    @Async
    @EventListener
    public void onNgoStatus(NgoStatusEvent event) {

        boolean approved =
            "approved".equals(event.getStatus());

        notificationService.createNotification(
            event.getNgoOwnerUserId(),
            approved
                ? Notification.TYPE_NGO_APPROVED
                : Notification.TYPE_NGO_REJECTED,
            approved
                ? "NGO Approved! 🎉"
                : "NGO Application Update",
            approved
                ? "Your NGO '" + event.getNgo().getName() +
                  "' has been approved!"
                : "Your NGO '" + event.getNgo().getName() +
                  "' application was not approved.",
            event.getNgo().getNgoId().toString()
        );

        // Send email
        if (approved) {
            emailService.sendNgoApprovalEmail(
                event.getAdminEmail(),
                event.getNgo()
            );
        }
    }

    // ✅ Fulfillment Confirmed
    @Async
    @EventListener
    public void onFulfillmentConfirmed(
            FulfillmentConfirmedEvent event) {

        notificationService.createNotification(
            event.getDonorUserId(),
            Notification.TYPE_FULFILLMENT_CONFIRMED,
            "Your donation made an impact! ✅",
            "A need has been fulfilled through your " +
                "donation. Thank you for your generosity!",
            event.getFulfillment()
                .getFulfillmentId().toString()
        );

        emailService.sendFulfillmentUpdateEmail(
            event.getDonorEmail(),
            event.getFulfillment()
        );
    }

    // ✅ OTP Sent
    @Async
    @EventListener
    public void onOtpSent(OtpSentEvent event) {

        notificationService.createNotification(
            event.getUserId(),
            Notification.TYPE_OTP_SENT,
            "OTP Sent 📱",
            "An OTP has been sent to " +
                event.getPhone(),
            null
        );
    }
}