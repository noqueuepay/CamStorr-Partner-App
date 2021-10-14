package com.anvay.cctvpartner.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.anvay.cctvpartner.MainActivity;
import com.anvay.cctvpartner.R;
import com.anvay.cctvpartner.databinding.FragmentProductDetailsBinding;
import com.anvay.cctvpartner.models.ProductItem;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.ProductItemViewModel;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProductDetailsFragment extends Fragment {
    private FragmentProductDetailsBinding binding;
    private ImageView imageDisplay;
    private View loadingView;
    private byte[] byteArray;
    private String id, name, brand, details, subCategory, imageUrl, sku, sla, mName, mAddress, gst = "18%";
    private int stockQuantity;
    //private double price, discount;
    private EditText idDisplay, nameDisplay, brandDisplay, detailsDisplay, categoryDisplay,
            skuDisplay, slaDisplay, mNameDisplay, mAddressDisplay, quantityDisplay, priceDisplay, discountDisplay;
    private Spinner gstSpinner, subCategorySpinner;
    private Bitmap bmp;
    private FirebaseFirestore db;
    private String category = Constants.KEY_PRODUCT_CAMERA;
    private ProductItem currentItem;
    private boolean editMode = false, updateMode = false;
    private String[] gstList, subCategoryList;
    private Button addImageButton, addItemButton, editItemButton, updateItemButton;
    private ProductItemViewModel productItemViewModel;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack("Product Details");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            category = getArguments().getString(Constants.KEY_PRODUCT_TYPE);
            editMode = getArguments().getBoolean(Constants.KEY_EDIT_MODE);
        }
        binding.gstSpinner.setEnabled(false);
        db = FirebaseFirestore.getInstance();
        productItemViewModel = new ViewModelProvider(requireActivity()).get(ProductItemViewModel.class);
        if (editMode)
            currentItem = productItemViewModel.getProductItem();
        String[] cameraList = requireContext().getResources().getStringArray(R.array.camera_sub_category);
        String[] accessoryList = requireContext().getResources().getStringArray(R.array.accessory_sub_category);
        gstList = requireContext().getResources().getStringArray(R.array.gst_list);
        subCategoryList = cameraList;
        if (category != null && category.equals(Constants.KEY_PRODUCT_ACCESSORY))
            subCategoryList = accessoryList;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, subCategoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        initUI();
        subCategorySpinner.setAdapter(adapter);
        ActivityResultLauncher<Intent> imageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK)
                        processResult(result);
                }
        );

        addImageButton.setOnClickListener(view -> {
            Intent gallery = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            gallery.setType("image/*");
            if (gallery.resolveActivity(requireActivity().getPackageManager()) != null) {
                imageLauncher.launch(gallery);
            }
        });
        updateItemButton.setOnClickListener(view -> {
            checkData();
        });
        editItemButton.setOnClickListener(view -> {
            updateMode = true;
            setupViews(true);
        });
        addItemButton.setOnClickListener(view -> checkData());
        return binding.getRoot();
    }

    private void checkData() {
        if (bmp == null && !editMode) {
            Toast.makeText(getContext(), "Upload store image", Toast.LENGTH_SHORT).show();
            return;
        }
        id = idDisplay.getText().toString();
        name = nameDisplay.getText().toString();
        mName = mNameDisplay.getText().toString();
        mAddress = mAddressDisplay.getText().toString();
        brand = brandDisplay.getText().toString();
        details = detailsDisplay.getText().toString();
        sku = skuDisplay.getText().toString();
        sla = slaDisplay.getText().toString();
        //gst = gstSpinner.getSelectedItem().toString();
        String stockQuantityString = quantityDisplay.getText().toString();
        //String discountString = discountDisplay.getText().toString();
        //String priceString = priceDisplay.getText().toString();
        subCategory = subCategorySpinner.getSelectedItem().toString();
        if (name.isEmpty() || id.isEmpty() || mName.isEmpty() || mAddress.isEmpty() || brand.isEmpty() || details
                .isEmpty()) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (!stockQuantityString.isEmpty())
                stockQuantity = Integer.parseInt(quantityDisplay.getText().toString());
//            if (!discountString.isEmpty())
//                discount = Double.parseDouble(discountString);
//            price = Double.parseDouble(priceDisplay.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Quantity should contain only numbers", Toast.LENGTH_SHORT).show();
            return;
        }
        if (editMode && bmp == null) {
            imageUrl = currentItem.getImageUrl();

            updateData();
        } else postPicture();
    }

    private void setupViews(boolean enabled) {
        nameDisplay.setEnabled(enabled);
        detailsDisplay.setEnabled(enabled);
        //gstSpinner.setEnabled(enabled);
        subCategorySpinner.setEnabled(enabled);
        mNameDisplay.setEnabled(enabled);
        brandDisplay.setEnabled(enabled);
        mAddressDisplay.setEnabled(enabled);
        quantityDisplay.setEnabled(enabled);
        //priceDisplay.setEnabled(enabled);
        //discountDisplay.setEnabled(enabled);
        skuDisplay.setEnabled(enabled);
        slaDisplay.setEnabled(enabled);
        idDisplay.setEnabled(false);
        if (editMode && !updateMode) {
            editItemButton.setVisibility(View.VISIBLE);
            addImageButton.setVisibility(View.INVISIBLE);
            addItemButton.setVisibility(View.INVISIBLE);
            updateItemButton.setVisibility(View.INVISIBLE);
        } else if (updateMode) {
            addImageButton.setVisibility(View.VISIBLE);
            editItemButton.setVisibility(View.INVISIBLE);
            addItemButton.setVisibility(View.INVISIBLE);
            updateItemButton.setVisibility(View.VISIBLE);
        } else {
            editItemButton.setVisibility(View.INVISIBLE);
            addImageButton.setVisibility(View.VISIBLE);
            addItemButton.setVisibility(View.VISIBLE);
            updateItemButton.setVisibility(View.INVISIBLE);
            idDisplay.setEnabled(true);
        }
    }

    private void updateData() {
        loadingView.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("brand", brand);
        map.put("details", details);
        map.put("subCategory", subCategory);
        map.put("imageUrl", imageUrl);
        map.put("sku", sku);
        map.put("sla", sla);
        map.put("mName", mName);
        map.put("mAddress", mAddress);
        map.put("stockQuantity", stockQuantity);
        // map.put("price", price);
        //map.put("discount", discount);
        //map.put("gst", gst);
        productItemViewModel.update(name, brand, details, subCategory,
                imageUrl, sku, sla, mName, mAddress, stockQuantity, 0.0, 0.0, gst);

        db.collection(Constants.ALL_PRODUCTS_URL)
                .document(currentItem.getDocumentId())
                .update(map)
                .addOnSuccessListener(unused -> {
                    loadingView.setVisibility(View.INVISIBLE);
                    updateMode = false;
                    setupViews(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show();
                    loadingView.setVisibility(View.INVISIBLE);
                });
    }

    private void setData() {
        loadingView.setVisibility(View.VISIBLE);
        ProductItem productItem = new ProductItem(id, name, brand, details, category, subCategory,
                imageUrl, MainActivity.storeName, MainActivity.storeId, sku, sla, mName, mAddress, stockQuantity, 0.00, 0.00, gst);
        db.collection(Constants.ALL_PRODUCTS_URL)
                .document()
                .set(productItem)
                .addOnSuccessListener(unused -> {
                    FragmentManager fragmentManager = getParentFragmentManager();
                    Toast.makeText(getContext(), "Product Added", Toast.LENGTH_SHORT).show();
                    fragmentManager.popBackStackImmediate();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show();
                    loadingView.setVisibility(View.INVISIBLE);
                });
    }

    private void initUI() {
        idDisplay = binding.idDisplay;
        nameDisplay = binding.nameDisplay;
        categoryDisplay = binding.categoryDisplay;
        subCategorySpinner = binding.subCategorySpinner;
        mNameDisplay = binding.mNameDisplay;
        mAddressDisplay = binding.mAddressDisplay;
        imageDisplay = binding.imageDisplay;
        gstSpinner = binding.gstSpinner;
        quantityDisplay = binding.quantityDisplay;
        priceDisplay = binding.priceDisplay;
        discountDisplay = binding.discountDisplay;
        brandDisplay = binding.brandDisplay;
        slaDisplay = binding.slaDisplay;
        detailsDisplay = binding.detailsDisplay;
        skuDisplay = binding.skuDisplay;
        addImageButton = binding.addImageButton;
        addItemButton = binding.addItem;
        updateItemButton = binding.updateItem;
        editItemButton = binding.editItem;
        loadingView = binding.loading.getRoot();
        categoryDisplay.setText(category);

        if (currentItem != null && editMode) {
            idDisplay.setText(currentItem.getId());
            nameDisplay.setText(currentItem.getName());
            categoryDisplay.setText(currentItem.getCategory());
            subCategorySpinner.setSelection(getSubCategoryItem());
            gstSpinner.setSelection(getGstItem());
            Picasso.get()
                    .load(currentItem.getImageUrl())
                    .placeholder(R.drawable.loading_bar)
                    .into(imageDisplay);
            imageDisplay.setVisibility(View.VISIBLE);
            quantityDisplay.setText(String.valueOf(currentItem.getStockQuantity()));
            priceDisplay.setText(String.valueOf(currentItem.getPrice()));
            discountDisplay.setText(String.valueOf(currentItem.getDiscount()));
            slaDisplay.setText(currentItem.getSla());
            detailsDisplay.setText(currentItem.getDetails());
            brandDisplay.setText(currentItem.getBrand());
            skuDisplay.setText(currentItem.getSku());
            mNameDisplay.setText(currentItem.getmName());
            mAddressDisplay.setText(currentItem.getmAddress());
            setupViews(false);
        }
    }

    private int getGstItem() {
        String selected = currentItem.getGst();
        return Arrays.asList(gstList).indexOf(selected);
    }

    private int getSubCategoryItem() {
        String selected = currentItem.getSubCategory();
        return Arrays.asList(subCategoryList).indexOf(selected);
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
                bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                imageDisplay.setVisibility(View.VISIBLE);
                imageDisplay.setImageBitmap(bmp);
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
        loadingView.setVisibility(View.VISIBLE);
        final StorageReference image = FirebaseStorage.getInstance().getReference().child("productImages/" + MainActivity.storeId + "/" + id);
        UploadTask uploadTask = image.putBytes(byteArray);
        uploadTask.addOnSuccessListener(taskSnapshot -> image
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    imageUrl = uri.toString();
                    if (updateMode) updateData();
                    else setData();
                }));
    }
}
