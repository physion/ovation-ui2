package ovation;

import java.rmi.Naming;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.ui.interfaces.*;


public class Updater {
    public boolean runUpdate(DataContext context, IUpdateProgress pu)
    {
	throw new UnsupportedOperationException("Subclasses of the Updater should override the runUpdate method");
    }
    
    /*public void runMain(String[] args)
    {
	if (args.length !=3) 
	{
	    System.out.println("Usage: java -jar <connectionfile> <username> <password>");
	    return;
	}

	DataStoreCoordinator c = null;
        DataContext context = null;
	try {
            c = DataStoreCoordinator.coordinatorWithConnectionFile(args[0], "UPGRADE");
	    context = c.getContext();
	    System.out.println("Getting context");
	    if (context == null)
		{
		    throw new OvationException("Something went wrong - context is null!");
		}
	    context.authenticateUser(args[1], args[2]);
	    System.out.println("Authenticating user");
        }catch (DatabaseOpenException e){
	    Ovation.getLogger().debug(e.getMessage());
	    throw new OvationException(e.getMessage(), e);
	} catch (DatabaseNotFoundException e){
	    Ovation.getLogger().debug(e.getMessage());
	    throw new OvationException(e.getMessage(), e);
	}
	catch (UserAuthenticationException e) {
	    Ovation.getLogger().debug(e.getMessage());
            throw new OvationException(e.getMessage(), e);
        }

        IUpdateProgress pu = null;
	try {                                                                                                                                                                                                                                                                                  
	     pu = (IUpdateProgress) Naming.lookup("rmi://localhost:10002/ProgressUpdater");                                                                                                                                                                                                     
	} catch (Exception e) {                                                                                                                                                                                                                                                                
	     pu = null;                                                                                                                                                                                                                                                                         
	}

	runUpdate(context, pu);
    }
    * 
    */

}