package it.unibo.unibodget.model.wallet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.CurrencyUnitDeserializer;
import it.unibo.unibodget.persistency.ModelFileManager;
import it.unibo.unibodget.persistency.parser.impl.PersistenceJacksonConfig;

/**
 * Handles loading and saving of {@link CashAccount} objects from JSON storage.
 * Uses {@link ModelFileManager} for file access and Jackson for deserialization.
 * Supports custom currency deserialization and returns fully reconstructed
 * account lists. Acts as the persistence gateway for cash wallets.
 */
public final class CashAccountManager {

    private static final Path PATH = Path.of("data/json/wallet/CashAccounts.json");
    private static final String RESOURCE = "/json/wallet/CashAccounts.json";
    private static final String KEY = "cashAccounts";

    private final ModelFileManager<CashAccount> manager =
            new ModelFileManager<>(
                    PATH,
                    RESOURCE,
                    CashAccount.class,
                    null,
                    KEY
            );

    /**
     * Loads all {@link CashAccount} entries from the JSON file.
     * Configures Jackson with a custom {@link CurrencyUnitDeserializer}
     * to correctly restore currency fields. Returns an empty list on failure.
     *
     * @return a list of deserialized {@link CashAccount} objects
     */
    public List<CashAccount> loadAll() {
        try {
            final ObjectMapper mapper = PersistenceJacksonConfig.mapper().copy();

            final SimpleModule module = new SimpleModule();
            module.addDeserializer(CurrencyUnit.class, new CurrencyUnitDeserializer());
            mapper.registerModule(module);

            manager.open();
            final JsonNode root = manager.loadJson();
            final JsonNode arr = root.get(KEY);

            return mapper.convertValue(
                arr,
                mapper.getTypeFactory().constructCollectionType(List.class, CashAccount.class)
            );

        } catch (final IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Saves all provided {@link CashAccount} objects to the JSON file.
     * Delegates serialization to {@link ModelFileManager}, replacing the
     * existing list under the configured JSON key.
     *
     * @param accounts the list of accounts to persist
     * @throws RuntimeException if saving fails
     */
    public void saveAll(final List<CashAccount> accounts) {
        try {
            manager.open();
            manager.saveList(KEY, accounts);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
