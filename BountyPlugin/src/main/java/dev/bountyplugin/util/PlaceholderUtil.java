package dev.bountyplugin.util;

import java.util.Map;

public final class PlaceholderUtil {

    private PlaceholderUtil() {
    }

    public static String apply(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
