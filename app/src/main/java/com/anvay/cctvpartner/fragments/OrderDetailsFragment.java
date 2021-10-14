package com.anvay.cctvpartner.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.cctvpartner.databinding.FragmentOrderDetailsBinding;
import com.anvay.cctvpartner.models.OrderItem;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.OrderViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrderDetailsFragment extends Fragment {
    private OrderItem orderItem;
    private FragmentOrderDetailsBinding binding;
    private View loadingView;
    private OrderViewModel orderViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false);
        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        loadingView = binding.loadingLayout.getRoot();
        orderItem = orderViewModel.getOrderItem();
        if (orderItem.getStatus() != Constants.OrderStatus.PENDING)
            binding.shipOrderButton.setVisibility(View.INVISIBLE);
        binding.shipOrderButton.setOnClickListener(view -> setReceived());
        binding.receiptButton.setOnClickListener(view -> launchReceiptFragment());
        initUI();
        return binding.getRoot();
    }

    private void launchReceiptFragment() {
//        CartReceiptFragment fragment = new CartReceiptFragment();
//        Bundle bundle = new Bundle();
//        bundle.putBoolean(Constants.KEY_FROM_ORDERS_SCREEN, true);
//        fragment.setArguments(bundle);
//        FragmentManager fragmentManager = getParentFragmentManager();
//        assert getParentFragment() != null;
//        fragmentManager.beginTransaction()
//                .add(getParentFragment().requireView().findViewById(R.id.fragment_container).getId(), fragment)
//                .addToBackStack(null)
//                .commit();
    }

    private void setReceived() {
        loadingView.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.ORDERS_URL)
                .document(orderItem.getDocumentId())
                .update(Constants.KEY_ORDER_STATUS, Constants.OrderStatus.SHIPPED)
                .addOnSuccessListener(unused -> {
                    binding.shipOrderButton.setVisibility(View.INVISIBLE);
                    binding.statusDisplay.setText(Constants.OrderStatus.SHIPPED.name);
                    binding.statusDisplay.setTextColor(Constants.OrderStatus.SHIPPED.color);
                    orderViewModel.setStatusShipped();
                    Toast.makeText(getContext(), "Order marked as shipped", Toast.LENGTH_SHORT).show();
                    loadingView.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                    loadingView.setVisibility(View.INVISIBLE);
                });
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        if (orderItem == null)
            return;
        binding.nameDisplay.setText(orderItem.getProductName());
        binding.customerNameDisplay.setText(orderItem.getCustomerName());
        binding.transactionNoDisplay.setText(orderItem.getTransactionId());
        binding.priceDisplay.setText("\u20B9 " + orderItem.getPrice());
        binding.quantityDisplay.setText(String.valueOf(orderItem.getQuantity()));
        binding.paidAmountDisplay.setText("\u20B9 " + orderItem.getTotalPrice());
        binding.statusDisplay.setText(orderItem.getStatus().name);
        binding.statusDisplay.setTextColor(orderItem.getStatus().color);
        binding.addressDisplay.setText(orderItem.getCustomerAddress());
        binding.paymentDateDisplay.setText(orderItem.getDate());
    }
}
