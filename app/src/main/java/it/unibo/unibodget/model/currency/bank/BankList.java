package it.unibo.unibodget.model.currency.bank;

import it.unibo.unibodget.persistency.ModelFileManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BankList {

    private static final double FIXED_FEE_1 = 1.0;
    private static final double FIXED_FEE_2 = 2.0;
    private static final double FIXED_FEE_3 = 3.0;
    private static final double PERC_FEE_0_5 = 0.5;
    private static final double PERC_FEE_1 = 1.0;
    private static final double PERC_FEE_1_5 = 1.5;

    private static final Path PATH = Path.of("data/json/currency/bank/Banks.json");
    private static final String RESOURCE = "/json/currency/bank/Banks.json";

    private static boolean initialized = false;
    private static final List<Bank> LOADED = new ArrayList<>();

    /**
     * Initializes the bank list by loading from the JSON 
     * resource file if it has not already been initialized.
     * 
     * <p>
     * If the JSON file is empty or cannot be loaded, a set of mock banks will
     * be generated and used instead. 
     * This ensures that the application always has a valid set of banks to work with.
     */
    public BankList() {
        if (!initialized) {
            init();
        }
    }

    /**
     * Returns an unmodifiable view of the list of banks currently loaded in memory.
     * 
     * <p>
     * This method ensures that the internal list cannot be modified externally,
     * preserving the integrity of the bank data.
     * 
     * @return an unmodifiable list of loaded banks; never {@code null}
     */
    public List<Bank> getBanks() {
        return Collections.unmodifiableList(LOADED);
    }

    /**
     * Adds a bank to the internal list if it is not already present.
     * 
     * <p>
     * If the bank is added, the updated list is saved to the JSON resource file.
     *
     * @param bank the bank to add; must not be {@code null}
     * @return {@code true} if the bank was added, {@code false} otherwise
     */
    public boolean add(final Bank bank) {
        final boolean added = !LOADED.contains(bank);
        if (added) {
            LOADED.add(bank);
            save();
        }
        return added;
    }

    /**
     * Saves the current list of banks to the JSON resource file.
     * 
     * <p>
     * This method is called whenever a new bank is added to ensure that
     * the persistent storage reflects the current state of the in-memory list.
     */
    private static void save() {
        try {
            final ModelFileManager<Bank> mgr =
                    new ModelFileManager<>(PATH, RESOURCE, Bank.class);
            mgr.open();
            mgr.saveList("banks", LOADED);
            mgr.close();
        } catch (final IOException e) {
            System.out.println("BankList save failed");
        }
    }

    /**
     * Initializes the bank list by loading from the JSON 
     * resource file if it has not already been initialized.
     * 
     * <p>
     * If the JSON file is empty or cannot be loaded, a set of mock banks will
     * be generated and used instead. 
     * This ensures that the application always has a valid set of banks to work with.
     */
    public static void init() {
        try {
            final ModelFileManager<Bank> mgr =
                    new ModelFileManager<>(PATH, RESOURCE, Bank.class);
            mgr.open();

            final var list = mgr.loadList("banks");
            mgr.close();

            if (list == null || list.isEmpty()) {
                System.out.println("Banks JSON empty → using mock banks");
                LOADED.clear();
                LOADED.addAll(generateMockBanks());
            } else {
                LOADED.clear();
                LOADED.addAll(list);
                System.out.println("Banks loaded → " + LOADED.size());
            }
            initialized = true;
        } catch (final IOException e) {
            System.out.println("Banks load failed → using mock banks");
            LOADED.clear();
            LOADED.addAll(generateMockBanks());
            initialized = true;
        }
    }

    /**
     * Generates a list of mock banks for use 
     * when the JSON file is unavailable or empty.
     *
     * @return a list of mock banks
     */
    private static List<Bank> generateMockBanks() {
        return List.of(
                new Bank("Intesa San Paolo", FIXED_FEE_1, PERC_FEE_0_5),
                new Bank("BPER", FIXED_FEE_2, PERC_FEE_1),
                new Bank("Banca di Romagna", FIXED_FEE_3, PERC_FEE_1_5)
        );
    }
}
