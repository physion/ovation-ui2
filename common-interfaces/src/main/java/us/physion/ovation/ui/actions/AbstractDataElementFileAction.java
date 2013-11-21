package us.physion.ovation.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

@Messages({
    "# {0} - data element name",
    "Getting_file=Getting file for {0}"
})
public abstract class AbstractDataElementFileAction extends AbstractAction {

    protected final DataElement element;

    public AbstractDataElementFileAction(DataElement element) {
        this.element = element;
    }

    protected abstract void process(File f);

    @Override
    public void actionPerformed(ActionEvent e) {
        EventQueueUtilities.runOffEDT(new Runnable() {
            @Override
            public void run() {
                try {
                    final File f = element.getData().get();
                    process(f);
                } catch (InterruptedException ex) {
                    throw new OvationException(ex);
                } catch (ExecutionException ex) {
                    throw new OvationException(ex);
                }
            }
        }, ProgressHandleFactory.createHandle(Bundle.Getting_file(element.getName())));
    }
}
