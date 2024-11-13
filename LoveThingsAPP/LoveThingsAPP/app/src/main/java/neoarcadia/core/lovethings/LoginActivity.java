package neoarcadia.core.lovethings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.login.LoginRequest;
import neoarcadia.core.lovethings.login.LoginResponse;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.login.PasswordUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText userEditText, passwordEditText;
    private Button loginButton;
    private TextView errorText;
    private TextView singup;

    private static final String TAG = "vArcadia";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userEditText = findViewById(R.id.user);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_btn);
        errorText = findViewById(R.id.errorform);
        singup = findViewById(R.id.sign_up);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                //String password = PasswordUtils.encryptPassword(passwordse);


                if (username.isEmpty() || password.isEmpty()) {
                    errorText.setText("Please fill in all fields");
                    errorText.setVisibility(View.VISIBLE);
                } else {
                    errorText.setVisibility(View.INVISIBLE);
                    login(username, password);
                }
            }
        });
        singup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    private void login(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        Log.d(TAG, "Login: " + loginRequest.toString());
        Log.d(TAG, "Login: "+ username + " " + password + " " + loginRequest.toString()+ " ");
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<LoginResponse> call = apiService.login(loginRequest);
        Log.i(TAG, "Login: " + call.request().url().toString());

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String roleMessage = getRoleMessage(loginResponse.getRoles());
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("ROLE_MESSAGE", roleMessage);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "Error logging in: " + response.message());
                    Log.e(TAG, "Error logging in: " + response.errorBody().toString());
                    Log.e(TAG, "Error logging in: " + response.raw().toString());
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

    private String getRoleMessage(List<String> roles) {
        if (roles.contains("ROLE_ADMIN")) return "ERES ADMINISTRADOR";
        if (roles.contains("ROLE_MODERATOR")) return "ERES MODERADOR";
        return "ERES USUARIO BÁSICO";
    }
}
