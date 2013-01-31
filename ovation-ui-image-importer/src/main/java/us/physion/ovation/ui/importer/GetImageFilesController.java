/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.ByteOrder;
import java.util.*;
import loci.formats.meta.MetadataRetrieve;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import ovation.ExternalDevice;
import ovation.NumericDataFormat;
import ovation.NumericDataType;
import ovation.OvationException;
import us.physion.ovation.ui.interfaces.BasicWizardPanel;

/**
 *
 * @author huecotanks
 */
public class GetImageFilesController extends BasicWizardPanel{

    ArrayList<FileMetadata> files;
    GetImageFilesController(ArrayList<FileMetadata> files)
    {
        super();
        this.files = files;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new GetImageFilesPanel(changeSupport, files);
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
        GetImageFilesPanel c = (GetImageFilesPanel)component;
        if (c != null)
        {
            List<FileMetadata> files = c.getFiles();
            return files.size() != 0;
        }
        return false;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        GetImageFilesPanel c = (GetImageFilesPanel)component;
        List<FileMetadata> files = c.getFiles();
        
        //set the user-approved start and end times
        int count =0;
        for (FileMetadata f : files)
        {
            f.setStart(c.getStart(count));
            f.setEnd(c.getEnd(count++));
        }
        Collections.sort(files, new FileMetadataComparator());

        Map<String, Map<String, Object>> devices = new HashMap<String, Map<String, Object>>();
        for (int i=0; i< files.size(); i++)
        {
            FileMetadata data = files.get(i);
            
            for (Map<String, Object> device : data.getDevices())
            {
                String id = (String)device.get("ID");
                if (devices.containsKey(id))
                {
                    devices.put(id, combineDevices(devices.get(id), device));
                }else{
                    devices.put(id, device);
                }
            }
            if (data.containsSingleEpoch())
            {
                importSingleEpoch(wiz, data, i);
            }else
            {
                importMultipleEpochs(wiz, data, i);
            }
        }
        wiz.putProperty("devices", devices);
    }

    private Map<String, Object> combineDevices(Map<String, Object> device1, Map<String, Object> device2) {
        device1.putAll(device2);
        return device1;
    }

    private void importSingleEpoch(WizardDescriptor wiz, FileMetadata data, int i) {
        String epochName = "epoch" + i;
        wiz.putProperty(epochName + ".start", data.getStart());
        wiz.putProperty(epochName + ".end", data.getEnd(false));

        wiz.putProperty(epochName + ".properties", data.getEpochProperties());

        for (Map<String, Object> response : data.getResponses()) {
            String responseName = epochName + "." + (String) response.get("name");
            response.remove("name");
            for (String key : response.keySet()) {
                wiz.putProperty(responseName + "." + key, response.get(key));
            }
        }
    }

    private void importMultipleEpochs(WizardDescriptor wiz, FileMetadata data, int i) {
        Map<String, Object> parentEpochGroup = data.getParentEpochGroup();
        parentEpochGroup.put("start", data.getStart());
        parentEpochGroup.put("end", data.getEnd(false));
        
        wiz.putProperty("parentEpochGroup", parentEpochGroup);
        wiz.putProperty("epoch.properties", data.getEpochProperties());
        wiz.putProperty("responses", data.getResponses());
    }
}
