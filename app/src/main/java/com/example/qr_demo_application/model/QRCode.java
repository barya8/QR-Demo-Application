package com.example.qr_demo_application.model;

import java.io.Serializable;

public class QRCode implements Serializable {
    private String size;
    private String correction;
    private String url;
    private String barcodeImage;
    private String id;
    private String startTime;
    private String endTime;
    private String isScanned;
    private String type;

    public QRCode() {
    }

    public String getSize() {
        return size;
    }

    public QRCode setSize(String size) {
        this.size = size;
        return this;
    }

    public String getCorrection() {
        return correction;
    }

    public QRCode setCorrection(String correction) {
        this.correction = correction;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public QRCode setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getBarcodeImage() {
        return barcodeImage;
    }

    public QRCode setBarcodeImage(String barcodeImage) {
        this.barcodeImage = barcodeImage;
        return this;
    }

    public String getId() {
        return id;
    }

    public QRCode setId(String id) {
        this.id = id;
        return this;
    }

    public String getStartTime() {
        return startTime;
    }

    public QRCode setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getEndTime() {
        return endTime;
    }

    public QRCode setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public String getIsScanned() {
        return isScanned;
    }

    public QRCode setIsScanned(String scanned) {
        this.isScanned = scanned;
        return this;
    }

    public String getType() {
        return type;
    }

    public QRCode setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "QRCode{" +
                "size='" + size + '\'' +
                ", correction='" + correction + '\'' +
                ", url='" + url + '\'' +
                ", barcodeImage='" + barcodeImage + '\'' +
                '}';
    }
}