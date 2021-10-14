package com.anvay.cctvpartner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.anvay.cctvpartner.databinding.ActivityMainBinding;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static String storeId, zipcode, storeName, imageUrl;
    private TitleViewModel titleViewModel;
    private NavController navController;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        titleViewModel.popTitleStack();
    }

    public void setActionBarTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavigationView navView = findViewById(R.id.nav_view);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        storeId = sharedPreferences.getString(Constants.STORE_FIREBASE_ID, "");
        storeName = sharedPreferences.getString(Constants.STORE_NAME, "");
        imageUrl = sharedPreferences.getString(Constants.STORE_IMAGE_URL, null);
        zipcode = sharedPreferences.getString(Constants.STORE_ZIPCODE, "");

        if (!sharedPreferences.getBoolean(Constants.IS_LOGGED_IN, false)) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        } else if (!sharedPreferences.getBoolean(Constants.IS_PROFILE_DONE, false)) {
            Intent i = new Intent(this, ProfileActivity.class);
            startActivity(i);
            finish();
        }
        titleViewModel = new ViewModelProvider(this).get(TitleViewModel.class);
        titleViewModel.getTitleStack().observe(this, titleStack -> {
            if (titleStack != null && !titleStack.isEmpty())
                setActionBarTitle(titleViewModel.getTopTitle());
        });
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder().build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}