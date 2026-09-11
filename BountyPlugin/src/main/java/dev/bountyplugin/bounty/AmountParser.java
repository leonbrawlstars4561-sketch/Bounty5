package dev.bountyplugin.bounty;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wandelt Nutzereingaben wie "1000", "1.5k", "2B", "10t" oder "1q" in einen
 * exakten {@link BigDecimal}-Betrag um. Es wird ausschliesslich mit
 * BigDecimal gerechnet, damit bei sehr grossen Bountys keine
 * Rundungsfehler durch double-Arithmetik entstehen.
 */
public final class AmountParser {

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("^(\\d+(?:\\.\\d+)?)([kKmMbBtTqQ]?)$");

    private AmountParser() {
    }

    public static BigDecimal parse(String input) throws AmountParseException {
        if (input == null || input.isBlank()) {
            throw new AmountParseException(input);
        }

        String trimmed = input.trim();
        Matcher matcher = AMOUNT_PATTERN.matcher(trimmed);

        if (!matcher.matches()) {
            throw new AmountParseException(input);
        }

        BigDecimal base;
        try {
            base = new BigDecimal(matcher.group(1));
        } catch (NumberFormatException exception) {
            throw new AmountParseException(input);
        }

        String suffix = matcher.group(2).toLowerCase(Locale.ROOT);
        BigDecimal multiplier = multiplierFor(suffix);

        if (multiplier == null) {
            throw new AmountParseException(input);
        }

        return base.multiply(multiplier);
    }

    private static BigDecimal multiplierFor(String suffix) {
        return switch (suffix) {
            case "" -> BigDecimal.ONE;
            case "k" -> new BigDecimal("1E3");
            case "m" -> new BigDecimal("1E6");
            case "b" -> new BigDecimal("1E9");
            case "t" -> new BigDecimal("1E12");
            case "q" -> new BigDecimal("1E15");
            default -> null;
        };
    }
}
