package neoarcadia.core.lovethings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import neoarcadia.core.lovethings.adapter.DishAdapter;
import neoarcadia.core.lovethings.add.AddDishActivity;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.models.Dish;
import neoarcadia.core.lovethings.models.Restaurant;
import neoarcadia.core.lovethings.utils.SettingsActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavActivity extends AppCompatActivity {
    private RecyclerView dishRecyclerView;
    private DishAdapter dishAdapter;
    private List<Dish> allDishes = new ArrayList<>();
    private List<Dish> filteredDishes = new ArrayList<>();
    private EditText searchQuery;
    private Button searchButton;
    private ImageButton postBtn;
    private ImageButton settingsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dishRecyclerView = findViewById(R.id.dishRecyclerView);
        searchQuery = findViewById(R.id.search_query);
        searchButton = findViewById(R.id.search_button);
        postBtn = findViewById(R.id.btnpost);
        settingsBtn = findViewById(R.id.btnsettings);

        dishRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dishAdapter = new DishAdapter(filteredDishes, this);
        dishRecyclerView.setAdapter(dishAdapter);

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
        Log.d("FavActivity", "RecyclerView tiene adaptador: " + (dishRecyclerView.getAdapter() != null));

        loadDishes();
        searchButton.setOnClickListener(v -> {
            String query = searchQuery.getText().toString().toLowerCase().trim();
            List<Dish> searchResults = filteredDishes.stream()
                    .filter(dish -> dish.getName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            dishAdapter.updateDishes(searchResults);
        });

    }
    private void loadDishes() {
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        Call<List<Restaurant>> call = apiService.getRestaurantsByUser();
        call.enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(@NonNull Call<List<Restaurant>> call, @NonNull Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
                    long userId = sharedPreferences.getLong("user_id", -1);
                    List<Dish> userDishes = new ArrayList<>();
                    Log.d("FavActivity", "Datos recibidos de la API:");
                    for (Restaurant restaurant : response.body()) {
                        Log.d("FavActivity", "Restaurante: " + restaurant.getName());
                        for (Dish dish : restaurant.getDishes()) {
                            Log.d("FavActivity", "Plato: " + dish.getName() + ", Usuario: " + (dish.getUser() != null ? dish.getUser().getUsername() : "null"));
                            if (dish.getUser() != null && dish.getUser().getId() == userId && dish.isFavorite()) {
                                userDishes.add(dish);
                            }
                        }
                    }
                    Log.d("FavActivity", "Platos después del filtrado:");
                    for (Dish dish : userDishes) {
                        Log.d("FavActivity", "Plato: " + dish.getName());
                    }
                    filteredDishes.clear();
                    filteredDishes.addAll(userDishes);
                    Log.d("FavActivity", "Filtrados: " + filteredDishes.size());
                    for (Dish dish : filteredDishes) {
                        Log.d("FavActivity", "Plato: " + dish.getName());
                    }
                    dishAdapter.updateDishes(filteredDishes);
                } else {
                    Log.e("FavActivity", "Error al cargar platos: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Restaurant>> call, @NonNull Throwable t) {
                Log.e("FavActivity", "Error en la API: " + t.getMessage());
            }
        });
    }

}