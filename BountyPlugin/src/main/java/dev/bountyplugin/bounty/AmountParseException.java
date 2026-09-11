package dev.bountyplugin.bounty;

public final class AmountParseException extends Exception {

    private final String rawInput;

    public AmountParseException(String rawInput) {
        super("Ungueltiger Betrag: " + rawInput);
        this.rawInput = rawInput;
    }

    public String getRawInput() {
        return rawInput;
    }
}
