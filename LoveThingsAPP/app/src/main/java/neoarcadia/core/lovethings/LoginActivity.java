package neoarcadia.core.lovethings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.login.LoginRequest;
import neoarcadia.core.lovethings.login.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText userEditText, passwordEditText;
    private Button loginButton;
    private TextView errorText;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userEditText = findViewById(R.id.user);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_btn);
        errorText = findViewById(R.id.errorform);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    errorText.setText("Please fill in all fields");
                    errorText.setVisibility(View.VISIBLE);
                } else {
                    errorText.setVisibility(View.INVISIBLE);
                    login(username, password);
                }
            }
        });
    }

    private void login(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);

        Call<LoginResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("jwt_token", loginResponse.getToken());
                    editor.putString("username", loginResponse.getUsername());
                    editor.apply();
                    Log.d("LoginActivity", "JWT Token Guardado: " + loginResponse.getToken());

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "Error logging in: " + response.message());
                    errorText.setText("Invalid credentials. Please try again.");
                    errorText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Error connecting to server", t);
                errorText.setText("Error connecting to server");
                errorText.setVisibility(View.VISIBLE);
            }
        });
    }
}
