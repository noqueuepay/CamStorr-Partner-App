package com.anvay.cctvpartner.models;


import com.google.firebase.Timestamp;

public class Wallet {
    private double walletBalance;
    private String accountHolderName, accountNumber, ifscCode, branchName;
    private Timestamp lastOrderTimestamp;
    private Timestamp lastProjectTimestamp;

    public Wallet() {
    }

    public Wallet(boolean empty) {
        walletBalance = 0.0;
        lastOrderTimestamp = Timestamp.now();
        lastProjectTimestamp = Timestamp.now();
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Timestamp getLastOrderTimestamp() {
        return lastOrderTimestamp;
    }

    public void setLastOrderTimestamp(Timestamp lastOrderTimestamp) {
        this.lastOrderTimestamp = lastOrderTimestamp;
    }

    public Timestamp getLastProjectTimestamp() {
        return lastProjectTimestamp;
    }

    public void setLastProjectTimestamp(Timestamp lastProjectTimestamp) {
        this.lastProjectTimestamp = lastProjectTimestamp;
    }
}
