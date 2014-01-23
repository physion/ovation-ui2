package us.physion.ovation.ui.actions;

import java.io.File;
import java.net.URI;
import java.util.List;
import org.netbeans.api.actions.Openable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.ui.actions.spi.DataElementLookupProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

@Messages({
    "Open_In_Separate_View=Open"
})
public class OpenInSeparateViewAction extends AbstractDataElementFileAction {
    private final List<URI> entityURI;

    public OpenInSeparateViewAction(DataElement e) {
        this(e, null);
    }
    
    public OpenInSeparateViewAction(DataElement e, List<URI> entityURI) {
        super(e);

        this.entityURI = entityURI;
        
        putValue(NAME, Bundle.Open_In_Separate_View());
    }

    @Override
    protected void process(File f) {
        final Openable o = Lookup.getDefault().lookup(DataElementLookupProvider.class).getLookup(element, entityURI).lookup(Openable.class);
        if (o != null) {
            EventQueueUtilities.runOnEDT(new Runnable() {
                @Override
                public void run() {
                    o.open();
                }
            });
        }
    }
}
