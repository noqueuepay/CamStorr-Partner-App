package com.anvay.cctvpartner.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anvay.cctvpartner.MainActivity;
import com.anvay.cctvpartner.R;
import com.anvay.cctvpartner.adapters.PostedTasksAdapter;
import com.anvay.cctvpartner.databinding.FragmentPostedTasksBinding;
import com.anvay.cctvpartner.models.Task;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.TaskViewModel;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PostedTasksFragment extends Fragment implements PostedTasksAdapter.PostedTaskClickListener {
    private List<Task> postedTasks;
    private PostedTasksAdapter adapter;
    private View loadingLayout;
    private TaskViewModel taskViewModel;
    private int clickedPosition;
    private FragmentPostedTasksBinding binding;
    private NavController navController;
    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack("Live Projects");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPostedTasksBinding.inflate(inflater, container, false);
        RecyclerView postedTasksRecycler = binding.postedTasksRecycler;
        loadingLayout = binding.loadingLayout.getRoot();
        postedTasks = new ArrayList<>();
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        postedTasksRecycler.setLayoutManager(linearLayoutManager);
        adapter = new PostedTasksAdapter(postedTasks, this);
        taskViewModel.getPostedTask().observe(requireActivity(), task ->
                adapter.notifyItemChanged(clickedPosition, task));
        postedTasksRecycler.setAdapter(adapter);
        getData();
        return binding.getRoot();
    }

    private void getData() {
        loadingLayout.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_TASKS_URL)
                .whereEqualTo(Constants.KEY_ZIP_CODE, MainActivity.zipcode)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingLayout.setVisibility(View.INVISIBLE);
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Task task = snapshot.toObject(Task.class);
                        assert task != null;
                        task.setTaskId(snapshot.getId());
                        postedTasks.add(task);
                        adapter.notifyItemInserted(adapter.getItemCount() - 1);
                    }
                    checkForEmptyRecycler();
                })
                .addOnFailureListener(e -> {
                    loadingLayout.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkForEmptyRecycler() {
        if (postedTasks.isEmpty())
            binding.emptyRecyclerText.setVisibility(View.VISIBLE);
        else binding.emptyRecyclerText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onTaskItemClicked(int position) {
        clickedPosition = position;
        FragmentManager fragmentManager = getParentFragmentManager();
        PostedTaskDetailsFragment fragment = new PostedTaskDetailsFragment();
        taskViewModel.setPostedTask(postedTasks.get(position));
        assert getParentFragment() != null;
        fragmentManager.beginTransaction()
                .add(getParentFragment().requireView().findViewById(R.id.fragment_container).getId(),
                        fragment)
                .addToBackStack("taskDetails")
                .commit();
    }
}
