package com.github.otbproject.otbproject.command.parser;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.script.ScriptProcessor;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TermLoader {
    private static final ScriptProcessor PROCESSOR = new ScriptProcessor(false);
    private static final String METHOD_NAME = "getTerm";

    public static boolean loadTerm(String scriptName) {
        try {
            ParserTerm term = PROCESSOR.process(scriptName, (FSUtil.termScriptDir() + File.separator + scriptName), METHOD_NAME, null, ParserTerm.class, null);
            App.logger.debug(Files.lines(new File(FSUtil.termScriptDir() + File.separator + scriptName).toPath()).collect(Collectors.joining("\n")));
            return (term != null) && (term.value() != null) && (term.action() != null) && CommandResponseParser.registerTerm(term);
        } catch (Exception | IllegalAccessError e) {
            App.logger.catching(e);
            return false;
        }
    }

    public static void loadTerms() {
        File[] files = new File(FSUtil.termScriptDir()).listFiles();
        if (files == null) {
            return;
        }
        Stream.of(files)
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .forEach(script -> {
                    App.logger.debug("Attempting to load custom term from script: " + script);
                    if (loadTerm(script)) {
                        App.logger.debug("Successfully loaded custom term from script: " + script);
                    } else {
                        App.logger.error("Failed to load custom term from script: " + script);
                    }
                });
    }
}
