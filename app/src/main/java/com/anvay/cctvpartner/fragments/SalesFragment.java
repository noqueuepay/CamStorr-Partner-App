package com.anvay.cctvpartner.fragments;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.cctvpartner.MainActivity;
import com.anvay.cctvpartner.databinding.FragmentSalesBinding;
import com.anvay.cctvpartner.models.OrderItem;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.DatePicker;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SalesFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private TextView totalSalesDisplay, totalOrdersDisplay, averageSalesDisplay, totalQuantityDisplay,
            startDateLabel, endDateLabel;
    private Button startDateDisplay, endDateDisplay, calculateSales;
    private String storeId;
    private View loadingView;
    private int offset = 0;
    private Date startDate, endDate;
    private boolean isStartButton = true;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack("My Sales");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentSalesBinding binding = FragmentSalesBinding.inflate(inflater, container, false);
        Spinner categorySpinner = binding.salesCategorySpinner;
        loadingView = binding.loadingLayout.getRoot();
        totalSalesDisplay = binding.totalSales;
        startDateDisplay = binding.startDateDisplay;
        calculateSales = binding.calculate;
        endDateDisplay = binding.endDateDisplay;
        startDateLabel = binding.startDateLabel;
        endDateLabel = binding.endDateLabel;
        totalOrdersDisplay = binding.totalOrders;
        averageSalesDisplay = binding.averageSales;
        totalQuantityDisplay = binding.totalQuantity;
        storeId = MainActivity.storeId;
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < 3) {
                    fetchData(getStartTimestamp(i), getEndTimestamp(startDate));
                    displayCustomDates(4);
                } else if (i == 3) {
                    fetchAllData();
                    displayCustomDates(4);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    displayCustomDates(0);
                } else
                    Toast.makeText(getContext(), "This option is not available on your device", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                fetchData(getStartTimestamp(0), getEndTimestamp(startDate));
            }
        });
        startDateDisplay.setOnClickListener(view -> {
            isStartButton = true;
            getCustomDate();
        });
        endDateDisplay.setOnClickListener(view -> {
            isStartButton = false;
            getCustomDate();
        });
        calculateSales.setOnClickListener(view -> {
            if (startDate == null || endDate == null)
                Toast.makeText(getContext(), "Select start and end date", Toast.LENGTH_SHORT).show();
            else if (endDate.before(startDate))
                Toast.makeText(getContext(), "End date must be later than start date", Toast.LENGTH_SHORT).show();
            else
                fetchData(new Timestamp(startDate), new Timestamp(endDate));
        });
        return binding.getRoot();
    }

    private void fetchAllData() {
        loadingView.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.ORDERS_URL)
                .whereEqualTo(Constants.KEY_STORE_ID, storeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalSales = 0;
                    int totalOrders = queryDocumentSnapshots.size(), totalQuantity = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderItem order = doc.toObject(OrderItem.class);
                        totalSales += order.getTotalPrice();
                        totalQuantity += order.getQuantity();
                    }
                    totalSalesDisplay.setText(String.valueOf(totalSales));
                    totalOrdersDisplay.setText(String.valueOf(totalOrders));
                    totalQuantityDisplay.setText(String.valueOf(totalQuantity));
                    double avg = totalSales / (offset == 0 ? 1 : Math.abs(offset));
                    DecimalFormat df = new DecimalFormat("#.##");
                    averageSalesDisplay.setText(df.format(avg));
                    loadingView.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    loadingView.setVisibility(View.INVISIBLE);
                    Log.e("sales", e.getMessage() + e.getCause());
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayCustomDates(int i) {
        startDateLabel.setVisibility(i);
        endDateLabel.setVisibility(i);
        startDateDisplay.setVisibility(i);
        endDateDisplay.setVisibility(i);
        calculateSales.setVisibility(i);
    }

    private Timestamp getStartTimestamp(int selectedIndex) {
        switch (selectedIndex) {
            case 0:
                offset = 0;
                break;
            case 1:
                offset = -7;
                break;
            case 2:
                offset = -30;
                break;
        }
        Calendar cal = Calendar.getInstance();
        startDate = cal.getTime();
        cal.add(Calendar.DATE, offset);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTime());
    }

    private void getCustomDate() {
        DatePicker datePicker = new DatePicker();
        datePicker.show(getChildFragmentManager(), "DATE PICK");
    }

    private Timestamp getEndTimestamp(Date startDate) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(startDate);
        cal.add(Calendar.DATE, +1);
        return new Timestamp(cal.getTime());
    }

    private void fetchData(Timestamp startTimestamp, Timestamp endTimestamp) {
        loadingView.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.ORDERS_URL)
                .whereEqualTo(Constants.KEY_STORE_ID, storeId)
                .whereGreaterThan("timestamp", startTimestamp)
                .whereLessThan("timestamp", endTimestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalSales = 0;
                    int totalOrders = queryDocumentSnapshots.size(), totalQuantity = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderItem order = doc.toObject(OrderItem.class);
                        totalSales += order.getTotalPrice();
                        totalQuantity += order.getQuantity();
                    }
                    totalSalesDisplay.setText(String.valueOf(totalSales));
                    totalOrdersDisplay.setText(String.valueOf(totalOrders));
                    totalQuantityDisplay.setText(String.valueOf(totalQuantity));
                    double avg = totalSales / (offset == 0 ? 1 : Math.abs(offset));
                    DecimalFormat df = new DecimalFormat("#.##");
                    averageSalesDisplay.setText(df.format(avg));
                    loadingView.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    loadingView.setVisibility(View.INVISIBLE);
                    Log.e("sales", e.getMessage() + e.getCause());
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        DateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        if (isStartButton) {
            startDate = cal.getTime();
            startDateDisplay.setText(formatter.format(startDate));
        } else {
            endDate = cal.getTime();
            endDateDisplay.setText(formatter.format(endDate));
        }
    }
}