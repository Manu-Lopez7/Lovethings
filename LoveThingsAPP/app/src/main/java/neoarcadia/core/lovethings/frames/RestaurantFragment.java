package neoarcadia.core.lovethings.frames;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import neoarcadia.core.lovethings.R;


public class RestaurantFragment extends Fragment {
    private RecyclerView dishRecyclerView;
    private DishAdapter dishAdapter;
    private long restid;
    private String restname;
    private List<Dish> filteredDishes = new ArrayList<>();
    private EditText searchQuery;
    private Button searchButton;
    private TextView infoUser, infoRest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fav, container, false);

        dishRecyclerView = view.findViewById(R.id.dishRecyclerView);
        searchQuery = view.findViewById(R.id.search_query);
        searchButton = view.findViewById(R.id.search_button);
        infoUser = view.findViewById(R.id.infouser);
        infoRest = view.findViewById(R.id.infolayout);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Usuario");
        if (getArguments() != null) {
            restid = getArguments().getLong("restaurant_id");
            Log.d("RestaurantFragment", "Recibido restid: " + restid);
            restname = getArguments().getString("restaurant_name");
            Log.d("RestaurantFragment", "Recibido restname: " + restname);
        }

        infoUser.setText(username);
        infoRest.setText(restname);

        dishRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        dishAdapter = new DishAdapter(filteredDishes, requireContext());
        dishRecyclerView.setAdapter(dishAdapter);

        Log.d("RestaurantFragment", "RecyclerView tiene adaptador: " + (dishRecyclerView.getAdapter() != null));

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
                    Log.d("RestaurantFragment", "Datos recibidos de la API:");
                    for (Restaurant restaurant : response.body()) {
                        Log.d("RestaurantFragment", "Restaurante: " + restaurant.getName());
                        for (Dish dish : restaurant.getDishes()) {
                            Log.d("RestaurantFragment", "Plato: " + dish.getName() + ", Usuario: " + (dish.getUser() != null ? dish.getUser().getUsername() : "null"));
                            // Aqui es donde se filtra si es del restaurante y si es del usuario
                            Log.d("RestaurantFragment", "Usuario: " + dish.getUser().getId() + ", Restaurante: " + dish.getRestaurantId()+" / " + restid);
                            if (dish.getUser() != null && dish.getUser().getId() == userId && dish.getRestaurantId() == restid) {
                                userDishes.add(dish);
                            }
                        }
                    }
                    Log.d("RestaurantFragment", "Platos después del filtrado:");
                    for (Dish dish : userDishes) {
                        Log.d("FavActivity", "Plato: " + dish.getName());
                    }

                    filteredDishes.clear();
                    filteredDishes.addAll(userDishes);
                    Log.d("RestaurantFragment", "Filtrados: " + filteredDishes.size());
                    for (Dish dish : filteredDishes) {
                        Log.d("RestaurantFragment", "Plato: " + dish.getName());
                    }
                    dishAdapter.updateDishes(filteredDishes);
                } else {
                    Log.e("RestaurantFragment", "Error al cargar platos: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Restaurant>> call, @NonNull Throwable t) {
                Log.e("RestaurantFragment", "Error en la API: " + t.getMessage());
            }
        });
    }
}

