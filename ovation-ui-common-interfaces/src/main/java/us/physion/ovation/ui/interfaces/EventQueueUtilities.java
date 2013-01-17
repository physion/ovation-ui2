package us.physion.ovation.ui.interfaces;
import java.awt.EventQueue;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Future;

public class EventQueueUtilities
{
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);

    public static void runOnEDT(Runnable r) {
	if (EventQueue.isDispatchThread()) {
	    r.run();
	} else {
	    SwingUtilities.invokeLater(r);
	}
    }
    
    public static void runAndWaitOnEDT(Runnable r) throws InterruptedException {
	if (EventQueue.isDispatchThread()) {
	    r.run();
	} else {
	    try{
	    SwingUtilities.invokeAndWait(r);
	    } catch (InvocationTargetException e)
            {
                e.printStackTrace(); //TODO: handle this better
            }
	}
    }
    
    public static Future runOffEDT(Runnable r) {
	if (EventQueue.isDispatchThread()) {
	    return executorService.submit(r);
	} else {
	    FutureTask t = new FutureTask(r, true);
	    t.run();
	    return t;
	}
    }
}