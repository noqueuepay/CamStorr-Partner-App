package com.anvay.cctvpartner.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WalletViewModel extends ViewModel {
    private final MutableLiveData<String> walletBalance = new MutableLiveData<>();

    public LiveData<String> getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(String walletBalance) {
        this.walletBalance.setValue(walletBalance);
    }
}
