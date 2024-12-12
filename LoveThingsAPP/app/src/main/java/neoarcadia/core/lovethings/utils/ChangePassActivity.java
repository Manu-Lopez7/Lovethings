package neoarcadia.core.lovethings.utils;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

import neoarcadia.core.lovethings.FavActivity;
import neoarcadia.core.lovethings.MainActivity;
import neoarcadia.core.lovethings.MapsActivity;
import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.SearchActivity;
import neoarcadia.core.lovethings.add.AddDishActivity;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.singup.MessageResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassActivity extends AppCompatActivity {

    private ImageButton postBtn;
    private ImageButton settingsBtn;
    private EditText oldPass, newPass, cnfPass;
    private Button cbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        postBtn = findViewById(R.id.btnpost);
        settingsBtn = findViewById(R.id.btnsettings);
        oldPass = findViewById(R.id.oldpass);
        newPass = findViewById(R.id.newpass);
        cnfPass = findViewById(R.id.cnfpass);
        cbtn = findViewById(R.id.cbtn);

        BottomNavigationView navigationBar = findViewById(R.id.navigationbar);
        navigationBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.menu_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (itemId == R.id.menu_post) {
                startActivity(new Intent(this, AddDishActivity.class));
                return true;
            } else if (itemId == R.id.menu_location) {
                startActivity(new Intent(this, MapsActivity.class));
                return true;
            } else if (itemId == R.id.menu_profile) {
                startActivity(new Intent(this, FavActivity.class));
                return true;
            } else {
                return false;
            }
        });
        postBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, AddDishActivity.class));
        });
        settingsBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
        cbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPass.getText().toString();
                String newPassword = newPass.getText().toString();
                String confirmPassword = cnfPass.getText().toString();

                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ChangePassActivity.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(ChangePassActivity.this, "Las contraseñas nuevas no coinciden.", Toast.LENGTH_SHORT).show();
                    return;
                }

                changePassword(oldPassword, newPassword);
            }
        });
    }

    private void changePassword(String oldPassword, String newPassword) {
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);

        Map<String, String> changePasswordRequest = new HashMap<>();
        changePasswordRequest.put("oldPassword", oldPassword);
        changePasswordRequest.put("newPassword", newPassword);

        Call<MessageResponse> call = apiService.changePassword(changePasswordRequest);

        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePassActivity.this, "Contraseña cambiada con éxito.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ChangePassActivity.this, "Error al cambiar la contraseña.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(ChangePassActivity.this, "Error en la conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
