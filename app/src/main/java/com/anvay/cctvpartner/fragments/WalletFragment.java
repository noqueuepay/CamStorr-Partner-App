package com.anvay.cctvpartner.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.cctvpartner.MainActivity;
import com.anvay.cctvpartner.databinding.FragmentWalletBinding;
import com.anvay.cctvpartner.models.OngoingTask;
import com.anvay.cctvpartner.models.OrderItem;
import com.anvay.cctvpartner.models.Wallet;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.anvay.cctvpartner.utils.WalletViewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class WalletFragment extends Fragment {
    private FragmentWalletBinding binding;
    private EditText holderNameDisplay, accountNoDisplay, confirmAccountNoDisplay, ifscCodeDisplay, branchDisplay;
    private Button editSaveButton;
    private boolean editMode;
    private TextView walletBalanceDisplay;
    private String storeId, holderName, accountNo, confirmAccountNo, ifscCode, branchName;
    private View loadingView;
    private FirebaseFirestore db;
    private Timestamp latestOrderTimestamp, latestProjectTimestamp;
    private double walletBalance;
    private WalletViewModel walletViewModel;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        String title = "My Wallet";
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack(title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWalletBinding.inflate(inflater, container, false);
        editMode = false;
        storeId = MainActivity.storeId;
        db = FirebaseFirestore.getInstance();
        initUI();
        setupFields();
        getData();
        walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);
        editSaveButton.setOnClickListener(view -> {
            if (editMode) {
                checkDetails();
            } else {
                editMode = true;
                setupFields();
            }
        });
        return binding.getRoot();
    }

    private void getData() {
        if (storeId == null)
            return;
        loadingView.setVisibility(View.VISIBLE);
        db.collection(Constants.WALLET_URL)
                .document(storeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Wallet wallet = documentSnapshot.toObject(Wallet.class);
                    if (wallet != null) {
                        updateUI(wallet);
                        calculateWalletBalance(wallet);
                    }
                    loadingView.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading your wallet", Toast.LENGTH_SHORT).show();
                    loadingView.setVisibility(View.INVISIBLE);
                });
    }

    private void calculateWalletBalance(Wallet wallet) {
        /**
         * Best Way to do this is at server side using firebase functions
         */
        Timestamp lastOrderTimestamp = wallet.getLastOrderTimestamp();
        db.collection(Constants.ORDERS_URL)
                .whereEqualTo(Constants.KEY_STORE_ID, storeId)
                .whereGreaterThan(Constants.KEY_TIMESTAMP, lastOrderTimestamp)
                .orderBy(Constants.KEY_TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty())
                        return;
                    DocumentSnapshot first = queryDocumentSnapshots.getDocuments().get(0);
                    OrderItem item = first.toObject(OrderItem.class);
                    if (item != null)
                        latestOrderTimestamp = item.getTimestamp();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderItem orderItem = doc.toObject(OrderItem.class);
                        if (orderItem != null)
                            walletBalance += orderItem.getTotalPrice();
                    }
                    walletBalanceDisplay.setText("\u20b9 " + walletBalance);
                    calculateWalletBalance2(wallet);
                })
                .addOnFailureListener(e -> {
                    Log.e("Wallet", e.getMessage() + e.getCause());
                });

    }

    private void calculateWalletBalance2(Wallet wallet) {
        Timestamp lastProjectTimestamp = wallet.getLastProjectTimestamp();
        db.collection(Constants.BASE_ONGOING_TASKS_URL)
                .whereEqualTo(Constants.KEY_STORE_ID, storeId)
                .whereGreaterThan("paymentTimestamp", lastProjectTimestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty())
                        return;
                    DocumentSnapshot first = queryDocumentSnapshots.getDocuments().get(0);
                    OngoingTask task = first.toObject(OngoingTask.class);
                    if (task != null)
                        latestProjectTimestamp = task.getPaymentTimestamp();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        OngoingTask ongoingTask = doc.toObject(OngoingTask.class);
                        if (ongoingTask != null)
                            walletBalance += ongoingTask.getAcceptedBidAmount();
                    }
                    walletBalanceDisplay.setText("\u20b9 " + walletBalance);
                    updateViewModel();
                })
                .addOnFailureListener(e -> {
                    Log.e("Wallet", e.getMessage() + e.getCause());
                });
    }

    private void updateViewModel() {
        walletViewModel.setWalletBalance(String.valueOf(walletBalance));
        db.collection(Constants.WALLET_URL)
                .document(storeId)
                .update(Constants.KEY_WALLET_BALANCE, walletBalance,
                        "lastProjectTimestamp", latestProjectTimestamp,
                        "lastOrderTimestamp", latestOrderTimestamp);
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(Wallet wallet) {
        holderNameDisplay.setText(wallet.getAccountHolderName());
        accountNoDisplay.setText(wallet.getAccountNumber());
        confirmAccountNoDisplay.setText(wallet.getAccountNumber());
        ifscCodeDisplay.setText(wallet.getIfscCode());
        branchDisplay.setText(wallet.getBranchName());
        walletBalance = wallet.getWalletBalance();
        walletBalanceDisplay.setText("\u20b9 " + wallet.getWalletBalance());
    }

    private void checkDetails() {
        holderName = holderNameDisplay.getText().toString();
        accountNo = accountNoDisplay.getText().toString();
        confirmAccountNo = confirmAccountNoDisplay.getText().toString();
        ifscCode = ifscCodeDisplay.getText().toString();
        branchName = branchDisplay.getText().toString();
        if (holderName.isEmpty() || accountNo.isEmpty() || confirmAccountNo.isEmpty() || ifscCode.isEmpty()
                || branchName.isEmpty())
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
        else if (!accountNo.equals(confirmAccountNo))
            Toast.makeText(getContext(), "Account Number does not match", Toast.LENGTH_SHORT).show();
        else updateBankDetails();
    }

    private void updateBankDetails() {
        loadingView.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put("accountHolderName", holderName);
        map.put("accountNumber", accountNo);
        map.put("ifscCode", ifscCode);
        map.put("branchName", branchName);
        db.collection(Constants.WALLET_URL)
                .document(storeId)
                .update(map)
                .addOnSuccessListener(unused -> {
                    editMode = false;
                    setupFields();
                    loadingView.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating details", Toast.LENGTH_SHORT).show();
                    loadingView.setVisibility(View.INVISIBLE);
                });
    }

    private void setupFields() {
        holderNameDisplay.setEnabled(editMode);
        accountNoDisplay.setEnabled(editMode);
        confirmAccountNoDisplay.setEnabled(editMode);
        ifscCodeDisplay.setEnabled(editMode);
        branchDisplay.setEnabled(editMode);
        if (editMode)
            editSaveButton.setText("Update Details");
        else
            editSaveButton.setText("Edit Details");
    }

    private void initUI() {
        holderNameDisplay = binding.accountHolderName;
        accountNoDisplay = binding.accountNumber;
        confirmAccountNoDisplay = binding.confirmAccountNumber;
        ifscCodeDisplay = binding.ifscCode;
        branchDisplay = binding.branchName;
        loadingView = binding.loadingLayout.getRoot();
        walletBalanceDisplay = binding.walletBalance;
        editSaveButton = binding.editSaveButton;
    }
}
