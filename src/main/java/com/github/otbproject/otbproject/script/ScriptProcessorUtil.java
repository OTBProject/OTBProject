package com.github.otbproject.otbproject.script;

import com.github.otbproject.otbproject.fs.FSUtil;

import java.io.File;

public class ScriptProcessorUtil {
    private ScriptProcessorUtil() {}

    public static void cacheFromDirectory(ScriptProcessor processor, String directory) {
        FSUtil.streamDirectory(new File(directory))
                .filter(File::isFile)
                .forEach(file -> processor.cache(file.getName(), file.getPath()));
    }
}
