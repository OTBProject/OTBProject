package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.App;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.zip.ZipFile;

public class Unpacker {
    private Unpacker() {}

    public static void unpack(String jarDir, String destDir) {
        try {
            ZipFile jar = new ZipFile(new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
            jar.stream()
                    .filter(zipEntry -> zipEntry.getName().startsWith(jarDir) && !zipEntry.isDirectory())
                    .forEach(zipEntry -> {
                        try {
                            App.logger.debug("Unpacking: " + zipEntry.getName());
                            String fileName = zipEntry.getName().substring(jarDir.length());
                            InputStream in = jar.getInputStream(zipEntry);
                            OutputStream out = new FileOutputStream(destDir + File.separator + fileName);
                            IOUtils.copy(in, out);
                            IOUtils.closeQuietly(in);
                            out.close();
                        } catch (IOException e) {
                            App.logger.catching(e);
                        }
                    });
            jar.close();
        } catch (URISyntaxException | IOException e) {
            App.logger.catching(e);
        }
    }
}
