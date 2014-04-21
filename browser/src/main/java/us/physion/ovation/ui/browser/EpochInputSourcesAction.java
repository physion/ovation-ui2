package us.physion.ovation.ui.browser;

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
        return record.getInputSources();
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
