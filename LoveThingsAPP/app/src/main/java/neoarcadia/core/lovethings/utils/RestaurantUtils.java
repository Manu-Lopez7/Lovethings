package neoarcadia.core.lovethings.utils;

import android.content.Context;
import android.util.Log;

import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.models.Restaurant;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantUtils {

    public interface RestaurantCallback {
        void onRestaurantNameFetched(String restaurantName);
        void onError(String errorMessage);
    }

    public static void fetchRestaurantName(Context context, Long restaurantId, RestaurantCallback callback) {
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
        Call<Restaurant> call = apiService.getRestaurantById(restaurantId);

        call.enqueue(new Callback<Restaurant>() {
            @Override
            public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String restaurantName = response.body().getName();
                    Log.d("RestaurantUtils", "Nombre del restaurante: " + restaurantName);
                    callback.onRestaurantNameFetched(restaurantName);
                } else {
                    String errorMessage = "Error al obtener el restaurante: " + response.code();
                    Log.e("RestaurantUtils", errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Restaurant> call, Throwable t) {
                String errorMessage = "Error en la API: " + t.getMessage();
                Log.e("RestaurantUtils", errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
}
