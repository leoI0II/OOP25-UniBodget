package it.unibo.unibodget.view.utils;

import it.unibo.unibodget.model.currency.Asset;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for formatting monetary and percentage values for display.
 */
public final class AssetFormatter {

    private AssetFormatter() { }

    /**
     * Formats an {@link Asset} as a human-readable string.
     *
     * <p>Output pattern: {@code "<symbol> <sign><amount>"}, where the amount is
     * rounded to the decimal places defined by the currency.</p>
     *
     * @param asset the asset to format
     * @return the formatted string, e.g. {@code "₿ 0.00123456"} or {@code "€ -12.50"}
     */
    public static String ofAsset(final Asset asset) {
        final var decimals = asset.currency().getDisplayDecimals();
        return String.format(
                "%s %s",
                asset.currency().getSymbol(),
                asset.amount().setScale(decimals, RoundingMode.HALF_UP)
        );
    }

    /**
     * Formats an {@link Asset} as a human-readable string, appending the currency short name in parentheses.
     *
     * <p>Output pattern: {@code "<symbol> <amount> (<shortName>)"}, where the amount is
     * rounded to the decimal places defined by the currency.</p>
     *
     * @param asset the asset to format
     * @return the formatted string, e.g. {@code "₿ 0.00123456 (BTC)"} or {@code "€ -12.50 (EUR)"}
     */
    public static String ofAssetWithName(final Asset asset) {
        final var decimals = asset.currency().getDisplayDecimals();
        return String.format(
                "%s %s (%s)",
                asset.currency().getSymbol(),
                asset.amount().setScale(decimals, RoundingMode.HALF_UP),
                asset.currency().getShortName()
        );
    }

    /**
     * Formats a {@link BigDecimal} as a percentage string.
     *
     * <p>Output pattern: {@code "<sign><value>%"}, always with two decimal places.</p>
     *
     * @param value the percentage value to format
     * @return the formatted string, e.g. {@code "12.50%"} or {@code "-3.00%"}
     */
    public static String ofPercentage(final BigDecimal value) {
        return String.format("%.2f%%", value);
    }

}
