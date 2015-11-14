package com.github.otbproject.otbproject.web;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.config.Configs;
import com.github.otbproject.otbproject.config.WebConfig;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.util.version.Version;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class WebInterface {
    private WebInterface() {}

    public static void start() {
        if (Configs.getWebConfig().get(WebConfig::isUpdateChecking)) {
            checkForNewVersion();
        }
        startInterface(Configs.getWebConfig().get(WebConfig::getPortNumber), Configs.getWebConfig().get(WebConfig::getIpBinding));
    }

    public static void checkForNewVersion() {
        Version current = WebVersion.lookupCurrent().orElse(Version.create(0, 0, Version.Type.RELEASE));
        File path = new File(warPath(current));
        if (App.VERSION.type == Version.Type.SNAPSHOT) {
            App.logger.warn("You are running a dev build of OTB. Please grab the latest build of the web interface and place in \"" +
                    FSUtil.webDir() + File.separator + "\" as \"web-interface-" + WebVersion.latest() +
                    ".war\". Releases will automatically download the latest version of the web interface for you");
        } else if ((App.VERSION.compareTo(WebVersion.requiredAppVersionForLatest()) >= 0)
                && (!path.exists()
                || (Configs.getWebConfig().get(WebConfig::isAutoUpdating)
                && (current.compareTo(WebVersion.latest()) < 0)))) {
            WarDownload.downloadLatest();
        }
    }

    private static void startInterface(int port, String address) {
        Version current = WebVersion.lookupCurrent().orElse(Version.create(0, 0, Version.Type.RELEASE));
        App.logger.info("Starting web interface version " + current);
        Server server = new Server();
        ServerConnector http = new ServerConnector(server);
        http.setHost(address);
        http.setPort(port);
        server.addConnector(http);
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(warPath(current));
        server.setHandler(webapp);
        try {
            server.start();
        } catch (Exception e) {
            App.logger.catching(e);
        }
    }

    static String warPath(Version version) {
        return FSUtil.webDir() + File.separator + "web-interface-" + version + ".war";
    }

    public static void openInBrowser() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI.create("http://127.0.0.1:" + Configs.getWebConfig().get(WebConfig::getPortNumber)));
            } catch (IOException e) {
                App.logger.catching(e);
            }
        } else {
            App.logger.warn("Unable to open web interface in browser - desktop not supported");
        }
    }
}
