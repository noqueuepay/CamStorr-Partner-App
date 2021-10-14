package com.anvay.cctvpartner.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anvay.cctvpartner.MainActivity;
import com.anvay.cctvpartner.R;
import com.anvay.cctvpartner.adapters.OrderItemsAdapter;
import com.anvay.cctvpartner.databinding.FragmentOrdersBinding;
import com.anvay.cctvpartner.models.OrderItem;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.OrderViewModel;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment implements OrderItemsAdapter.OrderItemsClickListener {
    private List<OrderItem> orderItemList;
    private OrderItemsAdapter adapter;
    private View loadingView;
    private OrderViewModel orderViewModel;
    private int clickedPosition;
    private Constants.OrderStatus status;
    private FragmentOrdersBinding binding;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        String title = "My Orders";
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack(title);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        loadingView = binding.loadingLayout.getRoot();
        orderItemList = new ArrayList<>();
        if (getArguments() != null) {
            String s = getArguments().getString(Constants.KEY_ORDER_STATUS);
            if (s == null)
                status = null;
            else if (s.equals(Constants.OrderStatus.SHIPPED.name))
                status = Constants.OrderStatus.SHIPPED;
            else if (s.equals(Constants.OrderStatus.RECEIVED.name))
                status = Constants.OrderStatus.RECEIVED;
            else if (s.equals(Constants.OrderStatus.PENDING.name))
                status = Constants.OrderStatus.PENDING;
            else if(s.equals(Constants.OrderStatus.CANCELLED.name))
                status = Constants.OrderStatus.CANCELLED;
        }
        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        RecyclerView ordersRecycler = binding.ordersRecycler;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        ordersRecycler.setLayoutManager(linearLayoutManager);
        adapter = new OrderItemsAdapter(orderItemList, this);
        ordersRecycler.setAdapter(adapter);
        if (status == null)
            getAllData();
        else getData();
        orderViewModel.getLiveOrderItem().observe(requireActivity(), orderItem -> {
            adapter.notifyItemChanged(clickedPosition, orderItem);
        });
        return binding.getRoot();
    }

    private void getAllData() {
        loadingView.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.ORDERS_URL)
                .whereEqualTo(Constants.KEY_STORE_ID, MainActivity.storeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderItem item = doc.toObject(OrderItem.class);
                        if (item != null) {
                            item.setDocumentId(doc.getId());
                            orderItemList.add(item);
                        }
                    }
                    adapter.notifyItemInserted(0);
                    checkForEmptyRecycler();
                    loadingView.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT).show();
                    Log.e("orders", e.getMessage() + e.getCause());
                    loadingView.setVisibility(View.INVISIBLE);
                });
    }

    private void getData() {
        loadingView.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.ORDERS_URL)
                .whereEqualTo(Constants.KEY_STORE_ID, MainActivity.storeId)
                .whereEqualTo(Constants.KEY_ORDER_STATUS, status)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderItem item = doc.toObject(OrderItem.class);
                        if (item != null) {
                            item.setDocumentId(doc.getId());
                            orderItemList.add(item);
                        }
                    }
                    adapter.notifyItemInserted(0);
                    checkForEmptyRecycler();
                    loadingView.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT).show();
                    Log.e("orders", e.getMessage() + e.getCause());
                    loadingView.setVisibility(View.INVISIBLE);
                });
    }

    private void checkForEmptyRecycler() {
        if (orderItemList.isEmpty())
            binding.emptyRecyclerText.setVisibility(View.VISIBLE);
        else binding.emptyRecyclerText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onItemClicked(int position) {
        clickedPosition = position;
        orderViewModel.setOrderItem(orderItemList.get(position));
        OrderDetailsFragment fragment = new OrderDetailsFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        assert getParentFragment() != null;
        fragmentManager.beginTransaction()
                .add(getParentFragment().requireView().findViewById(R.id.fragment_container).getId(), fragment)
                .addToBackStack(null)
                .commit();
    }
}
