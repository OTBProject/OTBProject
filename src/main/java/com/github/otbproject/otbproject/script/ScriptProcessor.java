package com.github.otbproject.otbproject.script;

import com.github.otbproject.otbproject.App;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptProcessor {
    private static final GroovyShell SHELL = new GroovyShell();

    private final ConcurrentHashMap<String, Script> cache;
    private final boolean doCache;

    public ScriptProcessor(boolean doCache) {
        this.doCache = doCache;
        if (doCache) {
            cache = new ConcurrentHashMap<>();
        } else {
            cache = null;
        }
    }

    public <T> T process(String scriptName, String path, String methodName, Object paramContainer, Class<T> responseClass, T defaultResponse) {
        T response;
        try {
            Script script;
            Object scriptReturn;

            // Get script
            if (doCache && cache.containsKey(scriptName)) {
                script = cache.get(scriptName);
            } else {
                script = SHELL.parse(new File(path));
                if (doCache) {
                    cache.put(scriptName, script);
                }
            }

            App.logger.debug("Running script: " + scriptName);
            scriptReturn = script.invokeMethod(methodName, paramContainer);
            App.logger.debug("Finished running script: " + scriptName);
            if ((scriptReturn == null) || !(responseClass.isInstance(scriptReturn))) {
                App.logger.warn("Missing or invalid return statement in script: " + scriptName);
                response = defaultResponse;
            } else {
                response = responseClass.cast(scriptReturn);
            }
        } catch (Exception e) {
            App.logger.error("Exception when running script: " + scriptName);
            App.logger.catching(e);
            response = defaultResponse;
        }

        App.logger.debug("Script '" + scriptName + "' returned: " + response);
        return response;
    }

    public void dropFromScriptCache(String scriptName) {
        if (doCache) {
            cache.remove(scriptName);
        }
    }

    public void clearScriptCache() {
        if (doCache) {
            cache.clear();
        }
    }

    public void cache(String scriptName, String path) {
        Script script;
        try {
            script = SHELL.parse(new File(path));
            cache.put(scriptName, script);
        } catch (IOException e) {
            App.logger.catching(e);
        }
    }
}
