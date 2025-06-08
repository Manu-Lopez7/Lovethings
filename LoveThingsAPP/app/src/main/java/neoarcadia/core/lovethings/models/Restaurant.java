package neoarcadia.core.lovethings.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class Restaurant implements Serializable {
    private Long id;
    private String name;
    private String address;
    private String category;
    private String phoneNumber;
    private String menuLink;
    private String hours;
    private String imagePath;
    private List<Dish> dishes;
    private Double latitude;
    private Double longitude;

    public Restaurant() {
        this.dishes = new ArrayList<>();
    }

    public Restaurant(Long id, String name, String address, String category, String phoneNumber, String menuLink, String hours, String imagePath, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.category = category;
        this.phoneNumber = phoneNumber;
        this.menuLink = menuLink;
        this.hours = hours;
        this.imagePath = imagePath;
        this.dishes = new ArrayList<>();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean hasDishesByUser(Long userId) {

        Log.d("Restaurant", "Filtrando platos para el usuario con ID: " + userId);
        for (Dish dish : dishes) {
            if (dish.getUser() != null && userId.equals(dish.getUser().getId())) {
                Log.d("Restaurant", "Plato encontrado para el usuario con ID: " + userId);
                return true;
            }
        }
        Log.d("Restaurant", "No se encontraron platos asociados al usuario con ID: " + userId);
        return false;}


        // Getters y setters
        public Long getId () {
            return id;
        }

        public void setId (Long id){
            this.id = id;
        }

        public String getName () {
            return name;
        }

        public void setName (String name){
            this.name = name;
        }

        public String getAddress () {
            return address;
        }

        public void setAddress (String address){
            this.address = address;
        }

        public String getCategory () {
            return category;
        }

        public void setCategory (String category){
            this.category = category;
        }

        public String getPhoneNumber () {
            return phoneNumber;
        }

        public void setPhoneNumber (String phoneNumber){
            this.phoneNumber = phoneNumber;
        }

        public String getMenuLink () {
            return menuLink;
        }

        public void setMenuLink (String menuLink){
            this.menuLink = menuLink;
        }

        public String getHours () {
            return hours;
        }

        public void setHours (String hours){
            this.hours = hours;
        }

        public String getImagePath () {
            return imagePath;
        }

        public void setImagePath (String imagePath){
            this.imagePath = imagePath;
        }

        public List<Dish> getDishes () {
            return dishes;
        }

        public void setDishes (List < Dish > dishes) {
            this.dishes = dishes;
        }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void addDish (Dish dish){
            this.dishes.add(dish);
        }
    }
