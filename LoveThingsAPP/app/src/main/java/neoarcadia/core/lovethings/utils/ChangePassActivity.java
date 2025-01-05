package neoarcadia.core.lovethings.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

import neoarcadia.core.lovethings.frames.FavActivity;
import neoarcadia.core.lovethings.frames.FeedActivity;
import neoarcadia.core.lovethings.frames.MapsActivity;
import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.frames.SearchActivity;
import neoarcadia.core.lovethings.add.AddDishActivity;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.singup.MessageResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassActivity extends Fragment {


    private EditText oldPass, newPass, cnfPass;
    private Button cbtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_change_pass, container, false);



        oldPass = view.findViewById(R.id.oldpass);
        newPass = view.findViewById(R.id.newpass);
        cnfPass = view.findViewById(R.id.cnfpass);
        cbtn = view.findViewById(R.id.cbtn);

        cbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPass.getText().toString();
                String newPassword = newPass.getText().toString();
                String confirmPassword = cnfPass.getText().toString();

                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(requireContext(), "Las contraseñas nuevas no coinciden.", Toast.LENGTH_SHORT).show();
                    return;
                }

                changePassword(oldPassword, newPassword);
            }
        });
        return view;
    }

    private void changePassword(String oldPassword, String newPassword) {
        ApiService apiService = ApiClient.getRetrofitInstance(requireContext()).create(ApiService.class);

        Map<String, String> changePasswordRequest = new HashMap<>();
        changePasswordRequest.put("oldPassword", oldPassword);
        changePasswordRequest.put("newPassword", newPassword);

        Call<MessageResponse> call = apiService.changePassword(changePasswordRequest);

        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Contraseña cambiada con éxito.", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                } else {
                    Toast.makeText(requireContext(), "Error al cambiar la contraseña.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Error en la conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
