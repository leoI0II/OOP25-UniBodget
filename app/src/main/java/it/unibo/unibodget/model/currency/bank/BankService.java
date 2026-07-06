package it.unibo.unibodget.model.currency.bank;

import it.unibo.unibodget.persistency.parser.impl.JsonDataParser;
import it.unibo.unibodget.persistency.parser.api.DataParserException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for loading and managing bank definitions used for
 * currency conversion fee calculations.
 * <p>
 * A {@code BankService} lazily loads a predefined list of banks from a JSON
 * resource file and keeps them in memory for subsequent access. Additional
 * banks may be added at runtime through {@link #addBankInMemory(Bank)}.
 * <p>
 * Notes:
 * <ul>
 *     <li>The JSON file is expected to contain an array of bank objects.</li>
 *     <li>Loading occurs only once; subsequent calls return a copy of the
 *         already loaded list.</li>
 *     <li>Debug print statements are included to help diagnose loading issues.</li>
 * </ul>
 */
public class BankService {

    private final List<Bank> loadedBanks = new ArrayList<>();
    private boolean initialized = false;

    /**
     * Loads the list of banks from the JSON resource file, if not already loaded.
     * <p>
     * Behavior:
     * <ul>
     *     <li>If the service has already been initialized, a copy of the cached
     *         list is returned.</li>
     *     <li>Otherwise, the method reads {@code BasicBanks.json} from the classpath,
     *         parses it using {@link JsonDataParser}, and stores the resulting banks
     *         in memory.</li>
     *     <li>Any I/O or parsing errors are logged, and an empty list is returned.</li>
     * </ul>
     *
     * @return a new list containing all loaded banks; never {@code null}
     */
    public List<Bank> loadBanks() {
        if (initialized) {
            return new ArrayList<>(loadedBanks);
        }

        JsonDataParser<Bank> parser = new JsonDataParser<>(Bank.class);

        try (InputStream is = Bank.class.getResourceAsStream("/json/currency/bank/BasicBanks.json")) {
            if (is != null) {
                String json = new String(is.readAllBytes());
                System.out.println("Loading banks from JSON: " + json);

                List<Bank> banks = parser.parseList(json);

                if (banks != null) {
                    loadedBanks.addAll(banks);
                    loadedBanks.forEach(b ->
                        System.out.println("Loaded bank: " + b.getName())
                    );
                }
            }
        } catch (IOException | DataParserException e) {
            System.err.println("Error loading banks: " + e.getMessage());
        }

        initialized = true;
        return new ArrayList<>(loadedBanks);
    }

    /**
     * Adds a bank to the in‑memory list without affecting the JSON source.
     * <p>
     * This method is useful for dynamically adding user‑defined banks during
     * runtime (e.g., custom fee structures).
     *
     * @param bank the bank to add; must not be {@code null}
     */
    public void addBankInMemory(Bank bank) {
        loadedBanks.add(bank);
    }
}
