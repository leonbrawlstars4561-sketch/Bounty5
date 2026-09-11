package dev.bountyplugin.bounty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormatter {

    private static final BigDecimal THOUSAND = new BigDecimal("1E3");
    private static final BigDecimal MILLION = new BigDecimal("1E6");
    private static final BigDecimal BILLION = new BigDecimal("1E9");
    private static final BigDecimal TRILLION = new BigDecimal("1E12");
    private static final BigDecimal QUADRILLION = new BigDecimal("1E15");

    private final String currencySymbol;

    public MoneyFormatter(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    /**
     * Kurze, abgekuerzte Darstellung, z.B. $1.50M, $2.75B, $10T, $1Q.
     */
    public String formatAbbreviated(BigDecimal amount) {
        BigDecimal absolute = amount.abs();
        String suffix;
        BigDecimal divisor;

        if (absolute.compareTo(QUADRILLION) >= 0) {
            divisor = QUADRILLION;
            suffix = "Q";
        } else if (absolute.compareTo(TRILLION) >= 0) {
            divisor = TRILLION;
            suffix = "T";
        } else if (absolute.compareTo(BILLION) >= 0) {
            divisor = BILLION;
            suffix = "B";
        } else if (absolute.compareTo(MILLION) >= 0) {
            divisor = MILLION;
            suffix = "M";
        } else if (absolute.compareTo(THOUSAND) >= 0) {
            divisor = THOUSAND;
            suffix = "K";
        } else {
            return currencySymbol + plain(amount);
        }

        BigDecimal scaled = amount.divide(divisor, 2, RoundingMode.HALF_UP);
        return currencySymbol + scaled.stripTrailingZeros().toPlainString() + suffix;
    }

    /**
     * Vollstaendige Darstellung mit Tausendertrennzeichen, z.B. $1.500.000,00
     */
    public String formatFull(BigDecimal amount) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.GERMANY);
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return currencySymbol + format.format(amount.setScale(2, RoundingMode.HALF_UP));
    }

    private String plain(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
