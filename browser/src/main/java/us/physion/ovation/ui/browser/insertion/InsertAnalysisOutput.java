/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import com.google.common.collect.Lists;
import java.net.URL;
import java.util.List;
import static javax.swing.Action.NAME;
import org.openide.WizardDescriptor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.ui.interfaces.AnalysisRecordInsertable;
import us.physion.ovation.ui.interfaces.EpochGroupInsertable;
import us.physion.ovation.ui.interfaces.ExperimentInsertable;
import us.physion.ovation.ui.interfaces.IEntityWrapper;


@ServiceProviders(value={
    @ServiceProvider(service=AnalysisRecordInsertable.class)
})
/**
 *
 * @author huecotanks
 */
public class InsertAnalysisOutput extends InsertEntity implements AnalysisRecordInsertable{

    public InsertAnalysisOutput()
    {
        putValue(NAME, "Add Analysis output...");
    }
    @Override
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent) {
        return Lists.<WizardDescriptor.Panel<WizardDescriptor>>newArrayList(new DataElementController());
    }

    @Override
    public void wizardFinished(WizardDescriptor wiz, DataContext ctx, IEntityWrapper ew) {
        AnalysisRecord analysis= ew.getEntity(AnalysisRecord.class);
        String name = (String)wiz.getProperty("dataElement.name");
        String contentType = (String)wiz.getProperty("dataElement.contentType");
        URL file = (URL)wiz.getProperty("dataElement.file");
        analysis.addOutput(name, file, contentType);
    }
    
}
