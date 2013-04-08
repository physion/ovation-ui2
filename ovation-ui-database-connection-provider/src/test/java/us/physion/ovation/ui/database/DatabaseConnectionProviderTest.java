/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;
import us.physion.ovation.ui.interfaces.IUpgradeDB;
import us.physion.ovation.ui.test.OvationTestCase;

/**
 *
 * @author huecotanks
 */
public class DatabaseConnectionProviderTest extends OvationTestCase {
    
    private class DummyDialog implements CancellableDialog {

        boolean cancelled = false;
        public DummyDialog() {}
        public void cancel()
        {
            cancelled = true;
        }                
        public boolean isCancelled(){
            return cancelled;
        }
        public void showDialog(){}
    }
    public DatabaseConnectionProviderTest() {
    }
    
    @Test
    public void testShouldRunUpdaterReturnsFalseIfUserCancels(){
        DBConnectionManager d = new DBConnectionManager();
        d.setInstallVersionDialog(new DummyDialog());
        DummyDialog cancelledDialog = new DummyDialog();
        cancelledDialog.cancel();
        d.setShouldRunDialog(cancelledDialog);
        
        assertFalse(d.shouldRunUpdater(1, 2));
    }
    
    @Test
    public void testShouldRunUpdaterReturnsTrueIfUserPressesOK(){
        DBConnectionManager d = new DBConnectionManager();
        d.setInstallVersionDialog(new DummyDialog());
        d.setShouldRunDialog(new DummyDialog());
        
        assertTrue(d.shouldRunUpdater(1, 2));
    }
     
    @Test
    public void testShouldRunUpdaterReturnsFalseIfDatabaseVersionAndAPIVersionMatch(){
        DBConnectionManager d = new DBConnectionManager();
        d.setInstallVersionDialog(new DummyDialog());
        d.setShouldRunDialog(new DummyDialog());
        
        assertFalse(d.shouldRunUpdater(1, 1));
    }
    
    @Test
    public void testShouldRunUpdaterReturnsTrueIfDatabaseVersionIsLessThanSchemaVersion(){
        DBConnectionManager d = new DBConnectionManager();
        d.setInstallVersionDialog(new DummyDialog());
        d.setShouldRunDialog(new DummyDialog());
        
        assertTrue(d.shouldRunUpdater(1, 3));
    }
    
    @Test
    public void testShouldRunUpdaterReturnsTrueIfDatabaseVersionIsGreaterThanSchemaVersionButAlertsUserOfThis(){
        DBConnectionManager d = new DBConnectionManager();
        d.setInstallVersionDialog(new DummyDialog());
        d.setShouldRunDialog(new DummyDialog());
        
        assertTrue(d.shouldRunUpdater(2, 1)); 
    }
    
    @Test
    public void testRunUpdaterReturnsFalseIfCancelled()
    {   
        DBConnectionManager d = new DBConnectionManager();
        CancellableDialog running = new DummyDialog();
        running.cancel();
        assertFalse(d.runUpdater(new TestUpgradeTool(), running));
    }
    
    @Test 
    public void testRunUpdaterReturnsTrueIfNotCancelled()
    {
        DBConnectionManager d = new DBConnectionManager();
        assertTrue(d.runUpdater(new TestUpgradeTool(), new DummyDialog()));
    }
    
    @Test
    public void testRunUpdaterWaitsForUsersToQuitBeforeRunning()
    {
        
        //TODO
    }
    
    @Test
    public void testPluginDependanciesAreHandledCorrectly()
    {
        //TODO
    }
    
    class TestUpgradeTool implements IUpgradeDB{

        @Override
        public void start() {
            //pass
        }
    }
}
