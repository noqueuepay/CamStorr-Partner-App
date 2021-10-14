package com.anvay.cctvpartner.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.cctvpartner.R;
import com.anvay.cctvpartner.databinding.FragmentProfileBinding;
import com.anvay.cctvpartner.models.Store;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class ProfileFragment extends Fragment {
    private View loadingLayout;
    private EditText storeNameDisplay, mobileNumberDisplay, storeAddressDisplay, zipCodeDisplay,
            merchantNameDisplay, emailDisplay;
    private Button editProfile, updateProfile;
    private ShapeableImageView storeImageDisplay;
    private ImageButton imageCapture;
    private String storeName, storeAddress, imageUrl, storeId, mobileNumber, email, zipcode, merchantName,
            imageUrlNew;
    private byte[] byteArray;
    private Store store;
    private FragmentProfileBinding binding;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack("Your Profile");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        initUI();
        setupFields(false);
        loadingLayout.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        storeId = user.getUid();
        fetchProfile(storeId);
        editProfile.setOnClickListener(view -> setupFields(true));
        updateProfile.setOnClickListener(view -> {
            storeName = storeNameDisplay.getText().toString();
            storeAddress = storeAddressDisplay.getText().toString();
            merchantName = merchantNameDisplay.getText().toString();
            email = emailDisplay.getText().toString();
            zipcode = zipCodeDisplay.getText().toString();
            if (!Constants.validateEmail(email)) {
                Toast.makeText(getContext(), "Invalid email", Toast.LENGTH_SHORT).show();
                loadingLayout.setVisibility(View.INVISIBLE);
            } else if (storeName.isEmpty() || storeAddress.isEmpty() || merchantName.isEmpty() || email.isEmpty() || zipcode.isEmpty())
                Toast.makeText(getContext(), "Fill all details", Toast.LENGTH_SHORT).show();
            else if (byteArray != null)
                postPicture();
            else
                updateOnFirestore();
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
            if (gallery.resolveActivity(requireActivity().getPackageManager()) != null) {
                imageLauncher.launch(gallery);
            }
        });
        storeImageDisplay.setOnClickListener(view -> imageCapture.performClick());
        return binding.getRoot();
    }

    private void fetchProfile(String storeId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_STORES_URL)
                .document(storeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    store = documentSnapshot.toObject(Store.class);
                    updateUI();
                    loadingLayout.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load details", Toast.LENGTH_SHORT).show();
                    loadingLayout.setVisibility(View.INVISIBLE);
                });
    }

    private void updateUI() {
        storeName = store.getStoreName();
        mobileNumber = store.getMobileNumber();
        storeAddress = store.getAddress();
        zipcode = store.getZipcode();
        email = store.getEmail();
        merchantName = store.getMerchantName();
        imageUrl = store.getImageUrl();
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.loading_bar)
                .into(storeImageDisplay);
        storeNameDisplay.setText(storeName);
        emailDisplay.setText(email);
        mobileNumberDisplay.setText(mobileNumber);
        storeAddressDisplay.setText(storeAddress);
        zipCodeDisplay.setText(zipcode);
        merchantNameDisplay.setText(merchantName);
    }

    private void setupFields(boolean editMode) {
        storeNameDisplay.setEnabled(editMode);
        storeAddressDisplay.setEnabled(editMode);
        merchantNameDisplay.setEnabled(editMode);
        storeImageDisplay.setEnabled(editMode);
        zipCodeDisplay.setEnabled(editMode);
        //  emailDisplay.setEnabled(editMode);
        if (editMode) {
            editProfile.setVisibility(View.INVISIBLE);
            imageCapture.setVisibility(View.VISIBLE);
            updateProfile.setVisibility(View.VISIBLE);
        } else {
            editProfile.setVisibility(View.VISIBLE);
            imageCapture.setVisibility(View.INVISIBLE);
            updateProfile.setVisibility(View.INVISIBLE);
        }
    }

    private void updateOnFirestore() {
        loadingLayout.setVisibility(View.VISIBLE);
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.STORE_ADDRESS, storeAddress);
        map.put(Constants.MERCHANT_NAME, merchantName);
        map.put(Constants.STORE_NAME, storeName);
        map.put(Constants.STORE_EMAIL, email);
        map.put(Constants.STORE_ZIPCODE, zipcode);
        if (imageUrlNew != null) {
            map.put(Constants.STORE_IMAGE_URL, imageUrlNew);
            imageUrl = imageUrlNew;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.BASE_STORES_URL)
                .document(storeId)
                .update(map)
                .addOnSuccessListener(unused -> {
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.STORE_ZIPCODE, store.getZipcode());
                    editor.putString(Constants.STORE_IMAGE_URL, store.getImageUrl());
                    editor.putString(Constants.STORE_NAME, store.getStoreName());
                    editor.apply();
                    setupFields(false);
                    loadingLayout.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
                    loadingLayout.setVisibility(View.INVISIBLE);
                });
    }

    private void initUI() {
        editProfile = binding.editProfile;
        updateProfile = binding.updateProfile;
        imageCapture = binding.storeImageCapture;
        loadingLayout = binding.loadingLayout.getRoot();
        storeNameDisplay = binding.storeNameDisplay;
        storeAddressDisplay = binding.storeAddressDisplay;
        storeImageDisplay = binding.storeImageDisplay;
        zipCodeDisplay = binding.zipcodeDisplay;
        mobileNumberDisplay = binding.mobileNumberDisplay;
        emailDisplay = binding.emailDisplay;
        merchantNameDisplay = binding.merchantNameDisplay;
    }

    private void processResult(ActivityResult result) {
        Intent data = result.getData();
        if (data != null) {
            Uri photoUri = data.getData();
            try {
                Bitmap profileImage = MediaStore.Images.Media.getBitmap(requireActivity()
                        .getContentResolver(), photoUri);
                Bitmap img = getResizedBitmap(profileImage, 150);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                img.compress(Bitmap.CompressFormat.JPEG, 70, bs);
                byteArray = bs.toByteArray();
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                storeImageDisplay.setImageBitmap(bmp);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Cannot load this image", Toast.LENGTH_SHORT).show();
            }
        }
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
                    imageUrlNew = uri.toString();
                    updateOnFirestore();
                }));
    }
}