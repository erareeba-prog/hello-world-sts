package com.example.demo.event;

import com.example.demo.model.Fulfillment;
import org.springframework.context.ApplicationEvent;

public class FulfillmentConfirmedEvent
        extends ApplicationEvent {

    private final Fulfillment fulfillment;
    private final Long donorUserId;
    private final String donorEmail;

    public FulfillmentConfirmedEvent(Object source,
                                      Fulfillment fulfillment,
                                      Long donorUserId,
                                      String donorEmail) {
        super(source);
        this.fulfillment = fulfillment;
        this.donorUserId = donorUserId;
        this.donorEmail = donorEmail;
    }

    public Fulfillment getFulfillment() {
        return fulfillment;
    }
    public Long getDonorUserId() { return donorUserId; }
    public String getDonorEmail() { return donorEmail; }
}