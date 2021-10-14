package com.anvay.cctvpartner.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.cctvpartner.databinding.FragmentOngoingTaskDetailsBinding;
import com.anvay.cctvpartner.models.OngoingTask;
import com.anvay.cctvpartner.models.Task;
import com.anvay.cctvpartner.utils.TaskViewModel;
import com.anvay.cctvpartner.utils.TitleViewModel;

import org.jetbrains.annotations.NotNull;

public class OngoingTaskDetailsFragment extends Fragment {
    private FragmentOngoingTaskDetailsBinding binding;
    private OngoingTask ongoingTask;
    private View loadingView;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        String title = "Project Details";
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack(title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOngoingTaskDetailsBinding.inflate(inflater, container, false);
        TaskViewModel taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        ongoingTask = taskViewModel.getOngoingTask();
        initUI();
        loadingView = binding.loadingLayout.getRoot();
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        Task task = ongoingTask.getTask();
        binding.typeDisplay.setText(task.getTypeOfService());
        binding.postedDateDisplay.setText(task.getDate());
        binding.brandDisplay.setText(task.getCameraBrand());
        binding.cameraTypeDisplay.setText(task.getCameraType());
        binding.hardDiskDisplay.setText(task.getHardDiskType());
        binding.wireLenDisplay.setText(task.getWireLength() + " meters");
        binding.quantityDisplay.setText(String.valueOf(task.getNumberOfCameras()));
        binding.descriptionDisplay.setText(task.getDescription());
        binding.storeNameDisplay.setText(ongoingTask.getStoreName());
        binding.orderIdDisplay.setText(ongoingTask.getOrderId());
        binding.transactionNoDisplay.setText(ongoingTask.getTransactionId());
        binding.paymentDateDisplay.setText(ongoingTask.getPaymentDate());
        binding.taskDateDisplay.setText(ongoingTask.getDateOfTask());
        binding.addressDisplay.setText(ongoingTask.getCustomerAddress());
    }
}
