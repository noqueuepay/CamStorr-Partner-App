package com.anvay.cctvpartner.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.cctvpartner.databinding.FragmentHomeBinding;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.TaskViewModel;
import com.anvay.cctvpartner.utils.TitleViewModel;

import org.jetbrains.annotations.NotNull;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.setTitleStack("Home");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        TextView walletBalanceDisplay = binding.walletBalanceDisplay;
        binding.postedProjects.setOnClickListener(view -> launchFragment(new PostedTasksFragment()));
        binding.acceptedProjects.setOnClickListener(view -> launchOngoingTaskFragment(new OngoingTasksFragment()));
        binding.shipOrders.setOnClickListener(view -> launchOrdersFragment(Constants.OrderStatus.PENDING.name));
        binding.allOrders.setOnClickListener(view -> launchOrdersFragment(null));
        binding.totalSales.setOnClickListener(view -> launchFragment(new SalesFragment()));
        binding.cancelledOrders.setOnClickListener(view -> launchOrdersFragment(Constants.OrderStatus.CANCELLED.name));
//        binding.myWallet.setOnClickListener(view -> {
//            launchFragment(new WalletFragment());
//        });
//        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
//        String walletBal = sharedPreferences.getString(Constants.KEY_WALLET_BALANCE, "0.00");
//        walletBalanceDisplay.setText("\u20b9 " + walletBal);
//        WalletViewModel walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);
//        walletViewModel.getWalletBalance().observe(requireActivity(), walletBalance -> {
//            walletBalanceDisplay.setText("\u20b9 " + walletBalance);
//            sharedPreferences.edit().putString(Constants.KEY_WALLET_BALANCE, walletBalance).apply();
//        });
        return binding.getRoot();
    }

    private void launchOngoingTaskFragment(Fragment fragment) {
        TaskViewModel taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskViewModel.setCompletedTask(false);
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .add(binding.fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void launchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .add(binding.fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    private void launchOrdersFragment(String status) {
        OrdersFragment fragment = new OrdersFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_ORDER_STATUS, status);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .add(binding.fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}