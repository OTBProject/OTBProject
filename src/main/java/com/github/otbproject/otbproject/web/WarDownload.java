package com.github.otbproject.otbproject.web;

import com.github.otbproject.otbproject.App;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class WarDownload implements Runnable {

    @Override
    public void run() {
        String war = "http://ts.tldcode.uk:8081/nexus/content/repositories/releases/com/github/otbproject/web-interface/"+WebVersion.get()+"/"+"web-interface-"+WebVersion.get()+".war";
        URL website = null;
        try {
            website = new URL(war);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(WebStart.WAR_PATH);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            URL md5Original = new URL(war+".md5");
            BufferedReader in = new BufferedReader(new InputStreamReader(md5Original.openStream()));
            String inputLine = in.readLine();
            in.close();
            FileInputStream fis = new FileInputStream(new File(WebStart.WAR_PATH));
            String md5 = DigestUtils.md5Hex(fis);
            fis.close();
            if(!slowEquals(fromHex(md5), fromHex(inputLine))){
                App.logger.error("Download of War file either corrupted or some 3rd party has changed the file");
            }
        } catch (IOException e) {
           App.logger.catching(e);
        }
    }

    public static boolean slowEquals(byte[] a, byte[] b)
    {
        int diff = a.length ^ b.length;
        for(int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }
    public static byte[] fromHex(String hex)
    {
        byte[] binary = new byte[hex.length() / 2];
        for(int i = 0; i < binary.length; i++)
        {
            binary[i] = (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return binary;
    }
}
