package com.example.bbazardelivery;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class OrderDetailsAdapter extends RecyclerView.Adapter<OrderDetailsAdapter.OrderViewHolder> {

    private List<OrderDetails> OrderDetailsList;

    public OrderDetailsAdapter(List<OrderDetails> OrderDetailsList) {
        this.OrderDetailsList = OrderDetailsList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_details, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderDetails OrderDetails = OrderDetailsList.get(position);
        holder.tvProductName.setText(OrderDetails.getName());
        holder.tvPricePerKg.setText("Price : " + OrderDetails.getPrice());
        holder.tvQuantity.setText("Quantity: " + OrderDetails.getQuantity());
        holder.tvtotalprice.setText("Total Price: " + "৳"+OrderDetails.getTotalPrice());
        holder.tv_unit.setText(" " + OrderDetails.getValue() + " " + OrderDetails.getUnit());


        // Load the product image using Glide
        Glide.with(holder.itemView.getContext())
                .load(OrderDetails.getImageUrl())
                .into(holder.ivProductImage);
    }

    @Override
    public int getItemCount() {
        return OrderDetailsList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvPricePerKg;
        TextView tvQuantity;
        TextView tvtotalprice;
        TextView tv_unit;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvPricePerKg = itemView.findViewById(R.id.tv_price_per_kg);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvtotalprice = itemView.findViewById(R.id.tv_total_price);
            tv_unit = itemView.findViewById(R.id.tv_product_unit);
        }
    }
}
