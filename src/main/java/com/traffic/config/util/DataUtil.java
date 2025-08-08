package com.traffic.config.util;

public class DataUtil {
    public static String formatBytes(byte[] bytes) {
        if (bytes.length == 0) return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < Math.min(bytes.length, 32); i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("0x%02X", bytes[i] & 0xFF));
        }
        if (bytes.length > 32) {
            sb.append(", ...(" + bytes.length + " bytes)");
        }
        sb.append("]");
        return sb.toString();
    }
}
