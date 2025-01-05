package neoarcadia.core.lovethings.frames;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.adapter.RestaurantAdapter;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.models.Restaurant;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedActivity extends Fragment {
    private List<Restaurant> restaurantList = new ArrayList<>();

    private RecyclerView restaurantRecyclerView;

    private RestaurantAdapter restaurantAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_feed, container, false);

        restaurantRecyclerView = view.findViewById(R.id.feedRecyclerView);
        restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadRestaurants();

        return view;
    }

    private void loadRestaurants() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("user_id", -1);

        ApiService apiService = ApiClient.getRetrofitInstance(requireContext()).create(ApiService.class);
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

                    RestaurantAdapter adapter = new RestaurantAdapter(requireContext(), filteredRestaurants);
                    restaurantRecyclerView.setAdapter(adapter);
                } else {
                    Log.e("MainActivity", "Error al cargar los restaurantes");
                }
            }
            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Log.e("MainActivity", "Error: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
