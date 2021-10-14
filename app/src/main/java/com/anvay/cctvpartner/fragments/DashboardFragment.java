package com.anvay.cctvpartner.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.cctvpartner.databinding.FragmentDashboardBinding;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.TaskViewModel;
import com.anvay.cctvpartner.utils.TitleViewModel;

import org.jetbrains.annotations.NotNull;

public class DashboardFragment extends Fragment {
    private FragmentManager fragmentManager;
    private FragmentDashboardBinding binding;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.setTitleStack("Dashboard");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        fragmentManager = getChildFragmentManager();
        binding.completedProjects.setOnClickListener(view -> {
            TaskViewModel taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
            taskViewModel.setCompletedTask(true);
            setFragment(new OngoingTasksFragment());
        });
        binding.addCamera.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            ProductDetailsFragment fragment = new ProductDetailsFragment();
            bundle.putBoolean(Constants.KEY_EDIT_MODE, false);
            bundle.putString(Constants.KEY_PRODUCT_TYPE, Constants.KEY_PRODUCT_CAMERA);
            fragment.setArguments(bundle);
            setFragment(fragment);
        });
        binding.addAccessory.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            ProductDetailsFragment fragment = new ProductDetailsFragment();
            bundle.putBoolean(Constants.KEY_EDIT_MODE, false);
            bundle.putString(Constants.KEY_PRODUCT_TYPE, Constants.KEY_PRODUCT_ACCESSORY);
            fragment.setArguments(bundle);
            setFragment(fragment);
        });

        binding.viewProducts.setOnClickListener(view -> {
            ProductsFragment fragment = new ProductsFragment();
            setFragment(fragment);
        });
        binding.shippedOrders.setOnClickListener(view -> {
            OrdersFragment fragment = new OrdersFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_ORDER_STATUS, Constants.OrderStatus.SHIPPED.name);
            fragment.setArguments(bundle);
            setFragment(fragment);
        });
        binding.completedOrders.setOnClickListener(view -> {
            OrdersFragment fragment = new OrdersFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_ORDER_STATUS, Constants.OrderStatus.RECEIVED.name);
            fragment.setArguments(bundle);
            setFragment(fragment);
        });
        return binding.getRoot();
    }

    private void setFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .add(binding.fragmentContainer.getId(), fragment)
                .addToBackStack("fragment")
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}