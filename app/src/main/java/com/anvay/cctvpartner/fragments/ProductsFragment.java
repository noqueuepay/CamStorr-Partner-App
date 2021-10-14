package com.anvay.cctvpartner.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anvay.cctvpartner.MainActivity;
import com.anvay.cctvpartner.R;
import com.anvay.cctvpartner.adapters.ProductsAdapter;
import com.anvay.cctvpartner.databinding.FragmentProductsBinding;
import com.anvay.cctvpartner.models.ProductItem;
import com.anvay.cctvpartner.utils.Constants;
import com.anvay.cctvpartner.utils.ProductItemViewModel;
import com.anvay.cctvpartner.utils.TitleViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment implements ProductsAdapter.ProductItemClickListener {
    private View loadingView;
    private List<ProductItem> itemList;
    private ProductsAdapter adapter;
    private ProductItemViewModel productItemViewModel;
    private int clickedPosition;
    private FragmentProductsBinding binding;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        TitleViewModel titleViewModel = new ViewModelProvider(requireActivity()).get(TitleViewModel.class);
        titleViewModel.addToTitleStack("All Products");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductsBinding.inflate(inflater, container, false);
        RecyclerView productsRecycler = binding.productsRecycler;
        loadingView = binding.loadingLayout.getRoot();
        itemList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new ProductsAdapter(itemList, this);
        getData();
        productsRecycler.setLayoutManager(layoutManager);
        productsRecycler.setAdapter(adapter);
        productItemViewModel = new ViewModelProvider(requireActivity()).get(ProductItemViewModel.class);
        productItemViewModel.getLiveProductItem().observe(requireActivity(), productItem -> {
            Log.e("productsFragment", "product Item observer" + productItemViewModel.getProductItem().getBrand());
            adapter.notifyItemChanged(clickedPosition, productItem);
        });
        return binding.getRoot();
    }

    private void getData() {
        loadingView.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.ALL_PRODUCTS_URL)
                .whereEqualTo(Constants.KEY_STORE_ID, MainActivity.storeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingView.setVisibility(View.INVISIBLE);
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ProductItem item = documentSnapshot.toObject(ProductItem.class);
                        if (item != null) {
                            item.setDocumentId(documentSnapshot.getId());
                            itemList.add(item);
                            adapter.notifyItemInserted(adapter.getItemCount() - 1);
                        }
                    }
                    checkForEmptyRecycler();
                })
                .addOnFailureListener(e -> {
                    loadingView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Error loading Try again", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkForEmptyRecycler() {
        if (itemList.isEmpty())
            binding.emptyRecyclerText.setVisibility(View.VISIBLE);
        else binding.emptyRecyclerText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onProductItemClicked(int position) {
        clickedPosition = position;
        productItemViewModel.setProductItem(itemList.get(position));
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.KEY_EDIT_MODE, true);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getParentFragmentManager();
        assert getParentFragment() != null;
        fragmentManager.beginTransaction()
                .add(getParentFragment().requireView().findViewById(R.id.fragment_container).getId()
                        , fragment)
                .addToBackStack(null)
                .commit();
    }
}

