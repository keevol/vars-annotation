package org.mbari.m3.vars.annotation.services.varsuserserver.v1;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mbari.m3.vars.annotation.gson.ByteArrayConverter;
import org.mbari.m3.vars.annotation.gson.DurationConverter;
import org.mbari.m3.vars.annotation.gson.TimecodeConverter;
import org.mbari.m3.vars.annotation.services.RetrofitServiceFactory;
import org.mbari.vcr4j.time.Timecode;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;

/**
 * @author Brian Schlining
 * @since 2017-06-09T08:29:00
 */
public class KBMiscServiceFactory extends RetrofitServiceFactory {

    @Inject
    public KBMiscServiceFactory(@Named("MISC_ENDPOINT") String endpoint) {
        super(endpoint);
    }

    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .registerTypeAdapter(Duration.class, new DurationConverter())
                .registerTypeAdapter(Timecode.class, new TimecodeConverter())
                .registerTypeAdapter(byte[].class, new ByteArrayConverter());

        // Register java.time.Instant
        return Converters.registerInstant(gsonBuilder)
                .create();

    }
}
