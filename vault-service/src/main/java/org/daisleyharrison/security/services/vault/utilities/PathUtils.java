package org.daisleyharrison.security.services.vault.utilities;

import java.nio.file.Path;

public class PathUtils {
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