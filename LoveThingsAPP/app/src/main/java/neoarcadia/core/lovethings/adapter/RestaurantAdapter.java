package neoarcadia.core.lovethings.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

import neoarcadia.core.lovethings.R;
import neoarcadia.core.lovethings.models.Dish;
import neoarcadia.core.lovethings.models.Restaurant;
import neoarcadia.core.lovethings.utils.CustomPicasso;


public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private final Context context;
    private final List<Restaurant> restaurantList;

    public RestaurantAdapter(Context context, List<Restaurant> restaurantList) {
        this.context = context;
        this.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_cardview, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        Log.d("RestaurantAdapter", "Cargando restaurante: " + restaurant.getName());

        SharedPreferences sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        long currentUserId = sharedPreferences.getLong("user_id", -1);
        String token = sharedPreferences.getString("jwt_token", null);


        Log.d("RestaurantAdapter", "Carando imagen: " + restaurant.getImagePath());
        String imageUrl = restaurant.getImagePath()
                .replace("C:\\uploads", "http://93.114.154.61:20202/uploads")
                .replace("\\", "/");
        Log.d("RestaurantAdapter", "URL de la imagen: " + imageUrl);


        if (token != null) {
            CustomPicasso.getInstance(context, token)
                    .load(imageUrl)
                    .into(holder.imageButton);
            Log.d("RestaurantAdapter", "Imagen cargada");
        } else {
            holder.imageButton.setImageResource(R.drawable.ic_launcher_foreground);
            Log.d("RestaurantAdapter", "Imagen no cargada");
        }
        holder.restaurantName.setText(restaurant.getName());

        // Build dishes string
        StringBuilder dishesBuilder = new StringBuilder();
        double totalPrice = 0;
        int totalRating = 0;
        int totalWaitTime = 0;
        int count = 0;

        for (Dish dish : restaurant.getDishes()) {
            if (dish.getUser() != null && dish.getUser().getId() == currentUserId) {
                dishesBuilder.append(dish.getName()).append("\n");
                totalPrice += dish.getPrice();
                totalRating += dish.getRating();
                totalWaitTime += dish.getWaitTime();
                count++;
                Log.d("RestaurantAdapter", "Plato: " + dish.getName() + ", Usuario: " + dish.getUser().getUsername());

            }
        }

        if (count > 0) {
            holder.dishes.setText(dishesBuilder.toString().trim());
            holder.averagePrice.setText(String.format("%.2f", totalPrice / count));
            holder.averageRating.setText(String.valueOf(totalRating / count));
            holder.averageWaitTime.setText(String.valueOf(totalWaitTime / count));
        } else {
            holder.dishes.setText("Sin platos disponibles");
            holder.averagePrice.setText("0");
            holder.averageRating.setText("0");
            holder.averageWaitTime.setText("0");
        }
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        ImageButton imageButton;
        TextView restaurantName;
        TextView dishes;
        TextView averagePrice;
        TextView averageRating;
        TextView averageWaitTime;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.restimg);
            restaurantName = itemView.findViewById(R.id.titlerest);
            dishes = itemView.findViewById(R.id.dishes);
            averagePrice = itemView.findViewById(R.id.price);
            averageRating = itemView.findViewById(R.id.ratingmed);
            averageWaitTime = itemView.findViewById(R.id.timewait);
        }
    }
}
