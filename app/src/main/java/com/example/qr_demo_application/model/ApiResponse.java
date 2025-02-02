package com.example.qr_demo_application.model;

import java.util.Map;

public class ApiResponse {

    private ServiceResult serviceResult;
    private Map<String, Object> data;

    public ApiResponse(ServiceResult serviceResult, Map<String, Object> data) {
        this.serviceResult = serviceResult;
        this.data = data;
    }

    public ServiceResult getServiceResult() {
        return serviceResult;
    }

    public ApiResponse setServiceResult(ServiceResult serviceResult) {
        this.serviceResult = serviceResult;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public ApiResponse setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }
}