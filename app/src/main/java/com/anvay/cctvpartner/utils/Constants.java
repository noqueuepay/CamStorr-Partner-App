package com.anvay.cctvpartner.utils;

import android.annotation.SuppressLint;
import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Constants {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static final String SHARED_PREF = "cctvCustomerApp",
            IS_PAYMENT_DONE = "isPaymentDone",
            LAST_LATITUDE = "lastLatitude",
            LAST_LONGITUDE = "lastLongitude",
            STORE_FIREBASE_ID = "firebaseId",
            STORE_ZIPCODE = "zipcode",
            STORE_NAME = "storeName",
            MERCHANT_NAME = "merchantName",
            STORE_CITY = "city",
            STORE_EMAIL = "email",
            STORE_IMAGE_URL = "imageUrl",
            STORE_ADDRESS = "address",
            MOBILE_NUMBER = "mobileNumber",
            IS_LOGGED_IN = "isLoggedIn",
            IS_PROFILE_DONE = "isProfileDone",
            KEY_TYPE_OF_SERVICE = "typeOfService",
            KEY_WALLET_BALANCE = "walletBalance",
            KEY_PRODUCT_CAMERA = "Camera",
            KEY_PRODUCT_ACCESSORY = "Accessory",
            KEY_PRODUCT_TYPE = "productType",
            KEY_EDIT_MODE = "editMode",
            KEY_COMPLAINT_OBJECT = "complaintObject";

    public static final String
            BASE_STORES_URL = "stores/",
            BASE_COMPLAINTS_URL = "storeComplaints",
            BASE_TASKS_URL = "liveProjects",
            ALL_PRODUCTS_URL = "allProducts",
            WALLET_URL = "storeWallets",
            KEY_STORE_ID = "storeId",
            KEY_ZIP_CODE = "zipcode",
            KEY_TIMESTAMP = "timestamp",
            KEY_BIDS_ARRAY = "bids",
            KEY_ORDER_STATUS = "status",
            ORDERS_URL = "allOrders",
            BASE_ONGOING_TASKS_URL = "ongoingProjects",
            KEY_IS_TASK_COMPLETED = "completed";
    public static final int
            TIMESTAMP_DATE_SHORT = 1,
            TIMESTAMP_DATE_LONG = 2,
            TIMESTAMP_DATE_TIME = 3;

    public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static String getDate(int type) {
        Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat();
        String pattern;
        switch (type) {
            case TIMESTAMP_DATE_SHORT:
                pattern = "dd-MMM";
                break;
            case TIMESTAMP_DATE_LONG:
                pattern = "dd-MMM-yy";
                break;
            case TIMESTAMP_DATE_TIME:
                pattern = "dd-MMM HH:mm";
                break;
            default:
                pattern = "yyyy-MM-dd HH:mm:ss";
        }
        df.applyPattern(pattern);
        return df.format(date);
    }

    public enum OrderStatus {
        PENDING("Pending", Color.RED), SHIPPED("Shipped", Color.parseColor("#008b00")),
        RECEIVED("Completed", Color.GRAY), CANCELLED("Cancelled",Color.BLACK);
        public final String name;
        public final int color;

        OrderStatus(String label, int color) {
            this.name = label;
            this.color = color;
        }
    }
}
