package com.example.bbazardelivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LiveOrderActivity extends AppCompatActivity {
    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private DatabaseReference orderDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userDatabase;
    private TextView tvNoOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_order);

        recyclerViewOrders = findViewById(R.id.recycler_view_orders);
        tvNoOrders = findViewById(R.id.tv_no_orders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList);
        recyclerViewOrders.setAdapter(orderAdapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        orderDatabase = FirebaseDatabase.getInstance().getReference("LiveOrder");
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        loadOrders();
    }

    private void loadOrders() {
        orderDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderList.clear();

                for (DataSnapshot userOrderSnapshot : dataSnapshot.getChildren()) {
                    String userId = userOrderSnapshot.getKey();
                    userDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                String userName = userSnapshot.child("name").getValue(String.class);
                                String userMobile = userSnapshot.child("mobile").getValue(String.class);
                                String userAddress = userSnapshot.child("address").getValue(String.class);

                                processOrders(userOrderSnapshot, userName, userMobile, userAddress);
                            }
                            updateOrderVisibility(); // Update UI after processing orders
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(LiveOrderActivity.this, "Error loading user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }


                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LiveOrderActivity.this, "Error loading orders: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processOrders(DataSnapshot userOrderSnapshot, String userName, String userMobile, String userAddress) {
        for (DataSnapshot orderSnapshot : userOrderSnapshot.getChildren()) {
            Order order = orderSnapshot.getValue(Order.class);
            if (order != null) {
                order.setUserName(userName);
                order.setUserMobile(userMobile);
                order.setUserAddress(userAddress);
                order.setOrderDate(orderSnapshot.child("OrderDate").getValue(String.class));
                order.setOrderTime(orderSnapshot.child("OrderTime").getValue(String.class));
                order.setDeliveryTime(orderSnapshot.child("deliveryTime").getValue(String.class));
                order.setId(orderSnapshot.getKey());
                order.setUserId(userOrderSnapshot.getKey());

                orderList.add(order);
            }
        }
        orderAdapter.notifyDataSetChanged();
    }

    private void updateOrderVisibility() {
        if (orderList.isEmpty()) {
            tvNoOrders.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
            tvNoOrders.setText("Your cart is empty");
        } else {
            tvNoOrders.setVisibility(View.GONE);
            recyclerViewOrders.setVisibility(View.VISIBLE);
        }
    }

    private void moveOrderToDelivered(String userId, String orderId) {
        DatabaseReference deliveredDatabase = FirebaseDatabase.getInstance().getReference("OnTheWay").child(userId).child(orderId);
        DatabaseReference liveOrderDatabase = orderDatabase.child(userId).child(orderId);
        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        liveOrderDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Copy the order to the "Delivered" database
                    deliveredDatabase.setValue(dataSnapshot.getValue()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Remove the order from "LiveOrder" after copying
                            liveOrderDatabase.removeValue().addOnCompleteListener(removeTask -> {
                                if (removeTask.isSuccessful()) {
                                    Toast.makeText(LiveOrderActivity.this, "Order moved for Ready", Toast.LENGTH_SHORT).show();

                                    // Save the user details with the updated total amount, orders, and first order date (current date)
                                    Order order = dataSnapshot.getValue(Order.class);
                                    if (order != null) {
                                        // Get the current date to be saved as firstOrderDate
                                        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                                        // Update totalAmount, totalOrders, and firstOrderDate in the "Users" database
                                        usersDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                double currentTotalAmount = 0;
                                                int totalOrders = 0;
                                                String firstOrderDate = currentDate; // Set to current date

                                                if (userSnapshot.exists()) {
                                                    // Retrieve current totalAmount
                                                    if (userSnapshot.child("totalAmount").exists()) {
                                                        currentTotalAmount = userSnapshot.child("totalAmount").getValue(Double.class);
                                                    }

                                                    // Retrieve current totalOrders
                                                    if (userSnapshot.child("totalOrders").exists()) {
                                                        totalOrders = userSnapshot.child("totalOrders").getValue(Integer.class);
                                                    }

                                                    // Retrieve firstOrderDate only if it exists
                                                    if (userSnapshot.child("firstOrderDate").exists()) {
                                                        firstOrderDate = userSnapshot.child("firstOrderDate").getValue(String.class);
                                                    }
                                                }

                                                // Update totalAmount and totalOrders
                                                double newTotalAmount = currentTotalAmount + order.getTotalPrice();
                                                int newTotalOrders = totalOrders + 1;

                                                // Save the updated data
                                                usersDatabase.child("totalAmount").setValue(newTotalAmount);
                                                usersDatabase.child("totalOrders").setValue(newTotalOrders);
                                                usersDatabase.child("firstOrderDate").setValue(firstOrderDate); // Update firstOrderDate

                                                Toast.makeText(LiveOrderActivity.this, "User details saved successfully", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(LiveOrderActivity.this, "Error retrieving user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(LiveOrderActivity.this, "Error removing from LiveOrder", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(LiveOrderActivity.this, "Error copying to Delivered", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LiveOrderActivity.this, "Error accessing order data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        private final List<Order> orderList;

        public OrderAdapter(List<Order> orderList) {
            this.orderList = orderList;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orderList.get(position);
            holder.tvUserName.setText("Name: " + order.getUserName());
            holder.tvUserMobile.setText("Mobile: " + order.getUserMobile());
            holder.tvUserAddress.setText("Address: " + order.getUserAddress());
            holder.tvTotalPrice.setText("Total Price: " + order.getTotalPrice() + "৳");
            holder.tvOrderDate.setText("Order Date: " + order.getOrderDate());
            holder.tvOrderTime.setText("Order Time: " + order.getOrderTime());
            holder.tvDeliveryTime.setText("Delivery Time: " + order.getDeliveryTime());

            holder.itemView.setOnClickListener(v -> {
                String orderId = order.getId();
                String userId = order.getUserId();

                if (orderId == null || userId == null) {
                    Toast.makeText(LiveOrderActivity.this, "Invalid order details", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(LiveOrderActivity.this, OrderDetailsActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("userId", userId);
                startActivity(intent);
            });

            holder.tvDelivered.setOnClickListener(v -> {
                if (order.getUserId() != null && order.getId() != null) {
                    moveOrderToDelivered(order.getUserId(), order.getId());
                    removeOrderFromList(position);
                } else {
                    Toast.makeText(LiveOrderActivity.this, "Order ID or User ID missing", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return orderList.size();
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserName, tvUserMobile, tvUserAddress, tvTotalPrice, tvOrderDate, tvOrderTime, tvDeliveryTime;
            Button tvDelivered;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUserName = itemView.findViewById(R.id.tv_user_name);
                tvUserMobile = itemView.findViewById(R.id.tv_user_mobile);
                tvUserAddress = itemView.findViewById(R.id.tv_user_address);
                tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
                tvOrderDate = itemView.findViewById(R.id.tv_order_date);
                tvOrderTime = itemView.findViewById(R.id.tv_order_time);
                tvDeliveryTime = itemView.findViewById(R.id.tv_delivery_time);
                tvDelivered = itemView.findViewById(R.id.bt_delivered);
            }
        }

        private void removeOrderFromList(int position) {
            orderList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, orderList.size());

            if (orderList.isEmpty()) {
                tvNoOrders.setVisibility(View.VISIBLE);
                recyclerViewOrders.setVisibility(View.GONE);
            }
        }
    }
}
