package core.neoarcadia.lovethings.controllers;

import core.neoarcadia.lovethings.models.Dish;
import core.neoarcadia.lovethings.models.Restaurant;
import core.neoarcadia.lovethings.models.User;
import core.neoarcadia.lovethings.repository.DishRepository;
import core.neoarcadia.lovethings.repository.RestaurantRepository;
import core.neoarcadia.lovethings.repository.UserRepository;
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
import java.security.Principal;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    private final String IMAGE_DIRECTORY = "uploads/dishes/";

    // Añadir un nuevo plato a un restaurante que ya esta
    @PostMapping("/add")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> addDish(
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("waitTime") Integer waitTime,
            @RequestParam("rating") Integer rating,
            @RequestParam("notes") String notes,
            @RequestParam("restaurantId") Long restaurantId,
            @RequestParam("image") MultipartFile image,
            Principal principal) {

        try {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            String imagePath = saveImage(image, IMAGE_DIRECTORY);

            Dish dish = new Dish();
            dish.setName(name);
            dish.setPrice(price);
            dish.setWaitTime(waitTime);
            dish.setRating(rating);
            dish.setNotes(notes);
            dish.setRestaurant(restaurant);
            dish.setUser(user);
            dish.setImagePath(imagePath);

            dishRepository.save(dish);
            return ResponseEntity.ok("Dish added successfully!");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving dish image.");
        }
    }

    // Metodo para guardar la imagen en el servidor (en profreso)
    private String saveImage(MultipartFile image, String directory) throws IOException {
        if (!image.isEmpty()) {
            byte[] bytes = image.getBytes();
            Path path = Paths.get(directory + image.getOriginalFilename());
            Files.write(path, bytes);
            return path.toString();
        }
        return null;
    }

    // Modificar un plato por rol
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<?> updateDish(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("waitTime") Integer waitTime,
            @RequestParam("rating") Integer rating,
            @RequestParam("notes") String notes) {

        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found"));

        dish.setName(name);
        dish.setPrice(price);
        dish.setWaitTime(waitTime);
        dish.setRating(rating);
        dish.setNotes(notes);

        dishRepository.save(dish);
        return ResponseEntity.ok("Dish updated successfully!");
    }

    // Obtener platos usuario
    @GetMapping("/user-feed")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getUserDishes(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(dishRepository.findByUser(user));
    }
}
