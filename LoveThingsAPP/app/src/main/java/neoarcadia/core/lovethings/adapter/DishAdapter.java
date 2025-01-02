package neoarcadia.core.lovethings.adapter;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.api.ApiClient;
import neoarcadia.core.lovethings.api.ApiService;
import neoarcadia.core.lovethings.models.Dish;
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
        Dish dish = dishList.get(position);
        Log.d("DishAdapter", "Enlazando plato: " + dish.getName());

        holder.title.setText(dish.getName());
        holder.description.setText(dish.getNotes());
        holder.price.setText(String.valueOf(dish.getPrice()));
        holder.rating.setText(String.valueOf(dish.getRating()));
        holder.waitTime.setText(String.valueOf(dish.getWaitTime()));
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", null);
        if (dish.getImagePath() != null && !dish.getImagePath().isEmpty()) {
            Log.d("DishAdapter", "Imagen del plato: " + dish.getImagePath());
            String imageUrl = dish.getImagePath()
                    .replace("C:\\uploads", "http://192.168.18.10:8080/uploads")
                    .replace("\\", "/");
            if (token != null) {
                CustomPicasso.getInstance(context, token)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_blank)
                        .error(R.drawable.ic_dashboard_black_24dp)
                        .into(holder.dishimg);

                Log.d("DishAdapter", "Imagen cargada");
            } else {
                holder.dishimg.setImageResource(R.drawable.image_blank);
                Log.d("DishAdapter", "Imagen no cargada");
            }
        } else {
            Log.d("DishAdapter", "La imagen es nula o vacía, se carga la imagen predeterminada");
            holder.dishimg.setImageResource(R.drawable.home);
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
        TextView title, description, price, rating, waitTime;
        ImageView dishimg;
        ImageButton favButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.nameds);
            description = itemView.findViewById(R.id.noteds);
            price = itemView.findViewById(R.id.price);
            rating = itemView.findViewById(R.id.ratingmed);
            waitTime = itemView.findViewById(R.id.timewait);
            dishimg = itemView.findViewById(R.id.dishimg);
            favButton = itemView.findViewById(R.id.favds);
        }
    }

}
