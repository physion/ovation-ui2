package us.physion.ovation.ui.browser;

import java.util.Map;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

@ActionID(
    category = "Navigate",
id = "us.physion.ovation.ui.browser.MeasurementInputSourcesAction")
@ActionRegistration(
    displayName = "#CTL_MeasurementInputSourcesAction")
@Messages("CTL_MeasurementInputSourcesAction=Input Sources")
public final class MeasurementInputSourcesAction extends AbstractInputsAction<Measurement, Source> {

    @Override
    protected Measurement getRecord(IEntityWrapper wrapper) {
        return wrapper.getEntity(Measurement.class);
    }

    @Override
    protected Map<String, Source> getInputs(Measurement record) {
        return record.getEpoch().getInputSources();
    }

    @Override
    protected String getDisplayName(Source data) {
        return data.getLabel();
    }

    @Override
    protected String getMenuDisplayName() {
        return Bundle.CTL_MeasurementInputSourcesAction();
    }

    @Override
    public String getName() {
        return Bundle.CTL_MeasurementInputSourcesAction();
    }
}
