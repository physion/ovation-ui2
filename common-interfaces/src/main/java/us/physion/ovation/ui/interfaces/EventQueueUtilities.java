package us.physion.ovation.ui.interfaces;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import us.physion.ovation.exceptions.OvationException;

public class EventQueueUtilities
{
    private static final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
            Executors.newFixedThreadPool(2)); //TODO We'd like to move to a work stealing queue when we can target JDK 8

    public static <T>  ListenableFuture<T> runOnEDT(Callable<T> c) {
        if(EventQueue.isDispatchThread()) {
            try {
                return Futures.immediateFuture(c.call());
            } catch (Exception ex) {
                return Futures.immediateFailedFuture(ex);
            }
        } else {
            ListenableFutureTask<T> task = ListenableFutureTask.create(c);

            SwingUtilities.invokeLater(task);

            return task;
        }

    }

    public static ListenableFuture<Void> runOnEDT(final Runnable r) {
	if (EventQueue.isDispatchThread()) {
            r.run();
	    return Futures.immediateFuture(null);
	} else {
            ListenableFutureTask<Void> task = ListenableFutureTask.create(() -> {
                r.run();
                return null;
            });

	    SwingUtilities.invokeLater(r);

            return task;
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


    public static ListenableFuture<Void> runOffEDT(Runnable r, final ProgressHandle ph) {
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

    public static <T> ListenableFuture<T> runOffEDT(Callable<T> r, final ProgressHandle ph) {
        if (EventQueue.isDispatchThread()) {
            start(ph);
            ListenableFuture<T> f = executorService.submit(r);
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
            T result;
            try {
                result = r.call();
            } catch (Exception ex) {
                throw new OvationException("Operation failed", ex);
            }
            finish(ph);
            return Futures.immediateFuture(result);
        }
    }

    public static <T> ListenableFuture<T> runOffEDT(Callable<T> r) {
        if (EventQueue.isDispatchThread()) {
            ListenableFuture<T> f = executorService.submit(r);


            return f;
        } else {
            T result;
            try {
                result = r.call();
            } catch (Exception ex) {
                throw new OvationException("Operation failed", ex);
            }
            return Futures.immediateFuture(result);
        }
    }
}