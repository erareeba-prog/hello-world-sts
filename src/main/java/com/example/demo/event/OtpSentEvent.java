package com.example.demo.event;

import org.springframework.context.ApplicationEvent;

public class OtpSentEvent extends ApplicationEvent {

    private final Long userId;
    private final String phone;
    private final String otp;

    public OtpSentEvent(Object source,
                         Long userId,
                         String phone,
                         String otp) {
        super(source);
        this.userId = userId;
        this.phone = phone;
        this.otp = otp;
    }

    public Long getUserId() { return userId; }
    public String getPhone() { return phone; }
    public String getOtp() { return otp; }
}