package com.cleanengine.coin.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.cleanengine.coin.configuration.TimeZoneConfig.SEOUL_ZONE_ID;

@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Double.class, doubleSerializer());
            module.addSerializer(LocalDateTime.class, localDateTimeSerializer());
            builder.modules(module);
        };
    }

    private JsonSerializer<LocalDateTime> localDateTimeSerializer() {
        return new SeoulLocalDateTimeSerializer();
    }

    private JsonSerializer<Double> doubleSerializer() {
        return new JsonSerializer<Double>() {
            @Override
            public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                if (value != null) {
                    gen.writeString(new BigDecimal(value.toString()).toPlainString());
                }
            }
        };
    }

    public static class SeoulLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            if (value != null) {
                ZonedDateTime zonedDateTime = value.atZone(SEOUL_ZONE_ID);
                gen.writeString(zonedDateTime.format(dateTimeFormatter));
            }
        }
    }
}
