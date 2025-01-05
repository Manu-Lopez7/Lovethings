package neoarcadia.core.lovethings;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import neoarcadia.core.lovethings.add.AddDishActivity;
import neoarcadia.core.lovethings.add.AddRestActivity;
import neoarcadia.core.lovethings.frames.FavActivity;
import neoarcadia.core.lovethings.frames.FeedActivity;
import neoarcadia.core.lovethings.frames.MapsActivity;
import neoarcadia.core.lovethings.frames.SearchActivity;
import neoarcadia.core.lovethings.login.LoginActivity;
import neoarcadia.core.lovethings.utils.ChangePassActivity;

public class MainFrameActivity extends AppCompatActivity {
    private ImageButton postBtn;
    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_frame);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        BottomNavigationView navigationBar = findViewById(R.id.navigationbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);
        String roles = sharedPreferences.getString("roles", "");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (!roles.contains("ROLE_ADMIN")){
            navigationView.getMenu().findItem(R.id.add_restaurant).setVisible(false);
        }

        if (token != null) {
            Log.d("MainActivity", "User logged in!");
            if (savedInstanceState == null) {
                // Carga el fragment inicial (por ejemplo, el de búsqueda)
                loadFragment(new FeedActivity());
            }
        } else {
            Log.e("ApiClient", "User not logged in! Redirecting to login activity");
            startActivity(new Intent(this, LoginActivity.class));
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.add_restaurant) {
                loadFragment(new AddRestActivity());
            } else if (itemId == R.id.change_pass) {
                loadFragment(new ChangePassActivity());
            } else if (itemId == R.id.menu_signout) {
                logout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        postBtn = findViewById(R.id.btnpost);

        navigationBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
                loadFragment(new FeedActivity());
                return true;
            } else if (itemId == R.id.menu_search) {
                loadFragment(new SearchActivity());
                return true;
            } else if (itemId == R.id.menu_post) {
                loadFragment(new AddDishActivity());
                return true;
            } else if (itemId == R.id.menu_location) {
                loadFragment(new MapsActivity());
                return true;
            } else if (itemId == R.id.menu_profile) {
                loadFragment(new FavActivity());
                return true;
            } else {
                return false;
            }
        });
        postBtn.setOnClickListener(view -> {
            loadFragment(new AddDishActivity());
        });

    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit();
    }
    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("jwt_token");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
