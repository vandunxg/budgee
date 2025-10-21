package com.budgee.config;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.*;

@Configuration
public class GsonConfig {

    @Bean
    public Gson gson() {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(
                Instant.class,
                (JsonSerializer<Instant>)
                        (src, typeOfSrc, context) ->
                                new JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(src)));

        builder.registerTypeAdapter(
                Instant.class,
                (JsonDeserializer<Instant>)
                        (json, typeOfT, context) -> Instant.parse(json.getAsString()));

        builder.setPrettyPrinting();

        return builder.create();
    }
}
