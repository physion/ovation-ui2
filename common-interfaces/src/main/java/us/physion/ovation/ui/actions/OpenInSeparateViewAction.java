package us.physion.ovation.ui.actions;

import java.io.File;
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

    public OpenInSeparateViewAction(DataElement e) {
        super(e);

        putValue(NAME, Bundle.Open_In_Separate_View());
    }

    @Override
    protected void process(File f) {
        final Openable o = Lookup.getDefault().lookup(DataElementLookupProvider.class).getLookup(element).lookup(Openable.class);
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
