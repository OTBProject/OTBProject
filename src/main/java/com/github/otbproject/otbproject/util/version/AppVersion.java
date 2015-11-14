package com.github.otbproject.otbproject.util.version;

public class AppVersion {
    private static final Version CURRENT = getCurrentVersion();

    private AppVersion() {}

    private static class LatestVersionHolder {
        private static final Version FIELD = Version.parseAsOptional(Versions.lookupLatestGithubVersion("otbproject"))
                .orElse(Version.create(0, 0, 0, Version.Type.RELEASE));
    }

    private static String getVersionString() {
        return AppVersion.class.getPackage().getImplementationVersion();
    }

    public static Version current() {
        return CURRENT;
    }

    private static Version getCurrentVersion() {
        Version version;
        try {
            version = Version.parseVersion(getVersionString());
        } catch (Version.ParseException e) {
            e.printStackTrace(); // because logger not initialized, and should also never fail in actual execution
            version = Version.create(0, 0, 0, Version.Type.RELEASE);
        }
        return version;
    }

    public static Version latest() {
        return LatestVersionHolder.FIELD;
    }
}
