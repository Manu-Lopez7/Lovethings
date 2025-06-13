package neoarcadia.core.lovethings.adapter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.frames.ChangeDishFragment;
import neoarcadia.core.lovethings.models.Dish;
import neoarcadia.core.lovethings.models.Restaurant;
import neoarcadia.core.lovethings.utils.CustomPicasso;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.ViewHolder> {
    private Context context;

    private List<Dish> dishList;

    public DishAdapter(List<Dish> dishList, Context context) {
        this.dishList = dishList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dish dish = dishList.get(holder.getAdapterPosition()); // `holder.getAdapterPosition()`

        if (dish.getRestaurantId() != null) {
            fetchRestaurantName(dish.getRestaurantId(), holder.namerest);
        } else {
            Log.e("DishAdapter", "restaurantId es nulo o vacío para el plato: " + dish.getName());
        }
        holder.title.setText(dish.getName());
        holder.description.setText(dish.getNotes());
        holder.price.setText(String.valueOf(dish.getPrice()));
        holder.rating.setText(String.valueOf(dish.getRating()));
        holder.waitTime.setText(String.valueOf(dish.getWaitTime()));
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);
        long currentUserId = sharedPreferences.getLong("user_id", -1);
        if (dish.getUser().getId() == currentUserId) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
        if (dish.getImagePath() != null && !dish.getImagePath().isEmpty()) {
            Log.d("DishAdapter", "Imagen del plato: " + dish.getImagePath());
            String imageUrl = dish.getImagePath()
                    .replace("C:\\uploads", "http://172.22.239.37:20202/uploads")
                    .replace("\\", "/");
            if (token != null) {
                CustomPicasso.getInstance(context, token)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_blank)
                        .error(R.drawable.no_image)
                        .into(holder.dishimg);

                Log.d("DishAdapter", "Imagen cargada");
            } else {
                holder.dishimg.setImageResource(R.drawable.image_blank);
                Log.d("DishAdapter", "Imagen no cargada");
            }
        } else {
            Log.d("DishAdapter", "La imagen es nula o vacía, se carga la imagen predeterminada");
            holder.dishimg.setImageResource(R.drawable.image_blank);
        }

        holder.favButton.setImageResource(dish.isFavorite() ? R.drawable.like : R.drawable.dislike);
        holder.favButton.setOnClickListener(v -> {
            boolean newFavoriteState = !dish.isFavorite();
            Log.d("DishAdapter", "Intentando actualizar favorito: Plato=" + dish.getName() +
                    ", ID=" + dish.getId() + ", Nuevo estado=" + newFavoriteState);
            dish.setFavorite(newFavoriteState);
            holder.favButton.setImageResource(newFavoriteState ? R.drawable.like : R.drawable.dislike);

            // Actualización en la API
            ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
            Call<Void> call = apiService.updateFavoriteStatus(dish.getId(), newFavoriteState);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("DishAdapter", "Favorito actualizado correctamente en la API.");
                    } else {
                        Log.e("DishAdapter", "Error al actualizar favorito: Código=" + response.code());
                        dish.setFavorite(!newFavoriteState); // Revertir el cambio en caso de error
                        holder.favButton.setImageResource(dish.isFavorite() ? R.drawable.like : R.drawable.dislike);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Error de conexión
                    dish.setFavorite(!newFavoriteState); // Revertir el cambio en caso de fallo
                    holder.favButton.setImageResource(dish.isFavorite() ? R.drawable.like : R.drawable.dislike);
                }
            });
        });
        holder.deleteButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return; // Salir si la posición no es válida
            }

            Dish currentDish = dishList.get(currentPosition);
            long dishId = currentDish.getId();
            Log.d("DishAdapter", "Intentando eliminar plato: ID=" + dishId);

            // Confirmar eliminación (opcional)
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar Plato")
                    .setMessage("¿Estás seguro de que deseas eliminar este plato?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
                        Call<Void> call = apiService.deleteDish(dishId);
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d("DishAdapter", "Plato eliminado correctamente");
                                    // Eliminar el plato de la lista y actualizar el adaptador
                                    dishList.remove(position);
                                    notifyItemRemoved(position);
                                } else {
                                    Log.e("DishAdapter", "Error al eliminar plato: Código=" + response.code());
                                    Toast.makeText(context, "Error al eliminar plato", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("DishAdapter", "Error al conectar con la API para eliminar plato", t);
                                Toast.makeText(context, "Error al eliminar plato", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
        holder.editButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return; // Salir si la posición no es válida
            }
            Dish currentDish = dishList.get(currentPosition);
            Log.d("DishAdapter", "Intentando editar plato: ID=" + currentDish.getId());
            Bundle args = new Bundle();
            args.putSerializable("dish", currentDish);
            ChangeDishFragment fragment = new ChangeDishFragment();
            fragment.setArguments(args);
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Log.e("DishAdapter", "Contexto no es una instancia de AppCompatActivity");
            }
        });
    }


    @Override
    public int getItemCount() {
        int size = dishList.size();
        Log.d("DishAdapter", "Número de platos: " + size);
        return size;
    }

    public void updateDishes(List<Dish> newDishes) {
        Log.d("DishAdapter", "Actualizando platos: " + newDishes.size());
        this.dishList = newDishes;
        Log.d("DishAdapter", "Número de platos después de actualizar: " + this.dishList.size());
        notifyDataSetChanged();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView namerest, title, description, price, rating, waitTime;
        ImageView dishimg;
        ImageButton favButton, deleteButton, editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            namerest = itemView.findViewById(R.id.restaurantdish);
            title = itemView.findViewById(R.id.nameds);
            description = itemView.findViewById(R.id.noteds);
            price = itemView.findViewById(R.id.price);
            rating = itemView.findViewById(R.id.ratingmed);
            waitTime = itemView.findViewById(R.id.timewait);
            dishimg = itemView.findViewById(R.id.dishimg);
            favButton = itemView.findViewById(R.id.favds);
            deleteButton = itemView.findViewById(R.id.delds);
            editButton = itemView.findViewById(R.id.editds);
        }

    }

    private void fetchRestaurantName(Long restaurantId, TextView restaurantNameTextView) {
        ApiService apiService = ApiClient.getRetrofitInstance(context).create(ApiService.class);
        Call<Restaurant> call = apiService.getRestaurantById(restaurantId);

        call.enqueue(new Callback<Restaurant>() {
            @Override
            public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String restaurantName = response.body().getName();
                    Log.d("DishAdapter", "Nombre del restaurante obtenido: " + restaurantName);
                    restaurantNameTextView.setText(restaurantName);
                } else {
                    Log.e("DishAdapter", "Error al obtener el restaurante: " + response.code());
                    restaurantNameTextView.setText("Restaurante desconocido");
                }
            }

            @Override
            public void onFailure(Call<Restaurant> call, Throwable t) {
                Log.e("DishAdapter", "Error en la API: " + t.getMessage());
                restaurantNameTextView.setText("Error al cargar");
            }
        });
    }

}
