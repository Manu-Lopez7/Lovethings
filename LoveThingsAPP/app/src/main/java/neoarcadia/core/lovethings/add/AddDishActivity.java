package neoarcadia.core.lovethings.add;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.frames.FeedActivity;
import neoarcadia.core.lovethings.models.Restaurant;
import neoarcadia.core.lovethings.utils.ImageUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDishActivity extends Fragment {
    private Spinner restaurantSpinner;
    private EditText nameEditText, notesEditText, priceEditText, ratingEditText, waitTimeEditText;
    private ImageButton imageButton;
    private Uri selectedImageUri;
    private Button addDishButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Restaurant> restaurantList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_dish, container, false);

        restaurantSpinner = view.findViewById(R.id.restaurants_spinner);
        nameEditText = view.findViewById(R.id.nameds);
        notesEditText = view.findViewById(R.id.noteds);
        priceEditText = view.findViewById(R.id.price);
        ratingEditText = view.findViewById(R.id.ratingmed);
        waitTimeEditText = view.findViewById(R.id.timewait);
        imageButton = view.findViewById(R.id.dishimg);
        addDishButton = view.findViewById(R.id.add_button);


        loadRestaurants();

        imageButton.setOnClickListener(v -> openImageChooser());

        addDishButton.setOnClickListener(v -> sendDishToApi());
        return view;
    }

    private void loadRestaurants() {
        ApiService apiService = ApiClient.getRetrofitInstance(requireContext()).create(ApiService.class);
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, restaurantNames);
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
        ApiService apiService = ApiClient.getRetrofitInstance(requireContext()).create(ApiService.class);
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
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_container, new FeedActivity())
                            .addToBackStack(null)
                            .commit();                } else {
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
        imagePickerLauncher.launch(intent);
    }


    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        // Redimensiona y comprime la imagen seleccionada
                        File compressedImageFile = ImageUtils.resizeAndCompressImage(
                                requireContext(),
                                selectedImageUri,
                                800, // Ancho máximo deseado
                                80   // Calidad de compresión (0-100)
                        );

                        // Muestra la imagen comprimida en el botón
                        Bitmap bitmap = BitmapFactory.decodeFile(compressedImageFile.getAbsolutePath());
                        imageButton.setImageBitmap(bitmap);
                        // Actualiza el URI para el archivo comprimido
                        selectedImageUri = Uri.fromFile(compressedImageFile);

                    } catch (IOException e) {
                        Log.e("AddRestActivity", "Error al procesar la imagen", e);
                    }
                }
            }
    );
    private File getFileFromUri(Uri uri) throws IOException {
        File tempFile = new File(requireContext().getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
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
