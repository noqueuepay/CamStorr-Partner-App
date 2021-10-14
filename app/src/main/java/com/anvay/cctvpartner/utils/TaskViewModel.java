package com.anvay.cctvpartner.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.anvay.cctvpartner.models.Bid;
import com.anvay.cctvpartner.models.OngoingTask;
import com.anvay.cctvpartner.models.Task;

public class TaskViewModel extends ViewModel {

    private final MutableLiveData<Task> postedTask = new MutableLiveData<>();

    private OngoingTask ongoingTask;
    private boolean completedTask;

    public LiveData<Task> getPostedTask() {
        return postedTask;
    }

    public void setPostedTask(Task task) {
        postedTask.setValue(task);
    }

    public OngoingTask getOngoingTask() {
        return ongoingTask;
    }

    public void setOngoingTask(OngoingTask ongoingTask) {
        this.ongoingTask = ongoingTask;
    }

    public boolean isCompletedTask() {
        return completedTask;
    }

    public void setCompletedTask(boolean completedTask) {
        this.completedTask = completedTask;
    }

    public void updateTaskBids(Bid bid) {
        if (postedTask.getValue() != null) {
            this.postedTask.getValue().addBid(bid);
            postedTask.setValue(postedTask.getValue());
        }
    }
}
