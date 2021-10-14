package com.anvay.cctvpartner.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.anvay.cctvpartner.models.Complaint;

public class ComplaintsViewModel extends ViewModel {

    private final MutableLiveData<Complaint> postedComplaint = new MutableLiveData<>();

    public LiveData<Complaint> getPostedComplaint() {
        return postedComplaint;
    }

    public void setPostedComplaint(Complaint complaint) {
        postedComplaint.setValue(complaint);
    }
}
