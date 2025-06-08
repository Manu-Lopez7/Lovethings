package neoarcadia.core.lovethings.frames;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.stream.Collectors;

import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.databinding.ActivityMapsBinding;
import neoarcadia.core.lovethings.models.Dish;
import neoarcadia.core.lovethings.models.Restaurant;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapfr);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MapsActivity", "No se pudo encontrar el fragmento del mapa");
        }
    return view;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ApiService apiService = ApiClient.getRetrofitInstance(requireContext()).create(ApiService.class);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
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
                                    .mapToDouble(Dish::getRating)
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