/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import com.google.common.collect.Lists;
import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.ByteOrder;
import java.util.*;
import loci.formats.meta.MetadataRetrieve;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
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
        wiz.putProperty("epochs", null);
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
        List<File> fileList = Lists.newArrayList();
        for (FileMetadata f: files)
        {
            fileList.add(f.getFile());
        }
        wiz.putProperty("files", fileList);

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
        Map<String, Object> epoch = new HashMap();
        epoch.put("name", "epoch" + i);
        epoch.put("start", data.getStart());
        epoch.put("end", data.getEnd(false));
        epoch.put("properties", data.getEpochProperties());
        epoch.put("measurements", data.getMeasurements());
        epoch.put("deviceParameters", data.getDeviceParameters());
        
        List<Map<String, Object>> epochs = (List<Map<String, Object>>)wiz.getProperty("epochs");
        if (epochs == null)
        {
            epochs = Lists.newLinkedList();
        }
        epochs.add(epoch);
        wiz.putProperty("epochs", epochs);
    }

    private void importMultipleEpochs(WizardDescriptor wiz, FileMetadata data, int i) {
        Map<String, Object> parentEpochGroup = data.getParentEpochGroup();
        parentEpochGroup.put("number", i);
        parentEpochGroup.put("start", data.getStart());
        parentEpochGroup.put("end", data.getEnd(false));
        parentEpochGroup.put("epoch.properties", data.getEpochProperties());//TODO: maybe FileMetadata should do this?
        
        List<Map<String, Object>> parentEpochGroups = (List<Map<String, Object>>)wiz.getProperty("parentEpochGroups");
        if (parentEpochGroups == null)
        {
            parentEpochGroups = Lists.newLinkedList();
        }
        parentEpochGroups.add(parentEpochGroup);
        wiz.putProperty("parentEpochGroups", parentEpochGroups);
    }
}
