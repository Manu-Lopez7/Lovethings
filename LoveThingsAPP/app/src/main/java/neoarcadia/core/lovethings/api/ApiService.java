package neoarcadia.core.lovethings.api;

import java.util.List;

import neoarcadia.core.lovethings.login.LoginRequest;
import neoarcadia.core.lovethings.login.LoginResponse;
import neoarcadia.core.lovethings.models.Restaurant;
import neoarcadia.core.lovethings.singup.MessageResponse;
import neoarcadia.core.lovethings.singup.SignupRequest;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @POST("/api/auth/signin")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
    @POST("/api/auth/signup")
    Call<MessageResponse> register(@Body SignupRequest signupRequest);
    @Multipart
    @POST("/api/dishes/add")
    Call<Void> addDish(
            @Part("name") RequestBody name,
            @Part("notes") RequestBody notes,
            @Part("price") RequestBody price,
            @Part("rating") RequestBody rating,
            @Part("waitTime") RequestBody waitTime,
            @Part("restaurantId") RequestBody restaurantId,
            @Part MultipartBody.Part image
    );
    @GET("/api/restaurants")
    Call<List<Restaurant>> getAllRestaurants();


}
