package neoarcadia.core.lovethings.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class ApiClient {

    private static Retrofit retrofit;
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";

    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Log.d("ApiClient", "Request URL: " + original.url());
                        Log.d("ApiClient", "Request Headers: " + original.headers());
                        Request.Builder builder = original.newBuilder();

                        SharedPreferences sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                        String token = sharedPreferences.getString("jwt_token", null);

                        if (token != null) {
                            builder.addHeader("Authorization", "Bearer " + token);
                            Log.d("ApiClient", "Add jwt succefully: Bearer " + token);
                        } else {
                            Log.e("ApiClient", "JWT = NULL!");
                        }


                        Request request = builder.build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
