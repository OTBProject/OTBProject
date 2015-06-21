package com.github.otbproject.otbproject.util;

public class VersionClass {

    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
}
