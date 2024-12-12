package neoarcadia.core.lovethings.add;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;

import neoarcadia.core.lovethings.FavActivity;
import neoarcadia.core.lovethings.MainActivity;
import neoarcadia.core.lovethings.MapsActivity;
import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.SearchActivity;
import neoarcadia.core.lovethings.utils.SettingsActivity;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.utils.FileUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRestActivity extends AppCompatActivity {
    private ImageButton postBtn;
    private ImageButton settingsBtn;
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageButton restaurantImage;
    private EditText restaurantName, restaurantAddress, restaurantCategory;
    private EditText restaurantPhone, restaurantMenuLink, restaurantHours;
    private Uri selectedImageUri;
    private Button addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rest);

        postBtn = findViewById(R.id.btnpost);
        settingsBtn = findViewById(R.id.btnsettings);
        restaurantImage = findViewById(R.id.btnimg);
        restaurantName = findViewById(R.id.restname);
        restaurantAddress = findViewById(R.id.restaddr);
        restaurantCategory = findViewById(R.id.restcat);
        restaurantPhone = findViewById(R.id.restnum);
        restaurantMenuLink = findViewById(R.id.restlink);
        restaurantHours = findViewById(R.id.resttime);
        addBtn = findViewById(R.id.btnadd);

        restaurantImage.setOnClickListener(v -> openImageChooser());
        addBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                uploadRestaurant();
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

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
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                restaurantImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.e("AddRestActivity", "Error al cargar la imagen", e);
            }
        }
    }

    private boolean validateInputs() {
        return !restaurantName.getText().toString().isEmpty()
                && !restaurantAddress.getText().toString().isEmpty()
                && !restaurantCategory.getText().toString().isEmpty()
                && !restaurantPhone.getText().toString().isEmpty()
                && !restaurantMenuLink.getText().toString().isEmpty()
                && !restaurantHours.getText().toString().isEmpty();
    }

    private void uploadRestaurant() {
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);

        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), restaurantName.getText().toString());
        RequestBody address = RequestBody.create(MediaType.parse("text/plain"), restaurantAddress.getText().toString());
        RequestBody category = RequestBody.create(MediaType.parse("text/plain"), restaurantCategory.getText().toString());
        RequestBody phone = RequestBody.create(MediaType.parse("text/plain"), restaurantPhone.getText().toString());
        RequestBody menuLink = RequestBody.create(MediaType.parse("text/plain"), restaurantMenuLink.getText().toString());
        RequestBody hours = RequestBody.create(MediaType.parse("text/plain"), restaurantHours.getText().toString());

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File file = new File(FileUtils.getPath(this, selectedImageUri));
            RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), file);
            imagePart = MultipartBody.Part.createFormData("image", file.getName(), imageBody);
        }

        Call<Void> call = apiService.addRestaurant(name, address, category, phone, menuLink, hours, imagePart);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddRestActivity.this, "Restaurante añadido con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddRestActivity.this, "Error al añadir el restaurante", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddRestActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AddRestActivity", "Error al subir restaurante", t);
            }
        });
    }
}
