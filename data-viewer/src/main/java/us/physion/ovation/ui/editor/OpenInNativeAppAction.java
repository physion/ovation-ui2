package us.physion.ovation.ui.editor;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

@Messages({
    "Open_in_native_application_button=Open in native application...",
    "# {0} - data element name",
    "Getting_and_opening_file=Getting and opening file for {0}",
    "Java_Desktop_not_supported=Java Desktop not supported on this machine"
})
public class OpenInNativeAppAction extends AbstractAction {

    private final DataElement fileFuture;
    private final File file;

    public OpenInNativeAppAction(File file) {
        this(file, null);
    }

    public OpenInNativeAppAction(DataElement fileFuture) {
        this(null, fileFuture);
    }

    private OpenInNativeAppAction(File file, DataElement fileFuture) {
        super(Bundle.Open_in_native_application_button());
        this.file = file;
        this.fileFuture = fileFuture;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (fileFuture == null) {
            openFile(file);
        } else {
            EventQueueUtilities.runOffEDT(new Runnable() {
                @Override
                public void run() {
                    try {
                        final File f = fileFuture.getData().get();
                        EventQueueUtilities.runOnEDT(new Runnable() {
                            @Override
                            public void run() {
                                openFile(f);
                            }
                        });
                    } catch (InterruptedException ex) {
                        throw new OvationException(ex);
                    } catch (ExecutionException ex) {
                        throw new OvationException(ex);
                    }
                }
            }, ProgressHandleFactory.createHandle(Bundle.Getting_and_opening_file(fileFuture.getName())));
        }
    }

    private void openFile(File f) {
        try {
            if (Desktop.isDesktopSupported()) {
                if (f.getAbsolutePath().length() > 254) {
                    Desktop.getDesktop().open(f.getParentFile());
                } else {
                    Desktop.getDesktop().open(f);
                }
            } else {
                throw new OvationException(Bundle.Java_Desktop_not_supported());
            }
        } catch (IOException ex) {
            throw new OvationException(ex);
        }
    }
}
