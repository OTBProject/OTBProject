package com.github.otbproject.otbproject.command.parser;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.script.ScriptProcessor;

import java.io.File;
import java.util.stream.Stream;

public class TermLoader {
    private static final ScriptProcessor PROCESSOR = new ScriptProcessor(false);
    private static final String METHOD_NAME = "getTerm";

    public static boolean loadTerm(String scriptName) {
        ParserTerm term = PROCESSOR.process(scriptName, (FSUtil.termScriptDir() + File.separator + scriptName), METHOD_NAME, null, ParserTerm.class, null);
        return (term != null) && (term.value() != null) && (term.action() != null) && CommandResponseParser.registerTerm(term);
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
