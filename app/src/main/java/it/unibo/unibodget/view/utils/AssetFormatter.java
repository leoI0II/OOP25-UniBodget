package it.unibo.unibodget.view.utils;

import it.unibo.unibodget.model.currency.Asset;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AssetFormatter {

    public static String ofAsset(final Asset asset) {
        var decimals = asset.currency().getDisplayDecimals();
        var sign = asset.isNegative() ? "-" : "";
        return String.format(
                "%s %s%s",
                asset.currency().getSymbol(),
                sign,
                asset.amount().abs().setScale(decimals, RoundingMode.HALF_UP)
        );
    }

    public static String ofPercentage(final BigDecimal value) {
        var sign = value.signum() == -1 ? "-" : "";
        return String.format(
                "%s%.2f%%",
                sign,
                value.abs().setScale(2, RoundingMode.HALF_UP)
        );
    }

}
