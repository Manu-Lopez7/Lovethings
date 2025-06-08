package neoarcadia.core.lovethings.api;

import java.util.List;
import java.util.Map;

import neoarcadia.core.lovethings.login.LoginRequest;
import neoarcadia.core.lovethings.login.LoginResponse;
import neoarcadia.core.lovethings.models.Restaurant;
import neoarcadia.core.lovethings.singup.MessageResponse;
import neoarcadia.core.lovethings.singup.SignupRequest;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/auth/signin")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
    @POST("/api/auth/signup")
    Call<MessageResponse> register(@Body SignupRequest signupRequest);
    @Multipart
    @POST("/api/dishes/add")
    Call<Void> addDish(
            @Part("name") RequestBody name,
            @Part("price") RequestBody price,
            @Part("waitTime") RequestBody waitTime,
            @Part("rating") RequestBody rating,
            @Part("notes") RequestBody notes,
            @Part("restaurantId") RequestBody restaurantId,
            @Part MultipartBody.Part image
    );
    @GET("/api/restaurants/getall")
    Call<List<Restaurant>> getAllRestaurants();
    @GET("/api/restaurants/{id}")
    Call<Restaurant> getRestaurantById(@Path("id") Long id);

    @GET("/api/restaurants/user")
    Call<List<Restaurant>> getRestaurantsByUser();
    @POST("/api/auth/change-password")
    Call<MessageResponse> changePassword(@Body Map<String, String> changePasswordRequest);
    @Multipart
    @POST("restaurants/add")
    Call<Void> addRestaurant(
            @Part("name") RequestBody name,
            @Part("address") RequestBody address,
            @Part("category") RequestBody category,
            @Part("phoneNumber") RequestBody phoneNumber,
            @Part("menuLink") RequestBody menuLink,
            @Part("hours") RequestBody hours,
            @Part MultipartBody.Part image
    );
    @PATCH("/api/dishes/favorite/{id}")
    Call<Void> updateFavoriteStatus(@Path("id") Long id, @Query("isFavorite") boolean isFavorite);
    @DELETE("/api/dishes/delete/{id}")
    Call<Void> deleteDish(@Path("id") Long id);

    @Multipart
    @PUT("/api/restaurants/update/{id}")
    Call<Void> updateRestaurant(
            @Path("id") Long id,
            @Part("name") RequestBody name,
            @Part("address") RequestBody address,
            @Part("category") RequestBody category,
            @Part("phoneNumber") RequestBody phoneNumber,
            @Part("menuLink") RequestBody menuLink,
            @Part("hours") RequestBody hours,
            @Part MultipartBody.Part image
    );
    @Multipart
    @PUT("/api/dishes/update/{id}")
    Call<Void> updateDish(
            @Path("id") Long id,
            @Part("name") RequestBody name,
            @Part("price") RequestBody price,
            @Part("waitTime") RequestBody waitTime,
            @Part("rating") RequestBody rating,
            @Part("notes") RequestBody notes,
            @Part MultipartBody.Part image
    );
    @GET("admin/users")
    Call<List<String>> getAllUsernames(@Header("Authorization") String token);

    @FormUrlEncoded
    @POST("admin/assign-role")
    Call<ResponseBody> assignRole(
            @Header("Authorization") String token,
            @Field("username") String username,
            @Field("roleName") String roleName
    );
}
