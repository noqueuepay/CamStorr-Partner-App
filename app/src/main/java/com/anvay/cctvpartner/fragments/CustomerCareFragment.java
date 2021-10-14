package com.anvay.cctvpartner.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anvay.cctvpartner.MainActivity;
import com.anvay.cctvpartner.R;
import com.anvay.cctvpartner.adapters.ComplaintsAdapter;
import com.anvay.cctvpartner.databinding.FragmentCustomerCareBinding;
import com.anvay.cctvpartner.models.Complaint;
import com.anvay.cctvpartner.utils.ComplaintsViewModel;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomerCareFragment extends Fragment implements ComplaintsAdapter.ComplaintsClickListener {
    private View loadingView;
    private ComplaintsAdapter adapter;
    private List<Complaint> complaintList;
    private TextView emptyComplaintsInfo;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        String title = "Customer Care";
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack(title);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentCustomerCareBinding binding = FragmentCustomerCareBinding.inflate(inflater, container, false);
        RecyclerView complaintsRecycler = binding.complaintsRecycler;
        FloatingActionButton newComplaintFab = binding.newComplaintFab;
        emptyComplaintsInfo = binding.emptyComplaintsInfo;
        loadingView = binding.loadingLayout.getRoot();
        ComplaintsViewModel complaintsViewModel = new ViewModelProvider(requireActivity()).get(ComplaintsViewModel.class);
        loadingView.setVisibility(View.VISIBLE);
        complaintList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new ComplaintsAdapter(complaintList, this);
        complaintsRecycler.setLayoutManager(linearLayoutManager);
        complaintsRecycler.setAdapter(adapter);
        getData();
        complaintsViewModel.getPostedComplaint().observe(getViewLifecycleOwner(), complaint -> {
            complaintList.add(0, complaint);
            adapter.notifyItemInserted(0);
            checkForEmptyRecycler();
        });
        newComplaintFab.setOnClickListener(view -> launchPostComplaint());
        return binding.getRoot();
    }

    private void getData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_COMPLAINTS_URL)
                .whereEqualTo(Constants.KEY_STORE_ID, MainActivity.storeId)
                .orderBy(Constants.KEY_TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        complaintList.add(documentSnapshot.toObject(Complaint.class));
                    }
                    adapter.notifyItemInserted(0);
                    checkForEmptyRecycler();
                    loadingView.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    loadingView.setVisibility(View.INVISIBLE);
                    Log.e("complaint", e.getMessage() + e.getCause());
                    Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkForEmptyRecycler() {
        if (complaintList == null ||
                complaintList.isEmpty())
            emptyComplaintsInfo.setVisibility(View.VISIBLE);
        else emptyComplaintsInfo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onComplaintClicked(int position) {
        FragmentManager fragmentManager = getParentFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_COMPLAINT_OBJECT, complaintList.get(position));
        ComplaintDetailsFragment fragment = new ComplaintDetailsFragment();
        fragment.setArguments(bundle);
        assert getParentFragment() != null;
        fragmentManager.beginTransaction()
                .add(getParentFragment().requireView().findViewById(R.id.account_fragment_host).getId(), fragment)
                .addToBackStack("complaintDetails")
                .commit();
    }

    private void launchPostComplaint() {
        FragmentManager fragmentManager = getParentFragmentManager();
        assert getParentFragment() != null;
        fragmentManager.beginTransaction()
                .add(getParentFragment().requireView().findViewById(R.id.account_fragment_host).getId(), new PostComplaintFragment())
                .addToBackStack("postComplaint")
                .commit();
    }
}