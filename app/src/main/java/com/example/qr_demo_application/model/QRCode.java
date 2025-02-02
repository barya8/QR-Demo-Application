package com.example.qr_demo_application.model;

import java.io.Serializable;

public class QRCode implements Serializable {
    private String size;
    private String correction;
    private String url;
    private String barcodeImage;
    private String id;

    public QRCode() {
    }

    public QRCode(String size, String correction, String url, String barcodeImage, int id) {
        this.size = size;
        this.correction = correction;
        this.url = url;
        this.barcodeImage = barcodeImage;
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