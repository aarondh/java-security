package org.daisleyharrison.security.services.openId.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StringUtils {
    public static final String DEFAULT_ELIPSIS = "...";
    public static final int DEFAULT_ELIPSIS_MAX_LENGTH = 64;

    public static String toElipsis(String value, int maxLength, String elipsis) {
        if (elipsis == null || elipsis.length() > DEFAULT_ELIPSIS_MAX_LENGTH) {
            throw new IllegalArgumentException("elipsis is invalid");
        }
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength is invalid");
        }
        if (value == null || value.length() < maxLength) {
            return value;
        } else {
            return value.substring(0, maxLength - elipsis.length()) + elipsis;
        }
    }

    public static String toElipsis(String value, int maxLength) {
        return toElipsis(value, maxLength, DEFAULT_ELIPSIS);
    }

    public static String toElipsis(String value) {
        return toElipsis(value, DEFAULT_ELIPSIS_MAX_LENGTH);
    }

    private final static String ISO_ENCODING = "ISO-8859-1";
    private final static int UTF8_BOM_LENGTH = 3;

    private static boolean isUTF8(byte[] bytes) {
        if ((bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return true;
        }
        return false;
    }

    public static String debom(String value) throws UnsupportedEncodingException {
        final byte[] bytes = value.getBytes(ISO_ENCODING);
        if (isUTF8(bytes)) {
            int length = bytes.length - UTF8_BOM_LENGTH;
            byte[] barray = new byte[length];
            System.arraycopy(bytes, UTF8_BOM_LENGTH, barray, 0, barray.length);
            return new String(barray, ISO_ENCODING);
        } else {
            return value;
        }

    }

    public static Map<String, List<String>> splitQuery(URL url) throws UnsupportedEncodingException {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        String query = url.getQuery();
        if (query != null) {
            final String[] pairs = query.split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, new LinkedList<String>());
                }
                final String value = idx > 0 && pair.length() > idx + 1
                        ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                        : null;
                query_pairs.get(key).add(value);
            }
        }
        return query_pairs;
    }

    public static String encodeAsWwwFormUrlEncoded(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        params.forEach((name, value) -> {
            if (result.length() > 0) {
                result.append("&");
            }
            try {
                result.append(URLEncoder.encode(name, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException exception) {
                // should never happen
            }
        });
        return result.toString();
    }
}