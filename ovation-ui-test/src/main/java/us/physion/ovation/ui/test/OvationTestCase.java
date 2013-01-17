/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.test;

import java.io.File;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.joda.time.DateTime;
import org.junit.*;
import ovation.IAuthenticatedDataStoreCoordinator;
import ovation.database.DatabaseManager;
import static org.junit.Assert.*;
import ovation.*;
/**
 *
 * @author huecotanks
 */
public class OvationTestCase {

    public OvationTestCase() { }
    private TestManager tm;
    public IAuthenticatedDataStoreCoordinator dsc;

    public void setTestManager(TestManager mgr)
    {
	tm = mgr;
    }

    
   public static void setUpDatabase(TestManager tm, int defaultID) {

        File f = new File(tm.getConnectionFile());
        if (!f.exists()) {
            String lockServer = System.getProperty("OVATION_LOCK_SERVER");
            if(lockServer == null) {
		try{
		    lockServer = InetAddress.getLocalHost().getHostName();
            	} catch (java.net.UnknownHostException e){
		    lockServer = "127.0.0.1";
		}
	    }
            
            String nodeFdIdString = System.getProperty("NODE_FDID");
	    if (nodeFdIdString == null)
	    {
		nodeFdIdString = System.getenv("NODE_FDID");
	    }
	    if (nodeFdIdString == null)
	    {
		nodeFdIdString = "0";
	    }
	    int nodeFdId = Integer.parseInt(nodeFdIdString);


            String jobFdIdString = System.getProperty("JOB_FDID");
	    if (jobFdIdString == null)
	    {
		jobFdIdString = System.getenv("JOB_FDID");
	    }
	    int jobFdId = defaultID;
	    if (jobFdIdString != null)
	    {
	        jobFdId = Integer.parseInt(jobFdIdString);
	    }
            
            DatabaseManager.createLocalDatabase(tm.getConnectionFile(), lockServer, nodeFdId + jobFdId);
        }

    }
    public static void tearDownDatabase(TestManager tm) throws Exception {
        DatabaseManager.deleteLocalDatabase(tm.getConnectionFile());
    }

    
    public IAuthenticatedDataStoreCoordinator setUpTest() {
        IAuthenticatedDataStoreCoordinator dsc = null;
	try {
            dsc = tm.setupDatabase();
        } catch (Exception e) {
	    System.err.println(e.getMessage());
            tearDownTest();
            fail(e.getMessage());
        }

        Ovation.enableLogging(LogLevel.DEBUG);
	return dsc;
    }

    public void tearDownTest() {
        tm.tearDownDatabase();
    }
    
}
