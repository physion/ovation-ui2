/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static javax.swing.Action.NAME;
import org.joda.time.DateTime;
import org.openide.WizardDescriptor;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochContainer;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
public class InsertMeasurement extends InsertEntity {
    public InsertMeasurement() {
        putValue(NAME, "Insert Measurement...");
    }

    @Override
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
    {
        Epoch epoch = parent.getEntity(Epoch.class);
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new MeasurementController(epoch));
        panels.add(new SourceNameSelectionController());
        panels.add(new DeviceNameSelectionController());
        return panels;
    }

    @Override
    public void wizardFinished(WizardDescriptor wiz, DataContext c, IEntityWrapper parent)
    {
        Source parentEntity = (Source)parent.getEntity();
        
        Map<String, Source> parentSources = (Map<String, Source>) wiz.getProperty("source.parents");
        Epoch epoch = (Epoch)wiz.getProperty("source.epoch");
        if (epoch == null)
        {
            EpochContainer ec = (EpochContainer) wiz.getProperty("source.epochContainer");

            DateTime start = (DateTime) wiz.getProperty("epoch.start");
            DateTime end = (DateTime) wiz.getProperty("epoch.end");
            Protocol protocol = getProtocolFromProtocolSelector(parentEntity.getDataContext(),
                    (Map<String, String>) wiz.getProperty("epoch.newProtocols"),
                    (String) wiz.getProperty("epoch.protocolName"),
                    (Protocol) wiz.getProperty("epoch.protocol"));

            Map<String, Object> protocolParameters = (Map<String, Object>) wiz.getProperty("epoch.protocolParameters");
            Map<String, Object> deviceParameters = (Map<String, Object>) wiz.getProperty("epoch.deviceParameters");
            
            epoch = ec.insertEpoch(parentSources, null, start, end, protocol, protocolParameters, deviceParameters);
        }

        parentEntity.insertSource(parentSources,
                epoch,
                (String) wiz.getProperty("source.name"),
                (String)wiz.getProperty("source.label"),
                    (String)wiz.getProperty("source.identifier"));
    }
}
