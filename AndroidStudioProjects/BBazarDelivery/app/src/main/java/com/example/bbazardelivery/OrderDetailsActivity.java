package com.example.bbazardelivery;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailsActivity extends AppCompatActivity {
    private TextView tvName, tvMobile, tvAddress, tvDeliveryTime, tvTotalPrice, tvOrderDate, tvOrderTime, extra;
    private RecyclerView recyclerViewProducts;
    private OrderDetailsAdapter orderDetailsAdapter;
    private List<OrderDetails> orderDetailsList;
    private String orderId;
    private String userId;
    private static final int STORAGE_PERMISSION_CODE = 1;
    private StoragePermissionManager storagePermissionManager;
    private Button pdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        tvName = findViewById(R.id.tv_name);
        tvMobile = findViewById(R.id.tv_mobile);
        tvAddress = findViewById(R.id.tv_address);
        tvDeliveryTime = findViewById(R.id.tv_delivery_time);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvOrderTime = findViewById(R.id.tv_order_time);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        extra = findViewById(R.id.tv_extra);
        pdf = findViewById(R.id.pdf);
        recyclerViewProducts = findViewById(R.id.recycler_view_products);
        storagePermissionManager = new StoragePermissionManager(this);

        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        orderDetailsList = new ArrayList<>();
        orderDetailsAdapter = new OrderDetailsAdapter(orderDetailsList);
        recyclerViewProducts.setAdapter(orderDetailsAdapter);

        // Obtain orderId and userId from the intent
        orderId = getIntent().getStringExtra("orderId");
        userId = getIntent().getStringExtra("userId");

        // Check for null values of orderId and userId
        if (orderId == null || userId == null) {
            Log.e("OrderDetailsActivity", "Order ID or User ID is null");
            Toast.makeText(this, "Invalid order details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pdf.setOnClickListener(v -> {
            if (storagePermissionManager.checkStoragePermission()) {
                generatePDF();
            } else {
                storagePermissionManager.requestPermissions();
            }
        });

        loadUserDetails();  // Load user details first
        loadOrderDetails();
    }

    private void loadUserDetails() {
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String mobile = dataSnapshot.child("mobile").getValue(String.class);
                    String address = dataSnapshot.child("address").getValue(String.class);

                    tvName.setText("Name: " + name);
                    tvMobile.setText("Mobile: " + mobile);
                    tvAddress.setText("Address: " + address);
                } else {
                    Log.e("OrderDetailsActivity", "User data not found");
                }
                loadProductDetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("OrderDetailsActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void loadOrderDetails() {
        DatabaseReference orderDatabase = FirebaseDatabase.getInstance().getReference("LiveOrder")
                .child(userId).child(orderId);

        orderDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String deliveryTime = dataSnapshot.child("deliveryTime").getValue(String.class);
                    String orderDate = dataSnapshot.child("OrderDate").getValue(String.class);
                    String orderTime = dataSnapshot.child("OrderTime").getValue(String.class);

                    Double totalPrice = dataSnapshot.child("totalPrice").getValue(Double.class);

                    List<String> extraTextsList = new ArrayList<>();
                    for (DataSnapshot extraTextSnapshot : dataSnapshot.child("extraTexts").getChildren()) {
                        extraTextsList.add(extraTextSnapshot.getValue(String.class));
                    }
                    String extraTexts = extraTextsList.isEmpty() ? "No extras" : String.join(", ", extraTextsList);


                    tvOrderDate.setText("Order Date: " + orderDate);
                    tvOrderTime.setText("Order Time: " + orderTime);
                    extra.setText("Extras: " + extraTexts);
                    tvDeliveryTime.setText("Delivery Time: " + deliveryTime);
                    tvTotalPrice.setText("Total Price: " + totalPrice);

                    DataSnapshot productsSnapshot = dataSnapshot.child("products");
                    for (DataSnapshot productSnapshot : productsSnapshot.getChildren()) {
                        OrderDetails orderDetails = productSnapshot.getValue(OrderDetails.class);
                        if (orderDetails != null) {
                            orderDetailsList.add(orderDetails);
                        } else {
                            Log.e("OrderDetailsActivity", "Failed to convert data for product key: " + productSnapshot.getKey());
                        }
                    }
                    orderDetailsAdapter.notifyDataSetChanged();
                } else {
                    Log.e("OrderDetailsActivity", "Order data not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("OrderDetailsActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }


    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    private void loadProductDetails() {
        DatabaseReference productsDatabase = FirebaseDatabase.getInstance().getReference("LiveOrder")
                .child(userId).child(orderId).child("carts");

        productsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderDetailsList.clear();
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    OrderDetails productDetails = productSnapshot.getValue(OrderDetails.class);
                    orderDetailsList.add(productDetails);
                }
                orderDetailsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permissions Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatePDF() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint boldPaint = new Paint(); // To create bold text
        Paint headerPaint = new Paint(); // For header styling

        // Set up bold paint for headers
        boldPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        boldPaint.setTextSize(14);
        boldPaint.setColor(Color.BLACK);

        // Set up header paint (larger size, perhaps a different color)
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerPaint.setTextSize(16);
        headerPaint.setColor(Color.DKGRAY);

        // Define a page size (A4 size)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Starting point for text
        int x = 20, y = 30;
        int lineSpacing = (int) (paint.descent() - paint.ascent()) + 4;  // Line spacing for readability

        // Set up the paint color for general text
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);

        // Title of the document
        canvas.drawText("         Order Details", x, y, headerPaint);
        y += lineSpacing * 2;  // Add space after the title

        // Write main order details
        canvas.drawText("          " + tvName.getText().toString(), x, y, paint);
        y += lineSpacing;
        canvas.drawText("           " + tvMobile.getText().toString(), x, y, paint);
        y += lineSpacing;
        canvas.drawText("           " + tvAddress.getText().toString(), x, y, paint);
        y += lineSpacing;
        canvas.drawText("           " + tvOrderDate.getText().toString(), x, y, paint);
        y += lineSpacing;
        canvas.drawText("           " + tvOrderTime.getText().toString(), x, y, paint);
        y += lineSpacing;
        canvas.drawText("           " + tvDeliveryTime.getText().toString(), x, y, paint);
        y += lineSpacing;
        canvas.drawText("            " + extra.getText().toString(), x, y, paint);
        y += lineSpacing * 2;

      // Extra space before product list

        // Write product section
        canvas.drawText("                Products:", x, y, boldPaint);
        y += lineSpacing;

        // Loop through the product list and write each product details
        paint.setTextSize(12);
        for (OrderDetails orderDetail : orderDetailsList) {
            canvas.drawText("        " + "            "+orderDetail.getName()+ "          "+ orderDetail.getPrice()+"       *  "+orderDetail.getQuantity()+"              =    "+orderDetail.getTotalPrice(), x, y, paint);
            y += lineSpacing;

        }

        canvas.drawText("                                             " + tvTotalPrice.getText().toString(), x, y, headerPaint);
        y += lineSpacing;

        // End the page and finish writing the document
        pdfDocument.finishPage(page);

        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        String fileName = "OrderSummary.pdf";
        OutputStream outputStream;

        try {
            File file = new File(directoryPath, fileName);
            outputStream = new FileOutputStream(file);
            pdfDocument.writeTo(outputStream);
            Toast.makeText(this, "PDF generated successfully: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }
}