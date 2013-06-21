/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static javax.swing.Action.NAME;
import org.joda.time.DateTime;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochContainer;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.Source;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.EpochInsertable;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.SourceInsertable;


@ServiceProviders(value={
    @ServiceProvider(service=EpochInsertable.class)
})
/**
 *
 * @author huecotanks
 */
public class InsertMeasurement extends InsertEntity implements EpochInsertable{
    public InsertMeasurement() {
        putValue(NAME, "Insert Measurement...");
    }

    @Override
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
    {
        Epoch epoch = parent.getEntity(Epoch.class);
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new DataElementController());
        panels.add(new SourceNameSelectionController(epoch));
        panels.add(new DeviceNameSelectionController(epoch));
        return panels;
    }

    @Override
    public void wizardFinished(WizardDescriptor wiz, DataContext c, IEntityWrapper parent)
    {
        Epoch e = (Epoch)parent.getEntity();
        String name = (String)wiz.getProperty("dataElement.name");
        String contentType = (String)wiz.getProperty("dataElement.contentType");
        URL file = (URL)wiz.getProperty("dataElement.file");
        Set<String> sourceNames = (Set<String>)wiz.getProperty("sourceNames");
        Set<String> deviceNames = (Set<String>)wiz.getProperty("deviceNames");

        e.insertMeasurement(name, sourceNames, deviceNames, file, contentType);
    }
}
