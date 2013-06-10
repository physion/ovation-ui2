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
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochContainer;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.RootInsertable;
import us.physion.ovation.ui.interfaces.SourceInsertable;

@ServiceProviders(value={
    @ServiceProvider(service=SourceInsertable.class)
})
/**
 *
 * @author huecotanks
 */
public class InsertChildSource extends InsertEntity implements SourceInsertable{
    public InsertChildSource() {
        putValue(NAME, "Insert Source...");
    }

    @Override
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
    {
        Source p = parent.getEntity(Source.class);
        Map<String, Source> defaultParents = new HashMap();
        defaultParents.put(p.getLabel(), p);
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new NameWizard("source"));
        panels.add(new InsertSourceWizardPanel1());
        //panels.add(new NamedSourceController("source.parents", defaultParents));
        //panels.add();//select epoch or epoch container
        //panels.add();//start and end time
        panels.add(new SelectProtocolController("epoch"));//protocol
        panels.add(new KeyValueController("Add Protocol Parameters", "Add optional protocol parameters", "epoch.protocolParameters"));
        panels.add(new KeyValueController("Add Device Parameters", "Add optional device parameters", "epoch.deviceParameters"));
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
                (Epoch) wiz.getProperty("source.epoch"),
                (String) wiz.getProperty("source.name"),
                (String)wiz.getProperty("source.label"),
                    (String)wiz.getProperty("source.identifier"));
    }
}
