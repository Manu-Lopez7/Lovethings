package neoarcadia.core.lovethings.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Redimensiona y comprime una imagen desde un URI.
     *
     * @param context   Contexto de la aplicación.
     * @param imageUri  URI de la imagen.
     * @param maxWidth  Ancho máximo deseado.
     * @param quality   Calidad de compresión (0-100).
     * @return Un archivo comprimido con la imagen.
     * @throws IOException Si ocurre un error al procesar la imagen.
     */
    public static File resizeAndCompressImage(Context context, Uri imageUri, int maxWidth, int quality) throws IOException {
        // Carga el bitmap original desde el URI
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

        if (originalBitmap == null) {
            Log.e(TAG, "El bitmap original es nulo");
            throw new IOException("Error al cargar la imagen desde el URI");
        }

        // Calcula el nuevo tamaño
        int targetWidth = maxWidth;
        int targetHeight = (originalBitmap.getHeight() * targetWidth) / originalBitmap.getWidth();

        // Redimensiona el bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, false);

        // Comprime la imagen
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

        // Escribe los bytes comprimidos en un archivo temporal
        File tempFile = new File(context.getCacheDir(), "compressed_image_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            fileOutputStream.write(outputStream.toByteArray());
        }

        Log.d(TAG, "Imagen comprimida y guardada en: " + tempFile.getAbsolutePath());

        return tempFile;
    }
}