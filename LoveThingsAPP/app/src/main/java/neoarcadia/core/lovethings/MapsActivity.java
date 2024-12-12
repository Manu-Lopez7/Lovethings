package neoarcadia.core.lovethings;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.stream.Collectors;

import neoarcadia.core.lovethings.add.AddDishActivity;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.databinding.ActivityMapsBinding;
import neoarcadia.core.lovethings.models.Dish;
import neoarcadia.core.lovethings.models.Restaurant;
import neoarcadia.core.lovethings.utils.SettingsActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ImageButton postBtn;
    private ImageButton settingsBtn;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        postBtn = findViewById(R.id.btnpost);
        settingsBtn = findViewById(R.id.btnsettings);
        BottomNavigationView navigationBar = findViewById(R.id.navigationbar);
        navigationBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.menu_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (itemId == R.id.menu_post) {
                startActivity(new Intent(this, AddDishActivity.class));
                return true;
            } else if (itemId == R.id.menu_location) {
                startActivity(new Intent(this, MapsActivity.class));
                return true;
            } else if (itemId == R.id.menu_profile) {
                startActivity(new Intent(this, FavActivity.class));
                return true;
            } else {
                return false;
            }
        });

        postBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, AddDishActivity.class));
        });
        settingsBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapfr);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MapsActivity", "No se pudo encontrar el fragmento del mapa");
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_id", -1);

        Call<List<Restaurant>> call = apiService.getRestaurantsByUser();
        call.enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(@NonNull Call<List<Restaurant>> call, @NonNull Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Restaurant restaurant : response.body()) {
                        Log.d("MapsActivity", "Restaurante: " + restaurant.getName());
                        List<Dish> userDishes = restaurant.getDishes().stream()
                                .filter(dish -> dish.getUser() != null && dish.getUser().getId() == userId)
                                .collect(Collectors.toList());

                        if (!userDishes.isEmpty()) {
                            // Calcular nota media y platos pedidos
                            double averageRating = userDishes.stream()
                                    .mapToInt(Dish::getRating)
                                    .average()
                                    .orElse(0.0);
                            int dishesCount = userDishes.size();
                            if (restaurant.getLatitude() != null && restaurant.getLongitude() != null) {
                                LatLng location = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
                                mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title(restaurant.getName())
                                        .snippet("Nota media: " + averageRating + " | Platos pedidos: " + dishesCount));

                            } else {
                                Log.e("MapsActivity", "Coordenadas no disponibles para el restaurante: " + restaurant.getName());
                            }
                        }
                    }

                    // Centrar la cámara en el primer restaurante (opcional)
                    if (!response.body().isEmpty()) {
                        Restaurant firstRestaurant = response.body().get(0);
                        LatLng firstLocation = new LatLng(firstRestaurant.getLatitude(), firstRestaurant.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10));
                    }
                } else {
                    Log.e("MapsActivity", "Error al cargar restaurantes: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Restaurant>> call, @NonNull Throwable t) {
                Log.e("MapsActivity", "Error en la API: " + t.getMessage());
            }
        });
    }

}