package com.anvay.cctvpartner.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anvay.cctvpartner.R;
import com.anvay.cctvpartner.databinding.ListItemProductBinding;
import com.anvay.cctvpartner.models.ProductItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {
    private final List<ProductItem> productItems;
    private final ProductItemClickListener listener;

    public ProductsAdapter(List<ProductItem> productItems, ProductItemClickListener listener) {
        this.productItems = productItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemProductBinding binding = ListItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return productItems == null ? 0 : productItems.size();
    }

    public interface ProductItemClickListener {
        void onProductItemClicked(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView productId, productBrand, productPrice, productName;
        private final ShapeableImageView productImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productBrand = itemView.findViewById(R.id.product_brand);
            productId = itemView.findViewById(R.id.product_id);
            productPrice = itemView.findViewById(R.id.product_price);
            productName = itemView.findViewById(R.id.product_name);
            productImage = itemView.findViewById(R.id.product_image);
            itemView.setOnClickListener(view -> listener.onProductItemClicked(getAdapterPosition()));
        }

        @SuppressLint("SetTextI18n")
        public void bind(int position) {
            ProductItem item = productItems.get(position);
            productId.setText(item.getId());
            productBrand.setText(item.getBrand());
            double price = item.getPrice();
            if (price == 0.00)
                productPrice.setText("Admin is deciding");
            else
                productPrice.setText("\u20B9 " + item.getPrice());
            productName.setText(item.getName());
            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.loading_bar)
                    .into(productImage);
        }
    }
}
