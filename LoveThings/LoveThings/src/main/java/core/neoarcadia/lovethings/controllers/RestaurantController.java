package core.neoarcadia.lovethings.controllers;

import core.neoarcadia.lovethings.models.Restaurant;
import core.neoarcadia.lovethings.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    private final String IMAGE_DIRECTORY = "uploads/restaurants/";

    // Crear un nuevo restaurante solo los adm
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRestaurant(
            @RequestParam("name") String name,
            @RequestParam("address") String address,
            @RequestParam("image") MultipartFile image) {
        
        try {
            // Guardar la imagen en el servidor (en progreso esta difisi el asunto)
            String imagePath = saveImage(image, IMAGE_DIRECTORY);

            // Crear y guardar el restaurante
            Restaurant restaurant = new Restaurant();
            restaurant.setName(name);
            restaurant.setAddress(address);
            restaurant.setImagePath(imagePath);

            restaurantRepository.save(restaurant);
            return ResponseEntity.ok("Restaurant created successfully!");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving restaurant image.");
        }
    }
    @GetMapping
    public ResponseEntity<?> getAllRestaurants() {
        return ResponseEntity.ok(restaurantRepository.findAll());
    }

    private String saveImage(MultipartFile image, String directory) throws IOException {
        if (!image.isEmpty()) {
            byte[] bytes = image.getBytes();
            Path path = Paths.get(directory + image.getOriginalFilename());
            Files.write(path, bytes);
            return path.toString();
        }
        return null;
    }
}
