/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.ui.database.UpdateJarStep;
import us.physion.ovation.ui.database.UpdateSchemaStep;

import us.physion.ovation.ui.interfaces.*;

/**
 *
 * @author huecotanks
 */
public class UpgradeTool implements IUpgradeDB {

    private IUpdateProgress pu;
    List<UpdateInfo> updates;
    String connectionFile;
    String username;
    String password;
    IUpdateUI uiUpdater = null;
    boolean forceUpdate = false;

    public UpgradeTool(List<UpdateInfo> updates, String connectionFile, String username, String password) {
        this.updates = updates;
        this.connectionFile = connectionFile;
        this.username = username;
        this.password = password;
    }

    public UpgradeTool(List<UpdateInfo> updates, String connectionFile, String username, String password, IUpdateUI ui) {
        this(updates, connectionFile, username, password);
        uiUpdater = ui;
    }

    public static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.contains("win"));

    }

    public static boolean isMac() {

        String os = System.getProperty("os.name").toLowerCase();
        // Mac
        return (os.contains("mac"));

    }

    public static boolean isLinux() {

        String os = System.getProperty("os.name").toLowerCase();
        // linux
        return (os.contains("linux"));

    }

    public void start() {
        /*DataStoreCoordinator dsc = null;
        try {
            dsc = DataStoreCoordinator.coordinatorWithConnectionFile(connectionFile, "UPGRADE");
        } catch (DatabaseOpenException ex) {
            throw new OvationException(ex.getMessage(), ex);
        } catch (DatabaseNotFoundException ex) {
            throw new OvationException(ex.getMessage(), ex);
        }
        
        if (dsc.getStringDatabasePreference("OVATION_UPGRADE_CONNECTION_LIST").split(":").length > 1)
        {
            dsc.removeFromConnectionList(dsc.getContext(), "OVATION_UPGRADE_CONNECTION_LIST");
            throw new DatabaseIsUpgradingException();
        }
        while (dsc.getConnectedHosts().size() != 0)
        {
            update(-1, "Waiting for " + dsc.getConnectedHosts().size() + " other users to disconnect...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                throw new OvationException(ex.getMessage());
            }
            
            if (forceUpdate)
            {
                break;
            }
        }
        start(connectionFile, username, password);
        * 
        */
    }
    
    public void forceUpdate()
    {
        forceUpdate = true;
    }

    public void start(String connectionFile, String username, String password) {
        /*if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        
        try {
            pu = new ProgressUpdater(connectionFile, username, password, uiUpdater);
            UnicastRemoteObject.exportObject(pu, 10002);
            Registry r = LocateRegistry.createRegistry(10002);
            r.bind("ProgressUpdater", pu);

        } catch (RemoteException e) {
            throw new RuntimeException(e.getMessage());
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e.getMessage());
        }

        String platform = "";
        File objyBin = null;
        if (isWindows()) {
            objyBin = new File("C:\\Program Files\\Physion\\Ovation\\Objectivity\\bin"); 
            platform = "Windows";
        } else if (isLinux()) {
            objyBin = new File("/usr/object/linux86_64/bin/");
            platform = "Linux";
        } else if (isMac()) {
            objyBin = new File("/opt/object/mac86_64/bin/");
            platform = "Darwin";
        }

        System.out.println("Running on platform " + platform);
        update(-1, "Initializing");
        int totalUpdates = updates.size();
        Map<UpdateStep, File> fileMap = new HashMap<UpdateStep, File>();
        try {
            for (UpdateInfo update : updates) {
                List<UpdateStep> steps = update.getUpdateSteps();
                for (UpdateStep step : steps) {
                    File f = downloadFile(step.getStepDescriptor());
                    if (!f.exists())
                    {
                        throw new Exception(step.getStepDescriptor());
                    }
                    fileMap.put(step, f);
                }
            }
        }
        catch (Exception e){
            DataStoreCoordinator dsc;
            try {
                dsc = DataStoreCoordinator.coordinatorWithConnectionFile(connectionFile, "UPGRADE");
                dsc.upgradeFinished();
                dsc.close();
            } catch (DatabaseOpenException ex) {
                Exceptions.printStackTrace(ex);
            } catch (DatabaseNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            throw new RuntimeException("Couldn't download upgrade file. " + e.getMessage());
            
        }
        try{
            for (UpdateInfo update : updates) {
                List<UpdateStep> steps = update.getUpdateSteps();
                for (UpdateStep step : steps) {
                    File file = fileMap.get(step);
                    if (step instanceof UpdateJarStep) {
                        try {
                            ProcessBuilder pb = new ProcessBuilder("java",
                                    "-Djava.security.policy=security.txt",
                                    "-Dno.parallel.scan=true",
                                    "-cp",
                                    System.getProperty("java.class.path") + ":" + file.getAbsolutePath(),
                                    "-jar",
                                    file.getAbsolutePath(),
                                    connectionFile,
                                    username,
                                    password);

                            runCommand(pb);

                        } catch (Exception e) {
                            throw new RuntimeException("Could not run jar '" + file + "'. " + e.getClass() + ": " + e.getMessage());
                        }
                    } else if (step instanceof UpdateSchemaStep) {
                        
                        try {
                            ProcessBuilder pb = new ProcessBuilder(new File(objyBin, "ooschemaupgrade").getPath(),
                                    "-infile",
                                    file.getAbsolutePath(),
                                    connectionFile);

                            runCommand(pb);

                        } catch (Exception e) {
                            throw new RuntimeException("Could not upgrade schema using file '" + file + "'. " + e.getMessage());
                        }
                    }
                }
                System.out.println("Done with update");

                try {
                    DataStoreCoordinator dsc = DataStoreCoordinator.coordinatorWithConnectionFile(connectionFile, "UPGRADE");
                    dsc.getContext();
                    dsc.setDatabasePreference("OVATION_SCHEMA_VERSION", String.valueOf(update.getSchemaVersion()));
                    dsc.close();
                } catch (DatabaseOpenException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (DatabaseNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            try {
                DataStoreCoordinator dsc = DataStoreCoordinator.coordinatorWithConnectionFile(connectionFile, "UPGRADE");
                dsc.upgradeFinished();
                dsc.close();
            } catch (DatabaseOpenException ex) {
                Exceptions.printStackTrace(ex);
            } catch (DatabaseNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }

            for (UpdateStep s : fileMap.keySet())
            {
                fileMap.get(s).delete();
            }
            update(100, "Done");
            
            
        } catch (Exception e) {
            if (uiUpdater != null) {
                uiUpdater.cancel();
            }
            throw new OvationException(e.getMessage(), e);
        }
        * 
        */
    }
  
    private void update(int percent, String text)
    {
        if (uiUpdater == null)
            return;
        
        uiUpdater.update(percent, text);
    }
    
    private File downloadFile(String filename) 
    {
        if (filename.toLowerCase().startsWith("http"))
        {
            try {
                URL url = new URL(filename);
                String[] pathSegments = url.getPath().split("/");
                String name = pathSegments[pathSegments.length-1];
                File f = File.createTempFile(name.split("\\.")[0], name.split("\\.")[1]);
                //f.deleteOnExit();
                FileOutputStream fos = new FileOutputStream(f);
                InputStream input = new BufferedInputStream(url.openStream());
                
                byte[] buffer = new byte[4096*4];
                int n = - 1;
                int numOfBytes =0;
                while ((n = input.read(buffer)) != -1) {
                    if (n > 0) {
                        fos.write(buffer, 0, n);
                        numOfBytes += n;
                    }
                }
                
                return f;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                throw new RuntimeException(ex.getLocalizedMessage());
            } 

        }
        return new File(filename);
    }
    
    
    //TODO: why can't I use commandUtilities here, netbeans?
    private void runCommand(ProcessBuilder pb)
    {
        /*try {
            Process p = pb.start();

            InputStreamHandler inputHandler = new InputStreamHandler(new InputStreamReader(p.getInputStream()));
            InputStreamHandler errHandler = new InputStreamHandler(new InputStreamReader(p.getErrorStream()));

            inputHandler.start();
            errHandler.start();

            try {
                int err = p.waitFor();

                if (err != 0) {
                    System.out.println("Error: " + errHandler.getCaptureBuffer());
                    Ovation.getLogger().error("Unable to complete command: " + errHandler.getCaptureBuffer());
                    throw new OvationException(errHandler.getCaptureBuffer().toString());
                }
            } catch (InterruptedException e) {
                throw new OvationException("Unable to complete command: " + e.getLocalizedMessage());
            }

        } catch (IOException e) {
            throw new OvationException("Communication with sub-process lost: " + e.getLocalizedMessage());
        }
        * 
        */
    }
    
    class InputStreamHandler extends Thread {

        private StringBuffer captureBuffer;
        private InputStreamReader stream;

        public InputStreamReader getStream() {
            return stream;
        }

        public StringBuffer getCaptureBuffer() {
            return captureBuffer;
        }

        InputStreamHandler(InputStreamReader inputStream) {
            stream = inputStream;
            captureBuffer = new StringBuffer();
        }

        public void run() {
            try {
                int nextChar;
                while ((nextChar = getStream().read()) >= 0) {
                    getCaptureBuffer().append((char) nextChar);
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(UpgradeTool.class).error(e.getLocalizedMessage());
            }
        }
    }
}
