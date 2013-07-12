/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author jackie
 */
public class FileUtilities {
    public static void downloadFile(URL url)
    {
        try{
            File f = File.createTempFile("file-test", "jar");
            System.out.println("File: " + f.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(f);

            InputStream input = new BufferedInputStream(url.openStream());

            byte[] buffer = new byte[4096 * 4];
            int count = 0;
            int n = -1;
            while ((n = input.read(buffer)) != -1) {
                if (n > 0) {
                    fos.write(buffer, 0, n);
                    count += n;
                }
            }
            System.out.println("Number of bytes: " + count);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws MalformedURLException
    {
        downloadFile(new URL("https://s3.amazonaws.com/com.physionconsulting.ovation.updates/1.4/Update_3.jar"));
    }
}
