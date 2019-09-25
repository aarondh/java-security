package org.daisleyharrison.security.services.key.utilities;

import java.nio.file.Path;

public class PathUtils {
    private static final String PRIVATE_KEY_PATH_SUFFIX = "/private";
    private static final String PUBLIC_KEY_PATH_SUFFIX = "/public";
    private static final String SECRET_KEY_PATH_SUFFIX = "/secret";

    public static String toPrivateKeyPath(String keyPath) {
        return keyPath + PRIVATE_KEY_PATH_SUFFIX;
    }

    public static String toPublicKeyPath(String keyPath) {
        return keyPath + PUBLIC_KEY_PATH_SUFFIX;
    }

    public static String toSecretKeyPath(String keyPath) {
        return keyPath + SECRET_KEY_PATH_SUFFIX;
    }

    public static boolean isPublicKeyPath(String keyPath) {
        return keyPath == null ? false : keyPath.endsWith(PUBLIC_KEY_PATH_SUFFIX);
    }

    public static boolean isPrivateKeyPath(String keyPath) {
        return keyPath == null ? false : keyPath.endsWith(PRIVATE_KEY_PATH_SUFFIX);
    }

    public static boolean isSecretKeyPath(String keyPath) {
        return keyPath == null ? false : keyPath.endsWith(SECRET_KEY_PATH_SUFFIX);
    }

    public static String removeExtension(String path) {
        if (path != null) {
            int sep = path.lastIndexOf('.');
            if (sep >= 0) {
                return path.substring(0, sep);
            }
        }
        return path;
    }

    public static Path removeExtension(Path path) {
        if (path != null) {
            return Path.of(removeExtension(path.toString()));
        }
        return path;
    }
}