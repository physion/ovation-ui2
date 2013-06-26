package us.physion.ovation.ui.interfaces;

import java.lang.Runnable;
import javax.swing.SwingUtilities;
import java.util.concurrent.FutureTask;

//TODO: call it something other than a listner
public class QueryListener {//implements PropertyChangeListener{

    private Runnable toRun;
    private FutureTask<Boolean> task;

    public QueryListener(Runnable r)
    {
        toRun = r;
    }
    
    public FutureTask<Boolean> run()
    {
	task = new FutureTask<Boolean>(toRun, true);
	task.run();  
        return task;
    }

    public boolean cancel()
    {
	if (task == null)
	{
	    return true;
	}
	if (task.isCancelled() || task.isDone())
	    return true;
	
	boolean cancelled = task.cancel(true);
	task = null;
	return cancelled;
    }


    /*@Override
	public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals("ovation.queryChanged"))
	    {
		toRun.run();
	    }
    }*/

}