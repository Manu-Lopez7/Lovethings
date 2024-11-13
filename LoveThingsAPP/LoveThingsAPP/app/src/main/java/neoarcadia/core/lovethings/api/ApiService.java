package neoarcadia.core.lovethings.api;

import neoarcadia.core.lovethings.login.LoginRequest;
import neoarcadia.core.lovethings.login.LoginResponse;
import neoarcadia.core.lovethings.singup.MessageResponse;
import neoarcadia.core.lovethings.singup.SignupRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/signin")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
    @POST("/api/auth/signup")
    Call<MessageResponse> register(@Body SignupRequest signupRequest);
}
