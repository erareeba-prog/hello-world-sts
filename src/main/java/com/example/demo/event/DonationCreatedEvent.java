package com.example.demo.event;

import com.example.demo.model.Donation;
import org.springframework.context.ApplicationEvent;

public class DonationCreatedEvent
        extends ApplicationEvent {

    private final Donation donation;
    private final String donorEmail;
    private final String donorName;

    public DonationCreatedEvent(Object source,
                                 Donation donation,
                                 String donorEmail,
                                 String donorName) {
        super(source);
        this.donation = donation;
        this.donorEmail = donorEmail;
        this.donorName = donorName;
    }

    public Donation getDonation() { return donation; }
    public String getDonorEmail() { return donorEmail; }
    public String getDonorName() { return donorName; }
}