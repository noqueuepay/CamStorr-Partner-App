package com.anvay.cctvpartner.fragments;

import android.content.Context;
import android.os.Bundle;
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
import com.anvay.cctvpartner.adapters.OngoingTasksAdapter;
import com.anvay.cctvpartner.databinding.FragmentOngoingTasksBinding;
import com.anvay.cctvpartner.models.OngoingTask;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.TaskViewModel;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OngoingTasksFragment extends Fragment implements OngoingTasksAdapter.OngoingTasksItemClickListener {
    private List<OngoingTask> ongoingTaskList;
    private View loadingView;
    private OngoingTasksAdapter adapter;
    private RecyclerView ongoingTasksRecycler;
    private boolean isCompleted = false;
    private TaskViewModel taskViewModel;
    private FragmentOngoingTasksBinding binding;
    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        isCompleted = taskViewModel.isCompletedTask();
        String title = "Accepted Projects";
        if (isCompleted)
            title = "Completed Projects";
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack(title);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOngoingTasksBinding.inflate(inflater, container, false);
        ongoingTaskList = new ArrayList<>();
        ongoingTasksRecycler = binding.ongoingTasksRecycler;
        loadingView = binding.loadingLayout.getRoot();
        isCompleted = taskViewModel.isCompletedTask();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        ongoingTasksRecycler.setLayoutManager(linearLayoutManager);
        getData();
        return binding.getRoot();
    }

    private void getData() {
        loadingView.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_ONGOING_TASKS_URL)
                .whereEqualTo(Constants.KEY_STORE_ID, MainActivity.storeId)
                .whereEqualTo(Constants.KEY_IS_TASK_COMPLETED, isCompleted)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        OngoingTask task = documentSnapshot.toObject(OngoingTask.class);
                        if (task != null) {
                            task.setOngoingTaskId(documentSnapshot.getId());
                            ongoingTaskList.add(task);
                        }
                    }
                    adapter = new OngoingTasksAdapter(ongoingTaskList, this, isCompleted);
                    ongoingTasksRecycler.setAdapter(adapter);
                    loadingView.setVisibility(View.INVISIBLE);
                    checkForEmptyRecycler();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Loading Error", Toast.LENGTH_SHORT).show();
                    loadingView.setVisibility(View.INVISIBLE);
                });
    }
    private void checkForEmptyRecycler() {
        if (ongoingTaskList.isEmpty())
            binding.emptyRecyclerText.setVisibility(View.VISIBLE);
        else binding.emptyRecyclerText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onItemClicked(int position) {
        OngoingTaskDetailsFragment fragment = new OngoingTaskDetailsFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        taskViewModel.setOngoingTask(ongoingTaskList.get(position));
        assert getParentFragment() != null;
        fragmentManager.beginTransaction()
                .add(getParentFragment().requireView().findViewById(R.id.fragment_container).getId(), fragment)
                .addToBackStack(null)
                .commit();
    }
}
