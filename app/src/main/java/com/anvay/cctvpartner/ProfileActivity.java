package com.anvay.cctvpartner;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.anvay.cctvpartner.databinding.ActivityProfileBinding;
import com.anvay.cctvpartner.models.Store;
import com.anvay.cctvpartner.models.Wallet;
import com.anvay.cctvpartner.utils.Constants;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private EditText storeNameDisplay, mobileNumberDisplay, merchantNameDisplay, storeAddressDisplay, zipCodeDisplay,
            emailDisplay, passwordDisplay;
    private Button createProfile;
    private ShapeableImageView storeImageDisplay;
    private ImageButton imageCapture;
    private View loadingLayout;
    private SharedPreferences sharedPreferences;
    private String imageUrl, storeId, mobileNumber, zipcode, storeName, storeAddress, email,
            merchantName, password;
    private byte[] byteArray;
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(this.getSupportActionBar()).setTitle("Profile");
        initUI();
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        mobileNumber = sharedPreferences.getString(Constants.MOBILE_NUMBER, "");
        storeId = sharedPreferences.getString(Constants.STORE_FIREBASE_ID, "");
        mobileNumberDisplay.setText(mobileNumber);
        createProfile.setOnClickListener(view -> {
            loadingLayout.setVisibility(View.VISIBLE);
            storeName = storeNameDisplay.getText().toString();
            storeAddress = storeAddressDisplay.getText().toString();
            zipcode = zipCodeDisplay.getText().toString();
            merchantName = merchantNameDisplay.getText().toString();
            email = emailDisplay.getText().toString();
            password = passwordDisplay.getText().toString();
            if (!Constants.validateEmail(email)) {
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
                loadingLayout.setVisibility(View.INVISIBLE);
            } else if (storeName.isEmpty() || storeAddress.isEmpty() || zipcode.isEmpty() || merchantName.isEmpty()
                    || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all details", Toast.LENGTH_SHORT).show();
                loadingLayout.setVisibility(View.INVISIBLE);
            } else if (byteArray != null)
                postPicture();
            else
                createProfile();
        });
        ActivityResultLauncher<Intent> imageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK)
                        processResult(result);
                }
        );
        imageCapture.setOnClickListener(view -> {
            Intent gallery = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            gallery.setType("image/*");
            if (gallery.resolveActivity(getPackageManager()) != null) {
                imageLauncher.launch(gallery);
            }
        });
        storeImageDisplay.setOnClickListener(view -> imageCapture.performClick());
    }

    private void processResult(ActivityResult result) {
        Intent data = result.getData();
        if (data != null) {
            Uri photoUri = data.getData();
            try {
                Bitmap profileImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                Bitmap img = getResizedBitmap(profileImage, 150);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                img.compress(Bitmap.CompressFormat.JPEG, 70, bs);
                byteArray = bs.toByteArray();
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                storeImageDisplay.setImageBitmap(bmp);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Cannot load this image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createProfile() {
        loadingLayout.setVisibility(View.VISIBLE);
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        firebaseUser.linkWithCredential(credential)
                .addOnSuccessListener(authResult -> Log.e("auth", Objects.requireNonNull(authResult.getUser()).getEmail()))
                .addOnFailureListener(e -> Log.e("auth", e.getMessage() + e.getCause()));
        Store store = new Store(storeName, merchantName, mobileNumber, zipcode, storeAddress, email, imageUrl);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.WALLET_URL)
                .document(storeId)
                .set(new Wallet(true));
        db.collection(Constants.BASE_STORES_URL)
                .document(storeId)
                .set(store)
                .addOnSuccessListener(unused -> completeSetup(store))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating profile", Toast.LENGTH_SHORT).show();
                    loadingLayout.setVisibility(View.INVISIBLE);
                });
    }

    private void completeSetup(Store store) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.IS_PROFILE_DONE, true);
        editor.putString(Constants.STORE_ZIPCODE, store.getZipcode());
        editor.putString(Constants.STORE_NAME, store.getStoreName());
        editor.putString(Constants.STORE_IMAGE_URL, store.getImageUrl());
        editor.apply();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void initUI() {
        createProfile = binding.createProfile;
        imageCapture = binding.storeImageCapture;
        loadingLayout = binding.loadingLayout.getRoot();
        storeNameDisplay = binding.storeNameDisplay;
        storeAddressDisplay = binding.storeAddressDisplay;
        storeImageDisplay = binding.storeImageDisplay;
        zipCodeDisplay = binding.zipcodeDisplay;
        mobileNumberDisplay = binding.mobileNumberDisplay;
        emailDisplay = binding.emailDisplay;
        passwordDisplay = binding.passwordDisplay;
        merchantNameDisplay = binding.merchantNameDisplay;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void postPicture() {
        loadingLayout.setVisibility(View.VISIBLE);
        final StorageReference image = FirebaseStorage
                .getInstance().getReference().child("storeImages/" + storeId);
        UploadTask uploadTask = image.putBytes(byteArray);
        uploadTask.addOnSuccessListener(taskSnapshot -> image
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    imageUrl = uri.toString();
                    createProfile();
                }));
    }
}