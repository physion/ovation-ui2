package us.physion.ovation.ui.actions;

import java.io.File;
import org.openide.util.Lookup;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.ui.actions.spi.FileManager;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

public class RevealElementAction extends AbstractResourceFileAction {

    private final FileManager manager;

    public RevealElementAction(Resource element) {
        super(element);

        manager = Lookup.getDefault().lookup(FileManager.class);

        putValue(NAME, manager.getRevealText());
    }

    @Override
    protected void process(final File f) {
        EventQueueUtilities.runOnEDT(new Runnable() {
            @Override
            public void run() {
                manager.revealInFinder(f);
            }
        });
    }
}
