package com.example.bbazardelivery;

public class OrderDetails {
    private String id;
    private String name;
    private double price;
    private String imageUrl;
    private int quantity;
    private String orderDate;  // New field for Order Date
    private String orderTime;  // New field for Order Time
    private double totalPrice; // New field for Total Price
    private String unit;       // New field for Unit
    private int value;         // New field for Value
    private String extraThings; // New field for Extra Things

    // Default constructor required for Firebase
    public OrderDetails() {
    }

    // Constructor with parameters, including extraThings
    public OrderDetails(String id, String name, double price, String imageUrl, int quantity, String orderDate, String orderTime, String unit, int value, String extraThings) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
        this.unit = unit;
        this.value = value;
        this.extraThings = extraThings;
        this.totalPrice = calculateTotalPrice();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.totalPrice = calculateTotalPrice(); // Update total price when price changes
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = calculateTotalPrice(); // Update total price when quantity changes
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getExtraThings() {
        return extraThings;
    }

    public void setExtraThings(String extraThings) {
        this.extraThings = extraThings;
    }

    // Method to calculate total price
    private double calculateTotalPrice() {
        return price * quantity;
    }
}
