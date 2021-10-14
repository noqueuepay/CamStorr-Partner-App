package com.anvay.cctvpartner;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.anvay.cctvpartner.databinding.ActivityLoginBinding;
import com.anvay.cctvpartner.models.Store;
import com.anvay.cctvpartner.utils.Constants;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private View loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Button loginButton = binding.loginButton;
        loadingView = binding.loadingLayout.getRoot();
        Objects.requireNonNull(this.getSupportActionBar()).setTitle("Login");
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build());
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                this::processResult);
        loginButton.setOnClickListener(view -> signInLauncher.launch(signInIntent));
    }

    private void processResult(FirebaseAuthUIAuthenticationResult result) {
        loadingView.setVisibility(View.VISIBLE);
        if (result.getResultCode() == Activity.RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            assert user != null;
            editor.putString(Constants.MOBILE_NUMBER, user.getPhoneNumber());
            editor.putString(Constants.STORE_FIREBASE_ID, user.getUid());
            editor.putBoolean(Constants.IS_LOGGED_IN, true);
            editor.apply();
            checkIfUserExists(user.getUid());
        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
            loadingView.setVisibility(View.GONE);
        }
    }

    private void checkIfUserExists(String storeId) {
        loadingView.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_STORES_URL)
                .document(storeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        assert snapshot != null;
                        if (snapshot.exists()) {
                            Store store = snapshot.toObject(Store.class);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            assert store != null;
                            editor.putString(Constants.STORE_ZIPCODE, store.getZipcode());
                            editor.putString(Constants.STORE_NAME, store.getName());
                            editor.putString(Constants.STORE_CITY, store.getEmail());
                            editor.putString(Constants.STORE_ADDRESS, store.getAddress());
                            editor.putBoolean(Constants.IS_PROFILE_DONE, true);
                            editor.apply();
                        }
                    }
                    if (sharedPreferences.getBoolean(Constants.IS_PROFILE_DONE, false)) {
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(LoginActivity.this, ProfileActivity.class);
                        startActivity(i);
                    }
                    finish();
                });
    }
}