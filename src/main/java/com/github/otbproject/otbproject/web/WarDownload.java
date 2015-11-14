package com.github.otbproject.otbproject.web;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.util.ThreadUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.*;

class WarDownload {
    private static final int ATTEMPTS = 2;
    static final String WAR_PREFIX = "web-interface-";
    static final String WAR_EXT = ".war";
    private static final String DL_EXT = ".download";

    private WarDownload() {}

    public static void downloadLatest() {
        ExecutorService executor = ThreadUtil.getSingleThreadExecutor("War Download");
        App.logger.info("Downloading web interface version " + WebVersion.latest());

        boolean success = false;

        for (int i = 1; i <= ATTEMPTS; i++) {
            App.logger.info("Attempting to download web interface (" + i + "/" + ATTEMPTS + ")");
            Future<Void> future = null;
            try {
                future = executor.submit(WarDownload::doDownload);
                future.get(1, TimeUnit.MINUTES);
                if (!moveTempDownload()) {
                    throw new WarDownloadException("Failed to rename download file to final file name");
                }
                success = true;
                break;
            } catch (TimeoutException | InterruptedException e) {
                if (e instanceof TimeoutException) {
                    App.logger.error("Taking too long to download web interface - aborting...");
                } else {
                    App.logger.error("Thread interrupted while waiting for web interface download to complete");
                    Thread.currentThread().interrupt();
                }
                future.cancel(true); // Doesn't seem possible for future to be null, even though FindBugs claims it might be
                App.logger.error("Stopping after " + i + "/" + ATTEMPTS + " download attempts");
                cleanupTempDownload();
                break;
            } catch (WarDownloadException | ExecutionException e) {
                App.logger.error("Error downloading web interface");
                App.logger.catching(e);
                App.logger.error("Failed attempt to download web interface (" + i + "/" + ATTEMPTS + ")");
                cleanupTempDownload();
            }
        }

        if (success) {
            App.logger.info("Successfully downloaded web interface");
            cleanupOldVersions();
        } else {
            App.logger.error("Failed to download web interface.");
            App.logger.warn("Please download the web interface yourself from 'https://github.com/OTBProject/OTBWebInterface/releases/latest'," +
                    " and put it in: " + FSUtil.webDir() + File.separator);
        }
    }

    private static void cleanupTempDownload() {
        if (!new File(dlPath()).delete()) {
            App.logger.error("Failed to cleanup files from failed download");
        }
    }

    private static boolean moveTempDownload() {
        String dlPath = dlPath();
        String finalPath = dlPath.substring(0, dlPath.length() - DL_EXT.length());
        return new File(dlPath).renameTo(new File(finalPath));
    }

    private static void cleanupOldVersions() {
        FSUtil.streamDirectory(new File(FSUtil.webDir()))
                .filter(File::isFile)
                .filter(file -> file.getName().startsWith(WarDownload.WAR_PREFIX))
                .filter(file -> file.getName().endsWith(WarDownload.WAR_EXT))
                .filter(file -> !file.getName().equals(WAR_PREFIX + WebVersion.latest() + WAR_EXT))
                .forEach(File::delete);
    }

    private static Void doDownload() throws WarDownloadException {
        String latestVersion = WebVersion.latest().toString();
        String warURL = "https://github.com/OTBProject/OTBWebInterface/releases/download/" + latestVersion + "/web-interface-" + latestVersion + ".war";
        String dlPath = dlPath();
        try {
            URL website = new URL(warURL);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(dlPath);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            URL md5Original = new URL(warURL + ".md5");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(md5Original.openStream()));
                 FileInputStream fis = new FileInputStream(dlPath)) {
                String inputLine = in.readLine();
                String md5 = DigestUtils.md5Hex(fis);
                if (!md5.equals(inputLine)) {
                    throw new WarDownloadException("Download of War file either corrupted or some 3rd party has changed the file");
                }
            }
        } catch (ClosedByInterruptException ignored) {
            App.logger.error("War download interrupted before it could complete");
        } catch (IOException e) {
            throw new WarDownloadException(e);
        }
        return null;
    }

    private static String dlPath() {
        return FSUtil.webDir() + File.separator + WAR_PREFIX + WebVersion.latest() + WAR_EXT + DL_EXT;
    }
}
