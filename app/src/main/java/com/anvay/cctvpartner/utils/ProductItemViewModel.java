package com.anvay.cctvpartner.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.anvay.cctvpartner.models.ProductItem;

public class ProductItemViewModel extends ViewModel {

    private final MutableLiveData<ProductItem> productItem = new MutableLiveData<>();

    public ProductItem getProductItem() {
        return productItem.getValue();
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem.setValue(productItem);
    }

    public LiveData<ProductItem> getLiveProductItem() {
        return productItem;
    }

    public void update(String name, String brand, String details, String subCategory, String imageUrl,
                       String sku, String sla, String mName, String mAddress, int stockQuantity,
                       double price, double discount, String gst) {
        ProductItem item = this.productItem.getValue();
        if (item != null) {
            item.setBrand(brand);
            item.setName(name);
            item.setDetails(details);
            item.setSubCategory(subCategory);
            item.setImageUrl(imageUrl);
            item.setSku(sku);
            item.setDiscount(discount);
            item.setSla(sla);
            item.setmName(mName);
            item.setmAddress(mAddress);
            item.setStockQuantity(stockQuantity);
            item.setPrice(price);
            item.setGst(gst);
        }
        productItem.setValue(item);
    }
}
