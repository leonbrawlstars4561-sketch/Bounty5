package dev.bountyplugin.economy;

public final class EconomyResult {

    private final boolean success;
    private final String errorMessage;

    private EconomyResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static EconomyResult success() {
        return new EconomyResult(true, null);
    }

    public static EconomyResult failure(String errorMessage) {
        return new EconomyResult(false, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
