package us.physion.ovation.ui.editor;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import us.physion.ovation.exceptions.OvationException;

public class OpenInNativeAppAction extends AbstractAction {

    private final Future<File> fileFuture;
    private final File file;

    public OpenInNativeAppAction(File file) {
        this(file, null);
    }
    
    public OpenInNativeAppAction(Future<File> fileFuture) {
        this(null, fileFuture);
    }
    
    private OpenInNativeAppAction(File file, Future<File> fileFuture) {
        //TODO: L10N
        super("Open in native application...");
        this.file = null;
        this.fileFuture = fileFuture;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        try {
            File f = file != null ? file : fileFuture.get();
            if (Desktop.isDesktopSupported()) {
                if (f.getAbsolutePath().length() > 254) {
                    Desktop.getDesktop().open(f.getParentFile());
                } else {
                    Desktop.getDesktop().open(f);
                }
            } else {
                //TODO: L10N
                throw new OvationException("Java Desktop not supported on this machine");
            }
        } catch (InterruptedException ex) {
            throw new OvationException(ex);
        } catch (ExecutionException ex) {
            throw new OvationException(ex);
        } catch (IOException ex) {
            throw new OvationException(ex);
        }
    }
}
