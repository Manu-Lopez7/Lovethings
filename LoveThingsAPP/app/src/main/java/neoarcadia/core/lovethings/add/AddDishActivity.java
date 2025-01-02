package neoarcadia.core.lovethings.add;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import neoarcadia.core.lovethings.FavActivity;
import neoarcadia.core.lovethings.MainActivity;
import neoarcadia.core.lovethings.MapsActivity;
import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.SearchActivity;
import neoarcadia.core.lovethings.utils.SettingsActivity;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.models.Restaurant;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDishActivity extends AppCompatActivity {
    private Spinner restaurantSpinner;
    private EditText nameEditText, notesEditText, priceEditText, ratingEditText, waitTimeEditText;
    private ImageButton imageButton, postBtn, settingsBtn;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Restaurant> restaurantList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dish);

        restaurantSpinner = findViewById(R.id.restaurants_spinner);
        nameEditText = findViewById(R.id.nameds);
        notesEditText = findViewById(R.id.noteds);
        priceEditText = findViewById(R.id.price);
        ratingEditText = findViewById(R.id.ratingmed);
        waitTimeEditText = findViewById(R.id.timewait);
        imageButton = findViewById(R.id.dishimg);
        postBtn = findViewById(R.id.btnpost);
        settingsBtn = findViewById(R.id.btnsettings);

        loadRestaurants();

        imageButton.setOnClickListener(v -> openImageChooser());

        postBtn.setOnClickListener(v -> sendDishToApi());

        settingsBtn.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

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
    }

    private void loadRestaurants() {
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        Call<List<Restaurant>> call = apiService.getAllRestaurants();
        call.enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    restaurantList = response.body();
                    List<String> restaurantNames = new ArrayList<>();
                    for (Restaurant restaurant : restaurantList) {
                        restaurantNames.add(restaurant.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddDishActivity.this, android.R.layout.simple_spinner_item, restaurantNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    restaurantSpinner.setAdapter(adapter);
                } else {
                    Log.e("AddDishActivity", "Error al cargar los restaurantes");
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Log.e("AddDishActivity", "Fallo en la API: " + t.getMessage());
            }
        });
    }

    private void sendDishToApi() {
        ApiService apiService = ApiClient.getRetrofitInstance(this).create(ApiService.class);
        int selectedRestaurantPosition = restaurantSpinner.getSelectedItemPosition();
        if (selectedRestaurantPosition == -1 || restaurantList.isEmpty()) {
            Log.e("AddDishActivity", "No se seleccionó un restaurante");
            return;
        }

        Restaurant selectedRestaurant = restaurantList.get(selectedRestaurantPosition);
        RequestBody namePart = RequestBody.create(MediaType.parse("text/plain"), nameEditText.getText().toString());
        RequestBody pricePart = RequestBody.create(MediaType.parse("text/plain"), priceEditText.getText().toString());
        RequestBody waitTimePart = RequestBody.create(MediaType.parse("text/plain"), waitTimeEditText.getText().toString());
        RequestBody ratingPart = RequestBody.create(MediaType.parse("text/plain"), ratingEditText.getText().toString());
        RequestBody notesPart = RequestBody.create(MediaType.parse("text/plain"), notesEditText.getText().toString());
        RequestBody restaurantIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedRestaurant.getId()));

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            try {
                File imageFile = getFileFromUri(selectedImageUri);
                RequestBody filePart = RequestBody.create(MediaType.parse("image/*"), imageFile);
                imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), filePart);
            } catch (IOException e) {
                Log.e("AddDishActivity", "Error al procesar la imagen", e);
            }
        }
        Call<Void> call = apiService.addDish(namePart, pricePart, notesPart, waitTimePart, ratingPart, restaurantIdPart, imagePart);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("AddDishActivity", "Plato añadido con éxito");
                    startActivity(new Intent(AddDishActivity.this, AddDishActivity.class));
                } else {
                    Log.e("AddDishActivity", "Error al añadir plato: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("AddDishActivity", "Fallo en la API: " + t.getMessage());
            }
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
                imageButton.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.e("AddDishActivity", "Error al cargar la imagen", e);
            }
        }
    }
    private File getFileFromUri(Uri uri) throws IOException {
        File tempFile = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
}
