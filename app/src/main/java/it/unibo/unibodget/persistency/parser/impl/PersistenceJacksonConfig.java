package it.unibo.unibodget.persistency.parser.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
//import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.CurrencyUnitDeserializer;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public final class PersistenceJacksonConfig {

    private static final ObjectMapper MAPPER = create();

    private PersistenceJacksonConfig() { }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    @SuppressWarnings("unchecked")
    private static ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        SimpleModule module = new SimpleModule();

        // Currency
        module.addDeserializer(CurrencyUnit.class, new CurrencyUnitDeserializer());

        // LocalDate as ISO string
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());

        //module.addSerializer(new OptionalUUIDSerializer());
        module.addSerializer(
            (Class<Optional<UUID>>) (Class<?>) Optional.class,
            new OptionalUUIDSerializer()
        );
        module.addDeserializer(Optional.class,new OptionalUUIDDeserializer());

        mapper.registerModule(module);
        //mapper.registerModule(new Jdk8Module());
        //mapper.registerModule(new JavaTimeModule());

        return mapper;
    }
}
