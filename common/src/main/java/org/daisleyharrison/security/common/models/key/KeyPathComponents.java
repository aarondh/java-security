package org.daisleyharrison.security.common.models.key;

/**
 * Support for key path with the following syntax:
 * 
 * {key-path} = {path} [ "/" "public" ] [ "@"" {version-number} ]
 * 
 * {path} = {root} "/" {alias}
 * 
 * {version-path} = {key-path}
 * 
 * {version-alias} = {alias} [ "@"" {version-number} ]
 */
public interface KeyPathComponents {
    public static final String PATH_SEPARATOR = "/";
    public static final String PATH_VERSION_SEPARATOR = "@";
    public static final String PUBLIC_KEY_PATH_SUFFIX = PATH_SEPARATOR + "public";

    public String getPath();

    public String getRoot();

    public String getAlias();

    public String getVersionPath();

    public boolean isPublic();

    public int getVersion();

    public String getVersionAlias();

    public static String removeSuffix(String path, String suffix) {
        if (path == null) {
            return path;
        }
        if (suffix == null) {
            return path;
        }
        int versionSep = path.indexOf(PATH_VERSION_SEPARATOR);
        String pathMinusVersion;
        String version;
        if (versionSep < 0) {
            pathMinusVersion = path;
            version = "";
        } else {
            pathMinusVersion = path.substring(0, versionSep);
            version = path.substring(0, versionSep);
        }
        if (pathMinusVersion.endsWith(suffix)) {
            return pathMinusVersion.substring(0, pathMinusVersion.length() - suffix.length()) + version;
        }
        return path;
    }

    public static String addSuffix(String path, String suffix) {
        if (path == null) {
            return path;
        }
        if (suffix == null) {
            return path;
        }
        int versionSep = path.indexOf(PATH_VERSION_SEPARATOR);
        if (versionSep < 0) {
            return join(path, suffix);
        } else {
            return join(path.substring(0, versionSep), suffix) + path.substring(versionSep);
        }
    }

    public static String removePrefix(String path, String prefix) {
        if (path == null) {
            return path;
        }
        if (prefix == null) {
            return path;
        }
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        return path;
    }

    public static String join(String leftPath, String rightPath) {
        if (leftPath == null) {
            return rightPath;
        }
        if (rightPath == null) {
            return leftPath;
        }
        if (leftPath.endsWith(PATH_SEPARATOR)) {
            if (rightPath.startsWith(PATH_SEPARATOR)) {
                return leftPath = leftPath + rightPath.substring(PATH_SEPARATOR.length());
            }
            return leftPath + rightPath;
        }
        int versionSep = leftPath.indexOf(PATH_VERSION_SEPARATOR);
        String version = "";
        if (versionSep >= 0) {
            leftPath = leftPath.substring(0, versionSep);
            version = leftPath.substring(versionSep);
        }
        if (rightPath.startsWith(PATH_SEPARATOR)) {
            return leftPath + rightPath + version;
        }
        return leftPath + PATH_SEPARATOR + rightPath + version;
    }

    public static KeyPathComponents getComponents(String root, String keyPath) {
        int versionSep = keyPath.indexOf(PATH_VERSION_SEPARATOR);
        String pathMinusVersion = versionSep >= 0 ? keyPath.substring(0, versionSep) : keyPath;
        return new KeyPathComponents() {

            @Override
            public boolean isPublic() {
                return pathMinusVersion.endsWith(PUBLIC_KEY_PATH_SUFFIX);
            }

            @Override
            public int getVersion() {
                if (versionSep >= 0) {
                    return Integer.parseInt(keyPath.substring(versionSep + 1));
                }
                return 0;
            }

            @Override
            public String getVersionPath() {
                return keyPath;
            }

            @Override
            public String getPath() {
                return pathMinusVersion;
            }

            @Override
            public String getRoot() {
                return root;
            }

            @Override
            public String getAlias() {
                return removeSuffix(removePrefix(pathMinusVersion, root), PUBLIC_KEY_PATH_SUFFIX);
            }

            @Override
            public String getVersionAlias() {
                return removeSuffix(getAlias(), PUBLIC_KEY_PATH_SUFFIX)
                        + (versionSep >= 0 ? keyPath.substring(versionSep) : "");
            }
        };
    }
}