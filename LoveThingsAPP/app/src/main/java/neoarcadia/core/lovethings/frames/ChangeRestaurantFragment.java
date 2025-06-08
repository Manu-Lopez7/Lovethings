package neoarcadia.core.lovethings.frames;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
import neoarcadia.core.lovethings.models.Restaurant;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChangeRestaurantFragment extends Fragment {

    private static final String TAG = "ChangeRestaurantFragment";
    private EditText nameEditText, addressEditText, phoneEditText, hoursEditText, categoryEditText, menuLinkEditText;
    private AutoCompleteTextView restSearch;

    private List<Restaurant> restaurantList;
    private ImageButton imageButton;
    private Button saveButton;
    private Restaurant restaurantToEdit;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                            imageButton.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            Log.e("ChangeRestFragment", "Error cargando imagen", e);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_restaurant, container, false);


        nameEditText = view.findViewById(R.id.restname);
        addressEditText = view.findViewById(R.id.restaddr);
        phoneEditText = view.findViewById(R.id.restnum);
        hoursEditText = view.findViewById(R.id.resttime);
        categoryEditText = view.findViewById(R.id.restcat);
        menuLinkEditText = view.findViewById(R.id.restlink);
        imageButton = view.findViewById(R.id.btnimg);
        saveButton = view.findViewById(R.id.btnadd);
        restSearch = view.findViewById(R.id.restaurants_search);

        loadRestaurants();

        restSearch.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            Restaurant selectedRestaurant = null;
            for (Restaurant r : restaurantList) {
                if (r.getName().equals(selectedName)) {
                    selectedRestaurant = r;
                    break;
                }
            }
            if (selectedRestaurant != null) {
                // Carga los datos en los campos de edición
                nameEditText.setText(selectedRestaurant.getName());
                addressEditText.setText(selectedRestaurant.getAddress());
                phoneEditText.setText(selectedRestaurant.getPhoneNumber());
                hoursEditText.setText(selectedRestaurant.getHours());
                categoryEditText.setText(selectedRestaurant.getCategory());
                menuLinkEditText.setText(selectedRestaurant.getMenuLink());
                // Guarda el restaurante seleccionado para actualizarlo después
                this.restaurantToEdit = selectedRestaurant;
            }
        });

        imageButton.setOnClickListener(v -> openImageChooser());
        saveButton.setOnClickListener(v -> updateRestaurant());

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void updateRestaurant() {
        String name = nameEditText.getText().toString();
        String address = addressEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String hours = hoursEditText.getText().toString();
        String category = categoryEditText.getText().toString();
        String menuLink = menuLinkEditText.getText().toString();

        ApiService apiService = ApiClient.getRetrofitInstance(requireContext()).create(ApiService.class);

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            try {
                File imageFile = getFileFromUri(selectedImageUri);
                RequestBody filePart = RequestBody.create(MediaType.parse("image/*"), imageFile);
                imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), filePart);
            } catch (IOException e) {
                Log.e("ChangeRestFragment", "Error al procesar imagen", e);
            }
        }

        RequestBody namePart = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody addressPart = RequestBody.create(MediaType.parse("text/plain"), address);
        RequestBody phonePart = RequestBody.create(MediaType.parse("text/plain"), phone);
        RequestBody hoursPart = RequestBody.create(MediaType.parse("text/plain"), hours);
        RequestBody categoryPart = RequestBody.create(MediaType.parse("text/plain"), category);
        RequestBody menuLinkPart = RequestBody.create(MediaType.parse("text/plain"), menuLink);

        Call<Void> call = apiService.updateRestaurant(
                restaurantToEdit.getId(),
                namePart, addressPart, phonePart, hoursPart, categoryPart, menuLinkPart, imagePart
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Restaurante actualizado correctamente", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar restaurante", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Fallo en la conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, restaurantNames);
                    restSearch.setAdapter(adapter);
                } else {
                    Log.e("ChangeRestFragment", "Error al cargar los restaurantes");
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                Log.e("ChangeRestFragment", "Fallo en la API: " + t.getMessage());
            }
        });
    }
}
