package neoarcadia.core.lovethings.frames;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.models.Dish;
import neoarcadia.core.lovethings.models.Restaurant;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeDishFragment extends Fragment {

    private static final String ARG_DISH = "dish";

    private EditText nameEditText, notesEditText, priceEditText, ratingEditText, waitTimeEditText;
    private ImageButton imageButton;
    private Button saveButton;
    private Uri selectedImageUri;
    private Dish dishToEdit;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static ChangeDishFragment newInstance(Dish dish) {
        ChangeDishFragment fragment = new ChangeDishFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DISH, dish);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recuperar el Dish del argumento
        if (getArguments() != null) {
            dishToEdit = (Dish) getArguments().getSerializable(ARG_DISH);
        }
        // Inicializar el launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                            imageButton.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            Log.e("ChangeDishFragment", "Error al cargar la imagen", e);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_dish, container, false);

        nameEditText = view.findViewById(R.id.nameds);
        notesEditText = view.findViewById(R.id.noteds);
        priceEditText = view.findViewById(R.id.price);
        ratingEditText = view.findViewById(R.id.ratingmed);
        waitTimeEditText = view.findViewById(R.id.timewait);
        imageButton = view.findViewById(R.id.dishimg);
        saveButton = view.findViewById(R.id.add_button);

        if (dishToEdit != null) {
            nameEditText.setText(dishToEdit.getName());
            notesEditText.setText(dishToEdit.getNotes());
            priceEditText.setText(String.valueOf(dishToEdit.getPrice()));
            ratingEditText.setText(String.valueOf(dishToEdit.getRating()));
            waitTimeEditText.setText(String.valueOf(dishToEdit.getWaitTime()));
            Picasso.get().load(dishToEdit.getImagePath()).into(imageButton);
        }

        imageButton.setOnClickListener(v -> openImageChooser());

        saveButton.setText("Actualizar");
        saveButton.setOnClickListener(v -> updateDish());

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void updateDish() {
        String name = nameEditText.getText().toString();
        String notes = notesEditText.getText().toString();
        String price = priceEditText.getText().toString();
        String rating = ratingEditText.getText().toString();
        String waitTime = waitTimeEditText.getText().toString();

        ApiService apiService = ApiClient.getRetrofitInstance(requireContext()).create(ApiService.class);

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            try {
                File imageFile = getFileFromUri(selectedImageUri);
                RequestBody filePart = RequestBody.create(MediaType.parse("image/*"), imageFile);
                imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), filePart);
            } catch (IOException e) {
                Log.e("ChangeDishFragment", "Error al procesar la imagen", e);
            }
        }

        RequestBody namePart = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody pricePart = RequestBody.create(MediaType.parse("text/plain"), price);
        RequestBody notesPart = RequestBody.create(MediaType.parse("text/plain"), notes);
        RequestBody waitTimePart = RequestBody.create(MediaType.parse("text/plain"), waitTime);
        RequestBody ratingPart = RequestBody.create(MediaType.parse("text/plain"), rating);

        Call<Void> call = apiService.updateDish(
                dishToEdit.getId(),
                namePart, pricePart, notesPart, waitTimePart, ratingPart, imagePart
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Plato actualizado correctamente", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar el plato", Toast.LENGTH_SHORT).show();
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
}
