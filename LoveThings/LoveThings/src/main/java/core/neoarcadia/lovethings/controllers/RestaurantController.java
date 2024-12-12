package core.neoarcadia.lovethings.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.neoarcadia.lovethings.models.Restaurant;
import core.neoarcadia.lovethings.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);


    @Autowired
    private RestaurantRepository restaurantRepository;

    private final String IMAGE_DIRECTORY = "C:\\uploads\\restaurants\\";

    // Crear un nuevo restaurante solo los adm
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRestaurant(
            @RequestParam("name") String name,
            @RequestParam("address") String address,
            @RequestParam("category") String category,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("menuLink") String menuLink,
            @RequestParam("hours") String hours,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        logger.info("Received request to create restaurant: {}", name);

        try {
            String imagePath = saveImage(image, IMAGE_DIRECTORY);
            logger.debug("Image saved at: {}", imagePath);

            Restaurant restaurant = new Restaurant();
            restaurant.setName(name);
            restaurant.setAddress(address);
            restaurant.setCategory(category);
            restaurant.setPhoneNumber(phoneNumber);
            restaurant.setMenuLink(menuLink);
            restaurant.setHours(hours);
            restaurant.setImagePath(imagePath);
            try {
                Map<String, Double> coordinates = getCoordinatesFromAddress(address);
                restaurant.setLatitude(coordinates.get("latitude"));
                restaurant.setLongitude(coordinates.get("longitude"));
            } catch (Exception e) {
                logger.error("Error obtaining coordinates: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid address provided.");
            }

            restaurantRepository.save(restaurant);
            return ResponseEntity.ok("Restaurant created successfully!");

        } catch (IOException e) {
            logger.error("Error saving restaurant image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving restaurant image.");
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error occurred while creating restaurant.");
        }
    }
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getRestaurantsForUser(Principal principal) {
        try {
            String username = principal.getName();
            logger.info("Fetching restaurants for user: {}", username);
            List<Restaurant> filteredRestaurants = restaurantRepository.findAll().stream()
                    .filter(restaurant -> restaurant.getDishes().stream()
                            .anyMatch(dish -> dish.getUser().getUsername().equals(username)))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(filteredRestaurants);
        } catch (Exception e) {
            logger.error("Error fetching restaurants for user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching restaurants for user.");
        }
    }
    @GetMapping("/getall")
    public ResponseEntity<?> getAllRestaurants() {
        logger.info("Charging all restaurants");
        String baseUrl = "http://192.168.18.10:8080/uploads";
        List<Restaurant> restaurants = restaurantRepository.findAll();
        for (Restaurant restaurant : restaurants) {
            if (restaurant.getImagePath() != null) {
                restaurant.setImagePath(restaurant.getImagePath().replace("C:\\uploads", baseUrl));
            }
        }
        return ResponseEntity.ok(restaurantRepository.findAll());
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
        logger.debug("No image provided for the restaurant");
        return null;
    }
    private Map<String, Double> getCoordinatesFromAddress(String address) throws Exception {
        String apiKey = "AIzaSyANL6cEVUwNHGO1yavmw1zN_dRWB8xabSw";
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(address, "UTF-8") + "&key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root = new ObjectMapper().readTree(response.getBody());
            JsonNode location = root.path("results").path(0).path("geometry").path("location");

            if (location.has("lat") && location.has("lng")) {
                Map<String, Double> coordinates = new HashMap<>();
                coordinates.put("latitude", location.get("lat").asDouble());
                coordinates.put("longitude", location.get("lng").asDouble());
                return coordinates;
            }
        }

        throw new RuntimeException("Error obtaining coordinates from Google API.");
    }
}
