package com.github.otbproject.otbproject.command.parser;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.script.ScriptProcessor;

import java.io.File;

public class TermLoader {
    private static final ScriptProcessor PROCESSOR = new ScriptProcessor(false);
    private static final String METHOD_NAME = "getTerm";

    private TermLoader() {}

    public static boolean loadTerm(String scriptName) {
        try {
            ParserTerm term = PROCESSOR.process(scriptName, (FSUtil.termScriptDir() + File.separator + scriptName), METHOD_NAME, null, ParserTerm.class, null);
            return (term != null) && (term.value() != null) && (term.action() != null) && CommandResponseParser.registerTerm(term);
        } catch (Exception | IllegalAccessError e) {
            App.logger.catching(e);
            return false;
        }
    }

    public static void loadTerms() {
        FSUtil.streamDirectory(new File(FSUtil.termScriptDir()))
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
