package neoarcadia.core.lovethings.models;

public class Restaurant {
    private Long id;
    private String name;
    private String address;
    private String imagePath;

    public Restaurant() {
    }

    public Restaurant(Long id, String name, String address, String imagePath) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.imagePath = imagePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
