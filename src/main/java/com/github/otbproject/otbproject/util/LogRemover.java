package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.config.Configs;
import com.github.otbproject.otbproject.config.GeneralConfig;
import com.github.otbproject.otbproject.fs.FSUtil;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class LogRemover {
    private LogRemover() {}

    public static void removeOldLogs() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final long now = System.currentTimeMillis();
        final long removeAfter = Configs.getGeneralConfig().get(GeneralConfig::getOldLogsRemovedAfter);

        final Pattern filePattern = Pattern.compile("(app|web)-\\d{4}-\\d{2}-\\d{2}-\\d+\\.log");
        final Pattern endPattern = Pattern.compile("-\\d+\\.log");

        long count = FSUtil.streamDirectory(new File(FSUtil.logsDir()))
                .filter(file -> filePattern.matcher(file.getName()).matches())
                .filter(file -> {
                    String dateStr = endPattern.matcher(file.getName().substring(4)).replaceFirst("");
                    try {
                        Date logDate = dateFormat.parse(dateStr);
                        long then = logDate.getTime();
                        // Delete if more than 60 days old
                        if ((now - then) > TimeUnit.DAYS.toMillis(removeAfter)) {
                            return true;
                        }
                    } catch (ParseException e) { // Really shouldn't happen if it matched the Pattern
                        App.logger.catching(e);
                    }
                    return false;
                })
                .filter(file -> {
                    if (!file.delete()) {
                        App.logger.error("Failed to delete old log file: " + file.getName());
                        return false;
                    }
                    return true;
                })
                .count();

        if (count > 0) {
            App.logger.info("Removed " + count + " old log" + (count == 1 ? "" : "s"));
        }
    }
}
