package it.unibo.unibodget.model.currency.bank;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class responsible for computing the total commission applied by a bank
 * during a currency conversion. The commission consists of a fixed fee plus a
 * percentage fee applied to the converted amount.
 * 
 * <p>
 * This class performs no validation on the input values; callers are expected
 * to provide non-negative amounts and valid {@link Bank} instances.
 */
public final class BankCalculator {

        /**
         * Calculates the total commission charged by a bank for converting a given amount.
         */
        private BankCalculator() {

        }

        /**
         * Computes the total commission charged by the given bank for converting
         * the specified amount.
         * 
         * <p>
         * Formula:
         * 
         * <pre>
         * totalCommission = (amount * (percentageFee / 100)) + fixedFee
         * </pre>
         * 
         * <p>
         * The result is rounded to two decimal places using
         * {@link RoundingMode#HALF_UP}, which is standard for financial operations.
         *
         * @param amount the amount to be converted; must not be {@code null}
         * @param bank   the bank providing the fee structure; must not be {@code null}
         * @return       the total commission as a {@link BigDecimal}, rounded to two decimals
         */
        static BigDecimal calculateCommission(final BigDecimal amount, final Bank bank) {
                final BigDecimal percentageValue =
                        amount.multiply(BigDecimal.valueOf(bank.getPercentageFee() / 100.0));

                final BigDecimal fixedValue =
                        BigDecimal.valueOf(bank.getFixedFee());

                return percentageValue.add(fixedValue)
                        .setScale(2, RoundingMode.HALF_UP);
        }

}
