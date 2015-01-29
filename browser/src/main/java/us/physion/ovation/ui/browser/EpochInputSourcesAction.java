package us.physion.ovation.ui.browser;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

@ActionID(
    category = "Navigate",
id = "us.physion.ovation.ui.browser.EpochInputSourcesAction")
@ActionRegistration(
    displayName = "#CTL_EpochInputSourcesAction")
@Messages("CTL_EpochInputSourcesAction=Input Sources")
public final class EpochInputSourcesAction extends AbstractInputsAction<Epoch, Source> {

    @Override
    protected Epoch getRecord(IEntityWrapper wrapper) {
        return wrapper.getEntity(Epoch.class);
    }

    @Override
    protected Map<String, Source> getInputs(Epoch record) {
        Map<String, Source> result = Maps.newHashMap();
        for (Map.Entry<String, Collection<Source>> e : record.getInputSources().asMap().entrySet()) {
            int i = 0;
            if (e.getValue().size() == 1) {
                Source src = Iterables.getFirst(e.getValue(), null);
                if (src != null) {
                    result.put(e.getKey(), src);
                }
            } else {
                for (Source s : e.getValue()) {
                    result.put(e.getKey() + "_" + Integer.toString(i++), s);
                }
            }
        }

        return result;
    }

    @Override
    protected String getDisplayName(Source data) {
        return data.getLabel();
    }

    @Override
    protected String getMenuDisplayName() {
        return Bundle.CTL_EpochInputSourcesAction();
    }

    @Override
    public String getName() {
        return Bundle.CTL_EpochInputSourcesAction();
    }
}
