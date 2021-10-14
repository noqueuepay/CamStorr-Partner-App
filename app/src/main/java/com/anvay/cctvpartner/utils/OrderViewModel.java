package com.anvay.cctvpartner.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.anvay.cctvpartner.models.OrderItem;

public class OrderViewModel extends ViewModel {

    private final MutableLiveData<OrderItem> orderItem = new MutableLiveData<>();

    public LiveData<OrderItem> getLiveOrderItem() {
        return orderItem;
    }

    public OrderItem getOrderItem() {
        return orderItem.getValue();
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem.setValue(orderItem);
    }

    public void setStatusShipped() {
        if (orderItem.getValue() != null) {
            this.orderItem.getValue().setStatus(Constants.OrderStatus.SHIPPED);
            orderItem.setValue(orderItem.getValue());
        }
    }
}
