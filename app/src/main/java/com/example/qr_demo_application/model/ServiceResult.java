package com.example.qr_demo_application.model;

public class ServiceResult {
    private String returnCode;
    private String returnMessage;

    public ServiceResult(String returnCode, String returnMessage) {
        this.returnCode = returnCode;
        this.returnMessage = returnMessage;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public ServiceResult setReturnCode(String returnCode) {
        this.returnCode = returnCode;
        return this;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public ServiceResult setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
        return this;
    }
}