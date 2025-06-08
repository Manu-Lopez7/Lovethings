package core.neoarcadia.lovethings.controllers;

import core.neoarcadia.lovethings.models.Dish;
import core.neoarcadia.lovethings.models.Restaurant;
import core.neoarcadia.lovethings.models.User;
import core.neoarcadia.lovethings.repository.DishRepository;
import core.neoarcadia.lovethings.repository.RestaurantRepository;
import core.neoarcadia.lovethings.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(DishController.class);

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    private final String IMAGE_DIRECTORY = "C:\\uploads\\dish\\";

    // Añadir un nuevo plato a un restaurante que ya esta
    @PostMapping("/add")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> addDish(
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam(value = "waitTime",required = false) String notes,
            @RequestParam("rating") Double rating,
            @RequestParam("notes") Double waitTime,
            @RequestParam("restaurantId") Long restaurantId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Principal principal) {
        logger.info("Inicio del método addDish");
        logger.info("Datos recibidos: name={}, price={}, waitTime={}, rating={}, notes={}, restaurantId={}", name, price, waitTime, rating, notes, restaurantId);


        try {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user == null) {
                logger.error("Usuario no encontrado");
            }
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            String imagePath = saveImage(image, IMAGE_DIRECTORY);
            logger.debug("Image saved at: {}", imagePath);

            Dish dish = new Dish();
            dish.setName(name);
            dish.setPrice(price);
            dish.setWaitTime(waitTime);
            dish.setRating(rating);
            dish.setNotes(notes);
            dish.setRestaurant(restaurant);
            dish.setUser(user);
            dish.setImagePath(imagePath);
            logger.info("Plato guardado correctamente: {}", dish);

            dishRepository.save(dish);
            return ResponseEntity.ok("Dish added successfully!");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving dish image.");
        }
    }

    private String saveImage(MultipartFile image, String directory) throws IOException {
        if (image != null && !image.isEmpty()) {
            Files.createDirectories(Paths.get(directory));
            byte[] bytes = image.getBytes();
            Path path = Paths.get(directory + image.getOriginalFilename());
            Files.write(path, bytes);
            logger.debug("Image saved at path: {}", path.toString());
            return path.toString();
        }
        logger.debug("No image provided for the dish");
        return null;
    }

    // Modificar un plato por rol
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> updateDish(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("notes") Double waitTime,
            @RequestParam("rating") Double rating,
            @RequestParam("waitTime") String notes,
            @RequestParam(value = "image", required = false) MultipartFile image

    ){

        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found"));

        dish.setName(name);
        dish.setPrice(price);
        dish.setWaitTime(waitTime);
        dish.setRating(rating);
        dish.setNotes(notes);
        if (image != null && !image.isEmpty()) {
            try {
                String imagePath = saveImage(image, IMAGE_DIRECTORY);
                dish.setImagePath(imagePath);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error saving dish image.");
            }
        }

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
    @PatchMapping("/favorite/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> markAsFavorite(@PathVariable Long id, @RequestParam Boolean isFavorite) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found"));
        dish.setIsFavorite(isFavorite);
        dishRepository.save(dish);
        logger.info("Marking dish as favorite: id={}, isFavorite={}", id, isFavorite);
        return ResponseEntity.ok("Dish favorite status updated successfully!");
    }
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteDish(@PathVariable Long id, Principal principal) {
        try {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Dish dish = dishRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Dish not found"));

            // Verificar que el usuario sea el propietario del plato
            if (!dish.getUser().equals(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You do not have permission to delete this dish.");
            }

            dishRepository.delete(dish);
            logger.info("Dish deleted successfully: id={}, user={}", id, username);
            return ResponseEntity.ok("Dish deleted successfully!");
        } catch (Exception e) {
            logger.error("Error deleting dish: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while trying to delete the dish.");
        }
    }
}
