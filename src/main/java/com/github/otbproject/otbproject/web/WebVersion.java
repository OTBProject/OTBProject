package com.github.otbproject.otbproject.web;

import com.github.otbproject.otbproject.App;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

public class WebVersion {

    public static String getWebVersion(){
        String xmlPath = "http://ts.tldcode.uk:8081/nexus/content/repositories/releases/com/github/otbproject/web-interface/maven-metadata.xml";
        URL xmlURL;
        BufferedReader in = null;
        try {
            xmlURL = new URL(xmlPath);
            in = new BufferedReader(new InputStreamReader(xmlURL.openStream()));

            Optional<String> stringOptional = in.lines()
                    .filter(line -> line != null)
                    .filter(line -> line.trim().startsWith("<release>"))
                    .map(line -> line.replace("<release>","").replace("</release>","").trim())
                    .findAny();
            if (stringOptional.isPresent()) {
                return stringOptional.get();
            }
        } catch (IOException e) {
            App.logger.catching(e);
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    App.logger.catching(e);
                }
            }
        }
        return "0.0.1";
    }
}