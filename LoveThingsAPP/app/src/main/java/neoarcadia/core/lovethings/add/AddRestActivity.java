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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;

import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.frames.FeedActivity;
import neoarcadia.core.lovethings.utils.FileUtils;
import neoarcadia.core.lovethings.utils.ImageUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRestActivity extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageButton restaurantImage;
    private EditText restaurantName, restaurantAddress, restaurantCategory;
    private EditText restaurantPhone, restaurantMenuLink, restaurantHours;
    private Uri selectedImageUri;
    private Button addBtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_rest, container, false);

        restaurantImage = view.findViewById(R.id.btnimg);
        restaurantName = view.findViewById(R.id.restname);
        restaurantAddress = view.findViewById(R.id.restaddr);
        restaurantCategory = view.findViewById(R.id.restcat);
        restaurantPhone = view.findViewById(R.id.restnum);
        restaurantMenuLink = view.findViewById(R.id.restlink);
        restaurantHours = view.findViewById(R.id.resttime);
        addBtn = view.findViewById(R.id.btnadd);

        restaurantImage.setOnClickListener(v -> openImageChooser());
        addBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                uploadRestaurant();
            } else {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        //startActivityForResult(intent, PICK_IMAGE_REQUEST);
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
                                65   // Calidad de compresión (0-100)
                        );

                        // Muestra la imagen comprimida en el botón
                        Bitmap bitmap = BitmapFactory.decodeFile(compressedImageFile.getAbsolutePath());
                        restaurantImage.setImageBitmap(bitmap);

                        // Actualiza el URI para el archivo comprimido
                        selectedImageUri = Uri.fromFile(compressedImageFile);

                    } catch (IOException e) {
                        Log.e("AddRestActivity", "Error al procesar la imagen", e);
                    }
                }
            }
    );
    private boolean validateInputs() {
        Log.e("AddRestActivity", "Validando inputs");
        Log.e("AddRestActivity", "Restaurant Name: " + restaurantName.getText().toString());
        Log.e("AddRestActivity", "Restaurant Address: " + restaurantAddress.getText().toString());
        Log.e("AddRestActivity", "Restaurant Category: " + restaurantCategory.getText().toString());
        Log.e("AddRestActivity", "Restaurant Phone: " + restaurantPhone.getText().toString());
        Log.e("AddRestActivity", "Restaurant Menu Link: " + restaurantMenuLink.getText().toString());
        Log.e("AddRestActivity", "Restaurant Hours: " + restaurantHours.getText().toString());

        return !restaurantName.getText().toString().isEmpty()
                && !restaurantAddress.getText().toString().isEmpty()
                && !restaurantCategory.getText().toString().isEmpty()
                && !restaurantPhone.getText().toString().isEmpty()
                && !restaurantMenuLink.getText().toString().isEmpty()
                && !restaurantHours.getText().toString().isEmpty();
    }

    private void uploadRestaurant() {
        ApiService apiService = ApiClient.getRetrofitInstance(requireContext()).create(ApiService.class);

        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), restaurantName.getText().toString());
        RequestBody address = RequestBody.create(MediaType.parse("text/plain"), restaurantAddress.getText().toString());
        RequestBody category = RequestBody.create(MediaType.parse("text/plain"), restaurantCategory.getText().toString());
        RequestBody phone = RequestBody.create(MediaType.parse("text/plain"), restaurantPhone.getText().toString());
        RequestBody menuLink = RequestBody.create(MediaType.parse("text/plain"), restaurantMenuLink.getText().toString());
        RequestBody hours = RequestBody.create(MediaType.parse("text/plain"), restaurantHours.getText().toString());

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File file = new File(FileUtils.getPath(requireContext(), selectedImageUri));
            RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), file);
            imagePart = MultipartBody.Part.createFormData("image", file.getName(), imageBody);
        }

        Call<Void> call = apiService.addRestaurant(name, address, category, phone, menuLink, hours, imagePart);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Restaurante añadido con éxito", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_container, new FeedActivity())
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(requireContext(), "Error al añadir el restaurante", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AddRestActivity", "Error al subir restaurante", t);
            }
        });
    }
}
