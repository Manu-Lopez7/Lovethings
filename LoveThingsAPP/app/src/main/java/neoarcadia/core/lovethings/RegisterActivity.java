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
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.singup.SignupRequest;
import neoarcadia.core.lovethings.singup.MessageResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView errorText;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.register_username);
        emailEditText = findViewById(R.id.register_email);
        passwordEditText = findViewById(R.id.register_password);
        confirmPasswordEditText = findViewById(R.id.register_confirm_password);
        registerButton = findViewById(R.id.register_btn);
        errorText = findViewById(R.id.register_error_text);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    errorText.setText("Please fill in all fields");
                    errorText.setVisibility(View.VISIBLE);
                } else if (!password.equals(confirmPassword)) {
                    errorText.setText("Passwords do not match");
                    errorText.setVisibility(View.VISIBLE);
                } else {
                    errorText.setVisibility(View.INVISIBLE);
                    registerUser(username, email, password);
                }
            }
        });
    }

    private void registerUser(String username, String email, String password) {
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        SignupRequest signupRequest = new SignupRequest(username, email, password);
        Call<MessageResponse> call = apiService.register(signupRequest);

        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    errorText.setText("Registration failed. Try again.");
                    errorText.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Registration failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                errorText.setText("Error connecting to server");
                errorText.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error connecting to server", t);
            }
        });
    }
}
