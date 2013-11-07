package us.physion.ovation.ui.interfaces;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.awt.EventQueue;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Future;
import org.netbeans.api.progress.ProgressHandle;

public class EventQueueUtilities
{
    private static ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
            Executors.newCachedThreadPool());

    public static void runOnEDT(Runnable r) {
	if (EventQueue.isDispatchThread()) {
	    r.run();
	} else {
	    SwingUtilities.invokeLater(r);
	}
    }
    
    public static void runOnEDT(Runnable r, ProgressHandle ph) {
	if (EventQueue.isDispatchThread()) {
            ph.start();
	    r.run();
            ph.finish();
	} else {
            start(ph);
	    SwingUtilities.invokeLater(r);
            finish(ph);
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
    
    private static void start(final ProgressHandle ph)
    {
        runOnEDT(new Runnable(){

                        @Override
                        public void run() {
                            ph.start();                        
                        }
                    });
    }
    
    private static void finish(final ProgressHandle ph)
    {
        runOnEDT(new Runnable(){

                        @Override
                        public void run() {
                            ph.finish();                        
                        }
                    });
    }
    
    
    public static Future runOffEDT(Runnable r, final ProgressHandle ph) {
        if (EventQueue.isDispatchThread()) {
            start(ph);
            ListenableFuture f = executorService.submit(r);
            Futures.addCallback(f, new FutureCallback() {
                @Override
                public void onSuccess(Object result) {
                    finish(ph);
                }

                @Override
                public void onFailure(Throwable t) {
                    finish(ph);
                }
            });
            return f;
        } else {
            start(ph);
            r.run();
            finish(ph);
            return Futures.immediateFuture(null);
        }
    }
}