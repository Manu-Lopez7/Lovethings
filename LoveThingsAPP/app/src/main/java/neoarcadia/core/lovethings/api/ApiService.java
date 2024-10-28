package neoarcadia.core.lovethings.api;

import neoarcadia.core.lovethings.login.LoginRequest;
import neoarcadia.core.lovethings.login.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/signin")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
}
