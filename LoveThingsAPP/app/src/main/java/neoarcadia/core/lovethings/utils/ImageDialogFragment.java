package neoarcadia.core.lovethings.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.squareup.picasso.Picasso;
import neoarcadia.core.lovethings.R;

public class ImageDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setContentView(R.layout.dialog_image);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);

        String token = sharedPreferences.getString("jwt_token", null);


        ImageView imageView = dialog.findViewById(R.id.fullscreen_image);
        String imageUrl = getArguments() != null ? getArguments().getString("image_url") : "";
        Log.d("ImageDialogFragment", "Cargando imagen: " + imageUrl);
        if (token != null) {
            CustomPicasso.getInstance(requireActivity(), token)
                    .load(imageUrl)
                    .into(imageView);
            Log.d("RestaurantAdapter", "Imagen cargada");
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
            Log.d("RestaurantAdapter", "Imagen no cargada");
        }

        return dialog;
    }
}
