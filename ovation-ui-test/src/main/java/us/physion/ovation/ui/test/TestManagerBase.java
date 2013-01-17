/*
 * Copyright (c) 2012. Physion Consulting LLC
 * All rights reserved.
 */

package us.physion.ovation.ui.test;

import com.objy.db.app.Session;
import com.objy.db.app.oo;
import com.objy.db.app.ooAPObj;
import com.objy.db.app.ooDBObj;
import ovation.*;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Base class for Objectivity/DB-backed Ovation unit tests.
 */
abstract class TestManagerBase {

    abstract public String getLicenseText();
    abstract public String getLicenseInstitution();
    abstract public String getLicenseGroup();

    abstract public String getConnectionFile();
    abstract public String getFirstUserName();
    abstract public String getFirstUserPassword();

    public IDataStoreCoordinator dsc = null;

    public IAuthenticatedDataStoreCoordinator setupDatabase() throws Exception {

        dsc = DataStoreCoordinator.coordinatorWithConnectionFile(getConnectionFile());
        //license database
        try {
            dsc.licenseDatabase( getLicenseInstitution(), getLicenseGroup(), getLicenseText(), false);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            dsc.close();
            dsc = null;
            throw e;
        }

        DataContext ctx = dsc.getContext();

        ctx.addUser(getFirstUserName(), getFirstUserPassword());
        ctx.authenticateUser(getFirstUserName(), getFirstUserPassword());

        return ctx.getAuthenticatedDataStoreCoordinator();
    }

    public void tearDownDatabase()
    {
        if(dsc == null) {
            return;
        }

        boolean databasesDeleted = false;
        try {
            dsc.activate();
            Session s = dsc.getContext().getSession();

            if(s.isTerminated())
                throw new OvationException("tearDownDatabase: session is terminated");

            s.join();

            s.begin();
            s.setOpenMode(oo.openReadWrite);
            s.setAllowNonQuorumRead();

            //set all autonomous partitions online
            s.setOfflineMode(oo.IGNORE);
            s.getFD().getBootAP().ensureAllImagesInQuorums();
            Iterator apitr = s.getFD().containedAPs();
            while (apitr.hasNext())
            {
                ooAPObj ap = (ooAPObj)(apitr.next());
                ap.setOnline(true);
            }

            //compile list of databases to delete
            Iterator itr = s.getFD().containedDBs();
            ArrayList<ooDBObj> toDelete = new ArrayList();
            while (itr.hasNext()) {
                toDelete.add((ooDBObj) itr.next());
            }

            s.commit();

            //Delete databases in a separate commit
            s.begin();
            s.setOpenMode(oo.openReadWrite);
            s.setAllowNonQuorumRead();
            for (ooDBObj db : toDelete){
                db.delete();
            }

            s.commit();


            if (s.isOpen())
            {
                s.abort();
                throw new OvationException("Transaction was not properly closed");
            }
            databasesDeleted = true;

        } finally {
            if (databasesDeleted)
                dsc.closeOpenContexts();//this method doesn't try to remove hosts from the Preferences object
            else{
                dsc.close();
            }
        }
    }

}
