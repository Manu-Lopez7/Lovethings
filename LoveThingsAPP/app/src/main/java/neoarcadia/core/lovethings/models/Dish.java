package neoarcadia.core.lovethings.models;

import android.net.Uri;

public class Dish {
    private Long id;
    private String name;
    private String imagePath;
    private double price = 0.0;
    private int waitTime = 0;
    private int rating = 0;
    private String notes;
    private User user;
    private String restaurantId;
    private boolean isFavorite;
    public Dish() {
    }

    public Dish(Long id ,String name, double price, int waitTime, int rating, String notes, String imagePath, User user, String restaurantId, boolean isFavorite) {
        this.name = name;
        this.price = price;
        this.waitTime = waitTime;
        this.rating = rating;
        this.notes = notes;
        this.imagePath = imagePath;
        this.user = user;
        this.restaurantId = restaurantId;
        this.isFavorite = isFavorite;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
}
