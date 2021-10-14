package com.anvay.cctvpartner.models;

public class Store {
    private String storeName, merchantName, mobileNumber, zipcode, address, email, imageUrl;

    public Store(String storeName, String merchantName, String mobileNumber, String zipcode, String address, String email, String imageUrl) {
        this.storeName = storeName;
        this.merchantName = merchantName;
        this.mobileNumber = mobileNumber;
        this.zipcode = zipcode;
        this.address = address;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public Store() {
    }

    public String getName() {
        return storeName;
    }

    public void setName(String name) {
        this.storeName = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
