package com.anvay.cctvpartner.fragments;

import android.content.Context;
import android.os.Bundle;
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
import com.anvay.cctvpartner.databinding.FragmentPostTaskDetailsBinding;
import com.anvay.cctvpartner.databinding.LayoutPlaceBidBinding;
import com.anvay.cctvpartner.models.Bid;
import com.anvay.cctvpartner.models.Task;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.TaskViewModel;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class PostedTaskDetailsFragment extends Fragment {
    private FragmentPostTaskDetailsBinding binding;
    private TextView typeDisplay, brandDisplay, wireLenDisplay, quantityDisplay, descriptionDisplay,
            hardDiskTypeDisplay, placedBidAmountDisplay,
            cameraTypeDisplay, dateDisplay;
    private Button placeBidButton, bidConfirm, bidCancel;
    private EditText bidAmountDisplay;
    private Task currentTask;
    private View placeBidLayout, loadingView, bidPlacedView;
    private TaskViewModel taskViewModel;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack("Project Details");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPostTaskDetailsBinding.inflate(inflater, container, false);
        getUI();
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        currentTask = taskViewModel.getPostedTask().getValue();
        placeBidButton.setOnClickListener(view -> openLayout());
        bidCancel.setOnClickListener(view -> placeBidLayout.setVisibility(View.INVISIBLE));
        updateUI(currentTask);
        bidConfirm.setOnClickListener(view -> {
            double bidAmount;
            if (!bidAmountDisplay.getText().toString().isEmpty()) {
                bidAmount = Double.parseDouble(bidAmountDisplay.getText().toString());
                uploadData(bidAmount);
            } else Toast.makeText(getContext(), "Enter amount to bid", Toast.LENGTH_SHORT).show();
        });
        return binding.getRoot();
    }

    private void uploadData(double bidAmount) {
        if (currentTask == null)
            return;
        loadingView.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Bid bid = new Bid(MainActivity.storeName, bidAmount, MainActivity.storeId, MainActivity.imageUrl);
        db.collection(Constants.BASE_TASKS_URL)
                .document(currentTask.getTaskId())
                .update(Constants.KEY_BIDS_ARRAY, FieldValue.arrayUnion(bid))
                .addOnSuccessListener(unused -> {
                    placeBidLayout.setVisibility(View.INVISIBLE);
                    taskViewModel.updateTaskBids(bid);
                    placeBidButton.setVisibility(View.INVISIBLE);
                    bidPlacedView.setVisibility(View.VISIBLE);
                    placedBidAmountDisplay.setText(String.format("₹ %s", bidAmount));
                    loadingView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Bid Placed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    loadingView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                });
    }

    private void openLayout() {
        placeBidLayout.setVisibility(View.VISIBLE);
    }

    private void updateUI(Task task) {
        if (task == null)
            return;
        loadingView.setVisibility(View.VISIBLE);
        typeDisplay.setText(task.getTypeOfService());
        brandDisplay.setText(task.getCameraBrand());
        dateDisplay.setText(task.getDate());
        descriptionDisplay.setText(task.getDescription());
        hardDiskTypeDisplay.setText(task.getHardDiskType());
        cameraTypeDisplay.setText(task.getCameraType());
        wireLenDisplay.setText(String.valueOf(task.getWireLength()));
        quantityDisplay.setText(String.valueOf(task.getNumberOfCameras()));
        checkIfBidPlaced();
    }

    private void checkIfBidPlaced() {
        Double bidAmount = currentTask.isBidPlaced(MainActivity.storeId);
        if (bidAmount != null) {
            bidPlacedView.setVisibility(View.VISIBLE);
            placeBidButton.setVisibility(View.INVISIBLE);
            placedBidAmountDisplay.setText(String.valueOf(bidAmount));
        } else {
            bidPlacedView.setVisibility(View.INVISIBLE);
            placeBidButton.setVisibility(View.VISIBLE);
        }
        loadingView.setVisibility(View.INVISIBLE);
    }

    private void getUI() {
        typeDisplay = binding.typeDisplay;
        quantityDisplay = binding.quantityDisplay;
        descriptionDisplay = binding.descriptionDisplay;
        hardDiskTypeDisplay = binding.hardDiskDisplay;
        cameraTypeDisplay = binding.cameraTypeDisplay;
        brandDisplay = binding.brandDisplay;
        wireLenDisplay = binding.wireLenDisplay;
        dateDisplay = binding.dateDisplay;
        LayoutPlaceBidBinding placeBidBinding = binding.placeBidLayout;
        placeBidLayout = placeBidBinding.getRoot();
        bidAmountDisplay = placeBidBinding.bidAmountDisplay;
        bidCancel = placeBidBinding.bidCancel;
        bidConfirm = placeBidBinding.bidConfirm;
        placedBidAmountDisplay = binding.placedBidAmountDisplay;
        bidPlacedView = binding.bidPlacedView;
        placeBidButton = binding.placeBidButton;
        loadingView = binding.loadingLayout.getRoot();
    }
}
