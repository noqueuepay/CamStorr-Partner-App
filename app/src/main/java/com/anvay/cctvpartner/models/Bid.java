package com.anvay.cctvpartner.models;

import androidx.annotation.Nullable;

import com.anvay.cctvpartner.utils.Constants;

public class Bid {

    private String storeName;
    private double bidAmount;
    private String storeId;
    private String date;
    private String imageUrl;

    public Bid() {
    }

    public Bid(String storeName, double bidAmount, String storeId, String imageUrl) {
        this.storeName = storeName;
        this.bidAmount = bidAmount;
        this.storeId = storeId;
        this.imageUrl = imageUrl;
        this.date = Constants.getDate(Constants.TIMESTAMP_DATE_SHORT);
    }

    public Bid(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof Bid)
            return ((Bid) object).storeId.equals(this.storeId);
        return false;
    }
}
