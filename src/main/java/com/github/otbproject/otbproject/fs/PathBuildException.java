package com.github.otbproject.otbproject.fs;

public class PathBuildException extends RuntimeException {
    public PathBuildException() {
        super("Unable to build path: unspecified path parts");
    }

    public PathBuildException(String message) {
        super(message);
    }
}
