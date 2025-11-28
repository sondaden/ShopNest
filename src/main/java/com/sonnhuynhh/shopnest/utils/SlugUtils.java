// src/main/java/com/sonnhuynhh/shopnest/utils/SlugUtils.java
package com.sonnhuynhh.shopnest.utils;

import java.text.Normalizer;

public class SlugUtils {
    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized
                .replaceAll("\\p{M}", "")
                .replace("đ", "d").replace("Đ", "D")
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "");
    }
}