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

import com.anvay.cctvpartner.databinding.FragmentComplaintDetailsBinding;
import com.anvay.cctvpartner.models.Complaint;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.TitleViewModel;

import org.jetbrains.annotations.NotNull;

public class ComplaintDetailsFragment extends Fragment {
    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        String title = "Complaint Details";
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack(title);
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        FragmentComplaintDetailsBinding binding = FragmentComplaintDetailsBinding.inflate(inflater, container, false);
        assert getArguments() != null;
        Complaint complaint = (Complaint) getArguments().getSerializable(Constants.KEY_COMPLAINT_OBJECT);
        binding.description.setText(complaint.getDescription());
        binding.complaintCategory.setText(complaint.getComplaintCategory());
        binding.complaintDate.setText(complaint.getDate());
        binding.ticketNumber.setText("Ticket Number:" + complaint.getTicketNumber());
        return binding.getRoot();
    }
}
