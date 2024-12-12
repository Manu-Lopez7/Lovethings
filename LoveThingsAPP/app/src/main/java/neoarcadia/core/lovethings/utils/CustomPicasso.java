package neoarcadia.core.lovethings.utils;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class CustomPicasso {

    private static Picasso picassoInstance;

    public static Picasso getInstance(Context context, String token) {
        if (picassoInstance == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request requestWithToken = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                    return chain.proceed(requestWithToken);
                })
                .build();

            picassoInstance = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(client))
                .build();
        }
        return picassoInstance;
    }
}
