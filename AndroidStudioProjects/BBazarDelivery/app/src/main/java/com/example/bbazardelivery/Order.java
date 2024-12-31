package com.example.bbazardelivery;

public class Order {
    private String id;           // Field for Order ID
    private String userId;       // New field for User ID
    private String userName;
    private String userMobile;
    private String userAddress;
    private double totalPrice;
    private String deliveryTime;
    private String orderDate;     // Field for Order Date
    private String orderTime;     // Field for Order Time

    public Order() {
        // Default constructor
    }

    public Order(String id, String userId, String userName, String userMobile, String userAddress, double totalPrice, String deliveryTime, String orderDate, String orderTime) {
        this.id = id;              // Initialize the Order ID
        this.userId = userId;      // Initialize the User ID
        this.userName = userName;
        this.userMobile = userMobile;
        this.userAddress = userAddress;
        this.totalPrice = totalPrice;
        this.deliveryTime = deliveryTime;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
    }

    // Getter and Setter for Order ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter for User ID
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getters and Setters for other fields
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
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
}
