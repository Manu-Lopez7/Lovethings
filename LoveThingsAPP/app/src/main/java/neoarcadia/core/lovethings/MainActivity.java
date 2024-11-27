package neoarcadia.core.lovethings;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.models.Restaurant;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RestaurantAdapter restaurantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.feedRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Cargar la lista de restaurantes
        loadRestaurants();
    }

    private void loadRestaurants() {
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);

        Call<List<Restaurant>> call = apiService.getAllRestaurants();
        call.enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    restaurantAdapter = new RestaurantAdapter(response.body());
                    recyclerView.setAdapter(restaurantAdapter);
                } else {
                    Log.e("MainActivity", "Failed to load restaurants: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Log.e("MainActivity", "Error: " + t.getMessage(), t);
            }
        });
    }
}
