package neoarcadia.core.lovethings.utils;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import neoarcadia.core.lovethings.FavActivity;
import neoarcadia.core.lovethings.MainActivity;
import neoarcadia.core.lovethings.MapsActivity;
import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.SearchActivity;
import neoarcadia.core.lovethings.add.AddDishActivity;
import neoarcadia.core.lovethings.add.AddRestActivity;
import neoarcadia.core.lovethings.login.LoginActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton postBtn;
    private ImageButton settingsBtn;
    private Button logoutButton, addRestaurantButton, changePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
        String roles = sharedPreferences.getString("roles", "");


        postBtn = findViewById(R.id.btnpost);
        settingsBtn = findViewById(R.id.btnsettings);
        logoutButton = findViewById(R.id.btncs);
        addRestaurantButton = findViewById(R.id.btnar);
        changePasswordButton = findViewById(R.id.btnsg);
        BottomNavigationView navigationBar = findViewById(R.id.navigationbar);
        if (!roles.contains("ROLE_ADMIN")) {
            addRestaurantButton.setVisibility(View.GONE);
        }
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
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        addRestaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AddRestActivity.class);
                startActivity(intent);
            }
        });
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ChangePassActivity.class);
                startActivity(intent);
            }
        });
    }
    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("jwt_token");
        editor.apply();

        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

