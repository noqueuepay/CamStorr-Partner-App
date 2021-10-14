package com.anvay.cctvpartner.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayDeque;
import java.util.Deque;

public class TitleViewModel extends ViewModel {
    private final MutableLiveData<Deque<String>> titleStack = new MutableLiveData<>();

    public LiveData<Deque<String>> getTitleStack() {
        return titleStack;
    }

    public void setTitleStack(String firstTitle) {
        titleStack.setValue(new ArrayDeque<>());
        if (titleStack.getValue() != null)
            titleStack.getValue().push(firstTitle);
    }

    public void addToTitleStack(String title) {
        if (titleStack.getValue() != null) {
            titleStack.getValue().push(title);
            titleStack.setValue(titleStack.getValue());
        }
    }

    public void popTitleStack() {
        if (titleStack.getValue() != null && titleStack.getValue().size() > 1) {
            titleStack.getValue().pop();
            titleStack.setValue(titleStack.getValue());
        }
    }

    public String getTopTitle() {
        if (titleStack.getValue() != null)
            return titleStack.getValue().peek();
        return "Home";
    }

}
