package neoarcadia.core.lovethings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import neoarcadia.core.lovethings.adapter.RestaurantAdapter;
import neoarcadia.core.lovethings.add.AddDishActivity;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.login.LoginActivity;
import neoarcadia.core.lovethings.models.Restaurant;
import neoarcadia.core.lovethings.utils.SettingsActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private List<Restaurant> restaurantList = new ArrayList<>();

    private RecyclerView restaurantRecyclerView;

    private RestaurantAdapter restaurantAdapter;
    private ImageButton postBtn;
    private ImageButton settingsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restaurantRecyclerView = findViewById(R.id.feedRecyclerView);
        restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(this));


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

        SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);

        if (token != null) {
            Log.d("MainActivity", "User logged in!");
        } else {
            Log.e("ApiClient", "User not logged in! Redirecting to login activity");
            startActivity(new Intent(this, LoginActivity.class));
        }


        loadRestaurants();
    }

    private void loadRestaurants() {
        SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("user_id", -1);

        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        Call<List<Restaurant>> call = apiService.getRestaurantsByUser();
        call.enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", "Restaurantes recibidos: " + response.body().toString());
                    List<Restaurant> allRestaurants = response.body();
                    List<Restaurant> filteredRestaurants = new ArrayList<>();

                    for (Restaurant restaurant : allRestaurants) {
                        if (restaurant.hasDishesByUser(userId)) {
                            filteredRestaurants.add(restaurant);
                        }
                    }
                    Log.d("API Response", new Gson().toJson(response.body()));

                    RestaurantAdapter adapter = new RestaurantAdapter(MainActivity.this, filteredRestaurants);
                    restaurantRecyclerView.setAdapter(adapter);
                } else {
                    Log.e("MainActivity", "Error al cargar los restaurantes");
                }
            }
            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Log.e("MainActivity", "Error: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
