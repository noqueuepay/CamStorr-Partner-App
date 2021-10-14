package com.anvay.cctvpartner.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.cctvpartner.databinding.FragmentPostComplaintBinding;
import com.anvay.cctvpartner.models.Complaint;
import com.anvay.cctvpartner.utils.ComplaintsViewModel;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class PostComplaintFragment extends Fragment {
    private View loadingView;
    private ComplaintsViewModel complaintsViewModel;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack("Post a new Complaint");
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentPostComplaintBinding binding = FragmentPostComplaintBinding.inflate(inflater, container, false);
        Spinner titleSpinner = binding.titleSpinner;
        loadingView = binding.loadingLayout.getRoot();
        EditText descriptionTextBox = binding.descriptionTextBox;
        complaintsViewModel = new ViewModelProvider(requireActivity()).get(ComplaintsViewModel.class);
        Button submitButton = binding.submitButton;
        SharedPreferences sharedPreferences = requireActivity()
                .getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString(Constants.STORE_FIREBASE_ID, "default");
        submitButton.setOnClickListener(view -> {
            String title = titleSpinner.getSelectedItem().toString();
            String description = descriptionTextBox.getText().toString();
            if (title.isEmpty() || description.isEmpty())
                Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            else
                uploadData(userId, title, description);
        });
        return binding.getRoot();
    }

    private void uploadData(String userId, String complaintCategory, String description) {
        loadingView.setVisibility(View.VISIBLE);
        Random rnd = new Random();
        String ticketNumber = String.valueOf(100000 + rnd.nextInt(900000));
        Complaint complaint = new Complaint(complaintCategory, ticketNumber, userId, description);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_COMPLAINTS_URL)
                .add(complaint)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Successfully Submitted", Toast.LENGTH_SHORT).show();
                    complaintsViewModel.setPostedComplaint(complaint);
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error uploading data", Toast.LENGTH_SHORT).show();
                    loadingView.setVisibility(View.INVISIBLE);
                });
    }
}