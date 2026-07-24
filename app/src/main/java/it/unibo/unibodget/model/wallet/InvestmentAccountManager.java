package it.unibo.unibodget.model.wallet;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import it.unibo.unibodget.model.converter.provider.PriceProvider;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.CurrencyUnitDeserializer;
import it.unibo.unibodget.persistency.ModelFileManager;
import it.unibo.unibodget.persistency.parser.impl.PersistenceJacksonConfig;

/**
 * Persistence manager for {@link InvestmentAccount} objects.
 * Handles JSON loading/saving using {@link ModelFileManager} and Jackson.
 * Restores accounts from disk and reattaches the required {@link PriceProvider}.
 * Acts as the persistence gateway for all investment wallets.
 */
public final class InvestmentAccountManager {

    private static final Path PATH = Path.of("data/json/wallet/InvestmentAccounts.json");
    private static final String RESOURCE = "/json/wallet/InvestmentAccounts.json";
    private static final String KEY = "investmentAccounts";

    private final PriceProvider provider;
    private final ModelFileManager<InvestmentAccount> manager;

    /**
     * Creates a new manager for investment accounts.
     * The provided {@link PriceProvider} will be attached to all loaded accounts.
     *
     * @param provider the price provider used for market value computations
     */
    public InvestmentAccountManager(final PriceProvider provider) {
        this.provider = provider;
        this.manager =
                new ModelFileManager<>(
                        PATH,
                        RESOURCE,
                        InvestmentAccount.class,
                        null,
                        KEY
                );
    }

    /**
     * Loads all {@link InvestmentAccount} entries from the JSON file.
     * Configures Jackson with a custom {@link CurrencyUnitDeserializer}
     * to correctly restore currency fields. After deserialization, each
     * account is rehydrated with the configured {@link PriceProvider}.
     * Returns an empty list on failure.
     *
     * @return a list of fully reconstructed {@link InvestmentAccount} objects
     */
    public List<InvestmentAccount> loadAll() {
        try {
            final ObjectMapper mapper = PersistenceJacksonConfig.mapper().copy();

            final SimpleModule module = new SimpleModule();
            module.addDeserializer(CurrencyUnit.class, new CurrencyUnitDeserializer());
            mapper.registerModule(module);

            manager.open();
            final JsonNode root = manager.loadJson();
            final JsonNode arr = root.get(KEY);

            final List<InvestmentAccount> LOADED = mapper.convertValue(
                arr,
                mapper.getTypeFactory().constructCollectionType(List.class, InvestmentAccount.class)
            );

            return LOADED.stream()
                    .map(a -> a.withProvider(provider))
                    .toList();

        } catch (final Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Saves all provided {@link InvestmentAccount} objects to the JSON file.
     * Delegates serialization to {@link ModelFileManager}, replacing the
     * existing list under the configured JSON key.
     *
     * @param accounts the list of accounts to persist
     * @throws RuntimeException if saving fails
     */
    public void saveAll(final List<InvestmentAccount> accounts) {
        try {
            manager.open();
            manager.saveList(
                    KEY,
                    accounts
            );
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
