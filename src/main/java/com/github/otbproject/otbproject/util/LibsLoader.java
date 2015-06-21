package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.fs.FSUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class LibsLoader {
    public static void load() {
        File libsdir = new File(FSUtil.scriptLibsDir());
        FilenameFilter fileNameFilter = (dir, name) -> {
            if (name.lastIndexOf('.') > 0) {
                // get last index for '.' char
                int lastIndex = name.lastIndexOf('.');

                // get extension
                String str = name.substring(lastIndex);

                // match path name extension
                if (str.equals(".jar")) {
                    return true;
                }
            }
            return false;
        };
        for (File jar : libsdir.listFiles(fileNameFilter)) {
            try {
                addSoftwareLibrary(jar);
            } catch (NoSuchMethodException | MalformedURLException | InvocationTargetException | IllegalAccessException e) {
                App.logger.catching(e);
            }
        }
    }

    private static void addSoftwareLibrary(File file) throws NoSuchMethodException, MalformedURLException, InvocationTargetException, IllegalAccessException {
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(ClassLoader.getSystemClassLoader(), file.toURI().toURL());
        App.logger.debug("File loaded: " + file.getName());
    }
}
