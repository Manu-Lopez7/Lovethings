package neoarcadia.core.lovethings.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.List;

import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GrantedRoles extends Fragment {

    private AutoCompleteTextView userSearch;
    private Spinner rolesSpinner;
    private Button assignButton;
    private ApiService apiService;
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_granted_roles, container, false);

        userSearch = view.findViewById(R.id.users_search);
        rolesSpinner = view.findViewById(R.id.roles_spinner);
        assignButton = view.findViewById(R.id.cbtn);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);

        apiService = ApiClient.getRetrofitInstance(requireContext()).create(ApiService.class);
        cargarUsuarios();
        cargarRoles();

        assignButton.setOnClickListener(v -> {
            String username = userSearch.getText().toString().trim();
            String role = rolesSpinner.getSelectedItem().toString().trim();

            if (!username.isEmpty() && !role.isEmpty()) {
                asignarRol(username, role);
            } else {
                Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void cargarUsuarios() {
        apiService.getAllUsernames(token).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, response.body());
                    userSearch.setAdapter(adapter);
                    userSearch.setOnClickListener(v -> userSearch.showDropDown());
                } else {
                    Toast.makeText(getContext(), "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarRoles() {
        List<String> roles = Arrays.asList("user", "mod", "admin");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rolesSpinner.setAdapter(adapter);
    }

    private void asignarRol(String username, String roleName) {
        apiService.assignRole(token, username, roleName).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Rol asignado correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al asignar rol", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
