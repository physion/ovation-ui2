package us.physion.ovation.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.actions.spi.FileManager;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

@Messages({
    "# {0} - data element name",
    "Getting_file=Getting file for {0}"
})
public class RevealElementAction extends AbstractAction {

    private final FileManager manager;
    private final DataElement element;

    public RevealElementAction(DataElement element) {
        super();

        manager = Lookup.getDefault().lookup(FileManager.class);
        this.element = element;

        putValue(NAME, manager.getRevealText());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EventQueueUtilities.runOffEDT(new Runnable() {
            @Override
            public void run() {
                try {
                    final File f = element.getData().get();
                    EventQueueUtilities.runOnEDT(new Runnable() {
                        @Override
                        public void run() {
                            manager.revealInFinder(f);
                        }
                    });
                } catch (InterruptedException ex) {
                    throw new OvationException(ex);
                } catch (ExecutionException ex) {
                    throw new OvationException(ex);
                }
            }
        }, ProgressHandleFactory.createHandle(Bundle.Getting_file(element.getName())));
    }
}
