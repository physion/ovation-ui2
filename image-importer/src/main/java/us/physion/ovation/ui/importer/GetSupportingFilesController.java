/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import com.google.common.collect.Lists;
import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import us.physion.ovation.ui.browser.insertion.ListSelectionPanel;
import us.physion.ovation.ui.interfaces.BasicWizardPanel;

/**
 *
 * @author jackie
 */
public class GetSupportingFilesController extends BasicWizardPanel{
    File mainFile;
    int fileNumber;
    GetSupportingFilesController(int fileNumber)
    {
        super();
        this.fileNumber = fileNumber;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new SupportingFilesPanel(changeSupport);
       
        }
        return component;
    }
    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        SupportingFilesPanel c = (SupportingFilesPanel)getComponent();
        wiz.putProperty("supportingFiles", c.getFiles());
        
    }
    
    @Override
    public void readSettings(WizardDescriptor wiz) {
        List<File> files = (List<File>)wiz.getProperty("files");
        mainFile = files.get(fileNumber);
        SupportingFilesPanel c = (SupportingFilesPanel)getComponent();
        c.setMainImage(mainFile);
    }
}
