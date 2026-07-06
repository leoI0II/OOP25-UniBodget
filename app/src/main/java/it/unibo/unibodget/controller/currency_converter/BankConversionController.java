package it.unibo.unibodget.controller.currency_converter;

import it.unibo.unibodget.model.currency.bank.Bank;
import it.unibo.unibodget.model.currency.bank.BankService;
import java.util.List;

/**
 * Controller responsible for managing bank-related currency conversion operations.
 * <p>
 * This class acts as an intermediary between the view layer and the {@link BankService},
 * providing access to available banks, adding new banks, and validating commission values
 * before insertion.
 */
public class BankConversionController {

    private final BankService bankService = new BankService();

    /**
     * Retrieves the list of all banks currently available in the system.
     *
     * @return a {@link List} containing all loaded {@link Bank} instances.
     *         The list may be empty if no banks have been registered.
     */
    public List<Bank> getAvailableBanks() {
        return bankService.loadBanks();
    }

    /**
     * Adds a new bank to the in-memory list of available banks.
     * <p>
     * This method does not perform validation on the bank's commission values.
     * It is recommended to call {@link #canAddBank(double, double)} before invoking this method.
     *
     * @param bank the {@link Bank} instance to be added; must not be {@code null}.
     */
    public void addBank(Bank bank) {
        bankService.addBankInMemory(bank);
    }

    /**
     * Validates whether a bank with the given commission values can be added to the system.
     * <p>
     * A bank is considered valid if both its fixed commission and percentage commission
     * do not exceed 50.0.
     *
     * @param fixed   the fixed commission applied by the bank (in the same currency unit).
     * @param percent the percentage commission applied by the bank (0–100 range expected).
     * @return {@code true} if both commission values are less than or equal to 50.0;
     *         {@code false} otherwise.
     */
    public boolean canAddBank(double fixed, double percent) {
        return fixed <= 50.0 && percent <= 50.0;
    }
}
