// src/main/java/com/sonnhuynhh/shopnest/utils/VietnameseUtils.java
package com.sonnhuynhh.shopnest.utils;

public class VietnameseUtils {
    private static final String VIETNAMESE = "àáảãạâầấẩẫậăằắẳẵặèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ";
    private static final String ENGLISH    = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyydd";

    public static String removeAccent(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toLowerCase().toCharArray()) {
            int index = VIETNAMESE.indexOf(c);
            sb.append(index >= 0 ? ENGLISH.charAt(index) : c);
        }
        return sb.toString();
    }
}