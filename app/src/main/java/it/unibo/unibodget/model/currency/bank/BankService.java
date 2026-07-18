package it.unibo.unibodget.model.currency.bank;

import java.util.List;

/**
 * Service responsible for loading and managing bank definitions used for
 * currency conversion fee calculations.
 * 
 * <p>
 * A {@code BankService} lazily loads a predefined list of banks from a JSON
 * resource file and keeps them in memory for subsequent access. Additional
 * banks may be added at runtime through {@link #addBankInMemory(Bank)}.
 * 
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

    private final BankList bankList = new BankList();

    /**
     * Adds a bank to the in‑memory list without affecting the JSON source.
     * <p>
     * This method is useful for dynamically adding user‑defined banks during
     * runtime (e.g., custom fee structures).
     *
     * @param bank the bank to add; must not be {@code null}
     */
    public void addBankInMemory(final Bank bank) {
        bankList.add(bank);
    }

    /**
     * Returns the list of banks currently loaded.
     * 
     * <p>
     * If the banks have not yet been loaded, this method triggers the loading
     * process from the JSON resource file.
     * 
     * @return a list of all loaded banks; never {@code null}
     */
    public List<Bank> loadBanks() {
        return bankList.getBanks();
    }
}
