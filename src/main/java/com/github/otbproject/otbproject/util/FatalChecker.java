package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Control;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.gui.GuiApplication;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FatalChecker {
    private FatalChecker() {}

    public static void checkForPreviousFatalCrashes() {
        List<String> fileList = FSUtil.streamDirectory(new File(System.getProperty("user.dir")))
                .filter(File::isFile)
                .map(File::getName)
                .filter(s -> s.startsWith("OTBProjectFatal-"))
                .collect(Collectors.toList());
        String path = FSUtil.dataDir() + File.separator + "fatal-crash-files.json";
        Optional<String[]> optional = JsonHandler.readValue(path, String[].class);
        List<String> previousList = Arrays.asList(optional.orElse(new String[0]));
        JsonHandler.writeValue(path, fileList);
        fileList.removeAll(previousList);

        if (fileList.size() == 1) {
            App.logger.error("OTB experienced a fatal error the last time it ran");
            String fileName = fileList.get(0);
            App.logger.error("Please report this problem to the developers, and provide them with the file: " + fileName);
            if (Control.Graphics.present()) {
                GuiApplication.fatalErrorAlert(fileName);
            }
        } else if (fileList.size() > 1) {
            App.logger.error("OTB somehow seems to have experienced multiple fatal errors the last time it ran. This should not be possible.");
            App.logger.error("Please report this problem to the developers, and provide them with the files: "
                    + fileList.stream().collect(Collectors.joining(", ")));
            if (Control.Graphics.present()) {
                GuiApplication.multipleFatalErrorAlert(fileList);
            }
        }
    }
}
