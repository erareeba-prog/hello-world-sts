package com.example.demo.event;

import com.example.demo.model.Ngo;
import org.springframework.context.ApplicationEvent;

public class NgoStatusEvent extends ApplicationEvent {

    private final Ngo ngo;
    private final String status; // "approved" or "rejected"
    private final String adminEmail;
    private final Long ngoOwnerUserId;

    public NgoStatusEvent(Object source,
                           Ngo ngo,
                           String status,
                           String adminEmail,
                           Long ngoOwnerUserId) {
        super(source);
        this.ngo = ngo;
        this.status = status;
        this.adminEmail = adminEmail;
        this.ngoOwnerUserId = ngoOwnerUserId;
    }

    public Ngo getNgo() { return ngo; }
    public String getStatus() { return status; }
    public String getAdminEmail() { return adminEmail; }
    public Long getNgoOwnerUserId() {
        return ngoOwnerUserId;
    }
}