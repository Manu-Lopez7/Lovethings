package neoarcadia.core.lovethings.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import neoarcadia.core.lovethings.MainFrameActivity;
import neoarcadia.core.lovethings.frames.FeedActivity;
import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.singup.RegisterActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 100;
    private EditText userEditText, passwordEditText;
    private Button loginButton;
    private TextView errorText;

    private TextView registerButton;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userEditText = findViewById(R.id.user);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_btn);
        errorText = findViewById(R.id.errorform);
        registerButton = findViewById(R.id.sign_up);
        requestStoragePermission();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

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
                    Gson gson = new Gson();
                    String rolesJson = gson.toJson(loginResponse.getRoles());
                    SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("is_logged_in", true);
                    editor.putLong(("user_id"), loginResponse.getId());
                    editor.putString("jwt_token", loginResponse.getToken());
                    editor.putString("username", loginResponse.getUsername());
                    editor.putString("email", loginResponse.getEmail());
                    editor.putString("roles", rolesJson);
                    editor.apply();
                    Log.d("LoginActivity", "JWT Token Guardado: " + loginResponse.getToken());
                    Log.d("LoginActivity", "User ID Guardado: " + loginResponse.getId());
                    Log.d("LoginActivity", "Email Guardado: " + loginResponse.getEmail());
                    Log.d("LoginActivity", "Username Guardado: " + loginResponse.getUsername());
                    Log.d("LoginActivity", "Roles Guardado: " + loginResponse.getRoles());
                    Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainFrameActivity.class);
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
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Manejo de permisos para Android 11 o superior
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            } else {
                login(userEditText.getText().toString(), passwordEditText.getText().toString());
            }
        } else {
            // Manejo de permisos para versiones anteriores a Android 11
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                login(userEditText.getText().toString(), passwordEditText.getText().toString());
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                login(userEditText.getText().toString(), passwordEditText.getText().toString());
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado. No se puede proceder.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                login(userEditText.getText().toString(), passwordEditText.getText().toString());
            } else {
                Toast.makeText(this, "Permiso de acceso completo a almacenamiento denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
//----------------------------------------------------------------

    // Recuperar roles de SharedPreferences
    //String rolesJson = sharedPreferences.getString("roles", "[]");
    //List<String> roles = gson.fromJson(rolesJson, new TypeToken<List<String>>() {}.getType());
