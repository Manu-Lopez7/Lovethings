package neoarcadia.core.lovethings.frames;

import androidx.annotation.Nullable;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.adapter.DishAdapter;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.models.Dish;
import neoarcadia.core.lovethings.models.Restaurant;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends Fragment {
    private RecyclerView dishRecyclerView;
    private DishAdapter dishAdapter;
    private List<Dish> filteredDishes = new ArrayList<>();
    private EditText searchQuery;
    private Button searchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_search, container, false);


        dishRecyclerView = view.findViewById(R.id.dishRecyclerView);
        searchQuery = view.findViewById(R.id.search_query);
        searchButton = view.findViewById(R.id.search_button);


        dishRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        dishAdapter = new DishAdapter(filteredDishes, requireContext());
        dishRecyclerView.setAdapter(dishAdapter);

        Log.d("SearchActivity", "RecyclerView tiene adaptador: " + (dishRecyclerView.getAdapter() != null));

        loadDishes();
        searchButton.setOnClickListener(v -> {
            String query = searchQuery.getText().toString().toLowerCase().trim();
            List<Dish> searchResults = filteredDishes.stream()
                    .filter(dish -> dish.getName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            dishAdapter.updateDishes(searchResults);
        });
        return view;
    }


    private void loadDishes() {
        ApiService apiService = ApiClient.getRetrofitInstance(requireContext()).create(ApiService.class);
        Call<List<Restaurant>> call = apiService.getRestaurantsByUser();
        call.enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(@NonNull Call<List<Restaurant>> call, @NonNull Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
                    long userId = sharedPreferences.getLong("user_id", -1);
                    List<Dish> userDishes = new ArrayList<>();
                    Log.d("SearchActivity", "Datos recibidos de la API:");
                    for (Restaurant restaurant : response.body()) {
                        Log.d("SearchActivity", "Restaurante: " + restaurant.getName());
                        for (Dish dish : restaurant.getDishes()) {
                            Log.d("SearchActivity", "Plato: " + dish.getName() + ", Usuario: " + (dish.getUser() != null ? dish.getUser().getUsername() : "null"));
                            if (dish.getUser() != null && dish.getUser().getId() == userId) {
                                userDishes.add(dish);
                            }
                        }
                    }
                    Log.d("SearchActivity", "Platos después del filtrado:");
                    for (Dish dish : userDishes) {
                        Log.d("SearchActivity", "Plato: " + dish.getName());
                    }
                    filteredDishes.clear();
                    filteredDishes.addAll(userDishes);
                    Log.d("SearchActivity", "Filtrados: " + filteredDishes.size());
                    for (Dish dish : filteredDishes) {
                        Log.d("SearchActivity", "Plato: " + dish.getName());
                    }
                    dishAdapter.updateDishes(filteredDishes);
                } else {
                    Log.e("SearchActivity", "Error al cargar platos: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Restaurant>> call, @NonNull Throwable t) {
                Log.e("SearchActivity", "Error en la API: " + t.getMessage());
            }
        });
    }

}