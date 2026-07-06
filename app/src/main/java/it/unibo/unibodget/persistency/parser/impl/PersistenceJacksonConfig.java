package it.unibo.unibodget.persistency.parser.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.wallet.InvestmentAccount;

/**
 * Centralized Jackson configuration for UniBodget persistence.
 * <p>
 * This class exposes a single shared {@link ObjectMapper} instance
 * configured with:
 * <ul>
 *     <li>registered modules (JavaTime, etc.)</li>
 *     <li>safe deserialization settings</li>
 *     <li>pretty-printing</li>
 *     <li>mixins for polymorphic types and ignored fields</li>
 * </ul>
 * <p>
 * It is used by all JSON-based persistence components to ensure
 * consistent serialization/deserialization across the entire app.
 */
public final class PersistenceJacksonConfig {

    /** 
     * Shared, pre-configured ObjectMapper instance.
     * Immutable and thread-safe after construction.
     */
    private static final ObjectMapper MAPPER = build();

    /** 
     * Private constructor: this is a pure utility class.
     */
    private PersistenceJacksonConfig() {
        // Prevent instantiation
    }

    /**
     * Returns the shared ObjectMapper used throughout UniBodget.
     *
     * @return configured ObjectMapper
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * Builds and configures the ObjectMapper instance.
     * <p>
     * Settings applied:
     * <ul>
     *     <li><strong>findAndRegisterModules()</strong> — enables support for JavaTime, JDK8 types, etc.</li>
     *     <li><strong>FAIL_ON_UNKNOWN_PROPERTIES = false</strong> — allows forward-compatible JSON</li>
     *     <li><strong>WRITE_DATES_AS_TIMESTAMPS disabled</strong> </li>
     *     <li><strong>INDENT_OUTPUT enabled</strong> — pretty-print JSON</li>
     * </ul>
     * <p>
     * Mixins:
     * <ul>
     *     <li>{@link CurrencyUnitTypeMixin} — enables polymorphic serialization for CurrencyUnit</li>
     *     <li>{@link IgnorePriceProviderMixin} — hides the priceProvider field in InvestmentAccount</li>
     * </ul>
     */
    private static ObjectMapper build() {
        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules() // JavaTimeModule, JDK8 module, etc.
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // ignore extra fields
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // use readable ISO dates
                .enable(SerializationFeature.INDENT_OUTPUT); // pretty-print

        // Add mixins to customize serialization/deserialization behavior
        mapper.addMixIn(CurrencyUnit.class, CurrencyUnitTypeMixin.class);
        mapper.addMixIn(InvestmentAccount.class, IgnorePriceProviderMixin.class);

        return mapper;
    }

    /**
     * Mixin enabling polymorphic serialization for CurrencyUnit.
     * <p>
     * Jackson will include a "@type" field containing the concrete class name.
     * This is required because CurrencyUnit is an abstract type with multiple implementations.
     */
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "@type"
    )
    private interface CurrencyUnitTypeMixin {
        // No methods: annotation-only mixin
    }

    /**
     * To ignore the "priceProvider" field
     * when serializing/deserializing InvestmentAccount.
     */
    @JsonIgnoreProperties({"priceProvider"})
    private interface IgnorePriceProviderMixin {
        // No methods: annotation-only mixin
    }
}
