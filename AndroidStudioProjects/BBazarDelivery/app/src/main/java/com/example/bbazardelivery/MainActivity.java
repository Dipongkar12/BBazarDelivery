package com.example.bbazardelivery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
public class MainActivity extends AppCompatActivity {

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons after setting content view
        button1 = findViewById(R.id.liveproduct);

        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.deliver);

        // Set click listeners for each button
        button1.setOnClickListener(view -> openLiveOrderActivity());
      //  button2.setOnClickListener(view -> openAddProductActivity());
       // button3.setOnClickListener(view -> editproductactivity());
       // button4.setOnClickListener(view -> forntspics());
        button5.setOnClickListener(view -> showToast("Button 5 clicked"));
        button6.setOnClickListener(view -> delivered());
       // button7.setOnClickListener(view -> user());
    }


    private void openLiveOrderActivity() {
        Intent intent = new Intent(MainActivity.this, LiveOrderActivity.class);
        startActivity(intent);
    }
    private void openAddProductActivity() {
        //Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
       // startActivity(intent);
    }
    private void editproductactivity(){
       // Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
      //  startActivity(intent);
    }
    private void forntspics(){
        //Intent intent = new Intent(MainActivity.this, ForntDesignActivity.class);
      //  startActivity(intent);

    }

    private void showToast(String message) {
       // Intent intent = new Intent(MainActivity.this, Productlistview.class);
       // startActivity(intent);


    }
    private void delivered(){
        //Intent intent = new Intent(MainActivity.this, DeliveredActivity.class);
     //   startActivity(intent);
    }
    private void user(){
       // Intent intent = new Intent(MainActivity.this, UserActivity.class);
      //  startActivity(intent);
    }
}
