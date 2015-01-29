package us.physion.ovation.ui.browser;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Collection;
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
        Map<String, Source> result = Maps.newHashMap();
        for (Map.Entry<String, Collection<Source>> e : record.getEpoch().getInputSources().asMap().entrySet()) {
            int i = 0;
            if (e.getValue().size() == 1) {
                Source src = Iterables.getFirst(e.getValue(), null);
                if(src != null) {
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
        return Bundle.CTL_MeasurementInputSourcesAction();
    }

    @Override
        public String getName() {
        return Bundle.CTL_MeasurementInputSourcesAction();
    }
}
