package org.mbari.m3.vars.annotation.services.annosaurus.v1;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import org.mbari.m3.vars.annotation.gson.ByteArrayConverter;
import org.mbari.m3.vars.annotation.gson.DurationConverter;
import org.mbari.m3.vars.annotation.gson.TimecodeConverter;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.vcr4j.time.Timecode;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.time.Duration;

/**
 * @author Brian Schlining
 * @since 2017-05-23T15:36:00
 */
public class ServiceGenerator {

    private final Retrofit.Builder retrofitBuilder;

    public ServiceGenerator(String endpoint) {
        String correctedEndpoint = (endpoint.endsWith("/")) ? endpoint : endpoint + "/";

        retrofitBuilder  = new Retrofit.Builder()
                .baseUrl(correctedEndpoint)
                .addConverterFactory(GsonConverterFactory.create(getGson()));
    }

    public <S> S create(Class<S> clazz) {
        return create(clazz, null);
    }


    public <S> S create(Class<S> clazz, Authorization auth) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        if (auth != null) {
            httpClient.addInterceptor(new AuthInterceptor(auth));
        }
        retrofitBuilder.client(httpClient.build());

        return retrofitBuilder.build()
                .create(clazz);
    }

    private static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .registerTypeAdapter(Duration.class, new DurationConverter())
                .registerTypeAdapter(Timecode.class, new TimecodeConverter())
                .registerTypeAdapter(byte[].class, new ByteArrayConverter());

        // Register java.time.Instant
        return Converters.registerInstant(gsonBuilder)
                .create();

    }
}

