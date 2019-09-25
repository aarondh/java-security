package org.daisleyharrison.security.services.vault.utilities;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;

public class Glob {
    public static interface GlobAction {
        public void action(Path file) throws Exception;
    }

    public static class Finder extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private GlobAction action;
        private Path rootPath;
        private Path toRelativePath(Path absolutePath){
            return rootPath.relativize(absolutePath);
        }
    
        Finder(String pattern, Path rootPath, GlobAction action) {
            this.matcher = FileSystems.getDefault().getPathMatcher(pattern);
            this.rootPath = rootPath;
            this.action = action;
        }

        // Compares the glob pattern against
        // the file or directory name.
        void find(Path file) throws Exception {
            Path path = toRelativePath(file);
            if (matcher.matches(path)) {
                action.action(file);
            }
        }

        // Invoke the pattern matching
        // method on each file.
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            try {
                find(file);
                return CONTINUE;
            } catch (Exception exception) {
                return TERMINATE;
            }
        }

        // Invoke the pattern matching
        // method on each directory.
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return CONTINUE;
        }
    }

    private Path startingDirectory;
    private String pattern;

    public Glob(Path startingDirectory, String pattern) {
        this.startingDirectory = startingDirectory;
        this.pattern = pattern;
    }

    public void forEach(GlobAction action) throws IOException {

        Finder finder = new Finder(pattern, startingDirectory, action);
        Files.walkFileTree(startingDirectory, finder);
    }
}