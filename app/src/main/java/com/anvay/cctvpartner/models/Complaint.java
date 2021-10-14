package com.anvay.cctvpartner.models;

import android.text.format.DateFormat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Calendar;

public class Complaint implements Serializable {
    @ServerTimestamp
    private Timestamp timestamp;
    private String complaintCategory;
    private String ticketNumber;
    private String storeId;
    private String description;

    public Complaint() {
    }

    public Complaint(String complaintCategory, String ticketNumber, String storeId, String description) {
        this.complaintCategory = complaintCategory;
        this.ticketNumber = ticketNumber;
        this.storeId = storeId;
        this.description = description;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getComplaintCategory() {
        return complaintCategory;
    }

    public void setComplaintCategory(String complaintCategory) {
        this.complaintCategory = complaintCategory;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Exclude
    public String getDate() {
        if (timestamp == null)
            return "Now";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.getSeconds() * 1000);
        return DateFormat.format("dd-MMM", calendar).toString();
    }
}
