package com.github.otbproject.otbproject.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.util.version.Version;
import com.github.otbproject.otbproject.util.version.Versions;

import java.io.File;
import java.util.Optional;

public class WebVersion {
    private static class LatestVersionHolder {
        private static final Version WEB_VERSION;
        private static final Version REQUIRED_APP_VERSION;

        static {
            Optional<JsonNode> optional = Versions.getJsonForLatestGithubRelease("otbwebinterface");
            WEB_VERSION = Version.parseAsOptional(Versions.getVersionFromJsonNodeOptional(optional))
                    .orElse(Version.create(0, 0, Version.Type.RELEASE));
            REQUIRED_APP_VERSION = Version.parseAsOptional(getRequiredAppVersion(optional))
                    .orElse(Version.create(0, 0, Version.Type.RELEASE));
        }
    }

    private WebVersion() {}

    public static Version latest() {
        return LatestVersionHolder.WEB_VERSION;
    }

    public static Version requiredAppVersionForLatest() {
        return LatestVersionHolder.REQUIRED_APP_VERSION;
    }

    static Optional<Version> lookupCurrent() {
        return FSUtil.streamDirectory(new File(FSUtil.webDir()))
                .filter(File::isFile)
                .map(File::getName)
                .filter(s -> s.startsWith(WarDownload.WAR_PREFIX))
                .filter(s -> s.endsWith(WarDownload.WAR_EXT))
                .map(s -> {
                    String versionStr = s.substring(WarDownload.WAR_PREFIX.length(), (s.length() - WarDownload.WAR_EXT.length()));
                    return Version.parseAsOptional(versionStr).orElse(null);
                })
                .filter(version -> version != null)
                .max(Version::compareTo);
    }

    private static String getRequiredAppVersion(Optional<JsonNode> optional) {
        if (optional.isPresent()) {
            String bodyText = optional.get().path("body").textValue();
            return bodyText.substring("Minimum Required App Version: ".length()).split("\\s", 2)[0];
        }
        return null;
    }
}