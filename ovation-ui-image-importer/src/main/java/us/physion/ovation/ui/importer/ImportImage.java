/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import us.physion.ovation.ui.browser.insertion.NamedSourceController;
import us.physion.ovation.ui.browser.insertion.KeyValueController;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import us.physion.ovation.ui.interfaces.*;
import loci.common.DateTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.in.PrairieReader;
import loci.formats.FormatReader;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;
import org.joda.time.DateTime;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import ovation.*;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.*;
import us.physion.ovation.exceptions.OvationException;

@ServiceProvider(service = EpochGroupInsertable.class)
/**
 *
 * @author huecotanks
 */
public class ImportImage extends InsertEntity implements EpochGroupInsertable
{
    ArrayList<FileMetadata> files;
    public ImportImage()
    {
        putValue(NAME, "Import Image...");
    }
    
    @Override
    public int getPosition() {
        return 101;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) 
    {
        setFiles();
        super.actionPerformed(ae);
    }
    
    public void setFiles()
    {
        JFileChooser chooser = new JFileChooser();
        FileFilter filter = new ImageFileFilter();
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(new JPanel());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            files = new ArrayList<FileMetadata>();
            File file = chooser.getSelectedFile();
            if (file != null)
            {
                files.add(new FileMetadata(file));
            } else {
                for (File f : chooser.getSelectedFiles()) {
                    files.add(new FileMetadata(f));
                }
            }
        }
    }
    
    @Override
    public List<Panel<WizardDescriptor>> getPanels(IEntityWrapper iew) {
        List<Panel<WizardDescriptor>> panels = new ArrayList<Panel<WizardDescriptor>>();
        int epochCount = files.size();

        panels.add(new GetImageFilesController(files));//set the files, and start/end times
        panels.add(new EquipmentSetupController());//set equipment setup info
        panels.add(new NamedSourceController("sources", null));

        for (int i = 0; i < epochCount; i++) {
            panels.add(new ProtocolController(i));//set protocol info
            panels.add(new KeyValueController(
                    "Epoch " + (i+1) + ": Protocol Parameters", 
                    "Enter any relevent protocol parameters below. These parameters will be associated with Epoch " + (i+1), 
                    "epochs;" + i + ";protocolParameters"));
            panels.add(new KeyValueController(
                    "Epoch " + (i+1) + ": Device Parameters", 
                    "Enter any relevent device parameters below. These parameters will be associated with Epoch " + (i+1), 
                    "epochs;" + i + ";deviceParameters"));
            int measurementCount = files.get(i).getMeasurements().size();
            for (int j = 0; j < measurementCount; j++) {
                panels.add(new MeasurementDetailsController(i, j));
                panels.add(new MeasurementSourceNamesController(i, j));
                panels.add(new MeasurementDeviceNamesController(i, j));
            }
        }
        return panels;
    }

    @Override
    public void wizardFinished(WizardDescriptor wd, DataContext c, IEntityWrapper iew) {
        EpochGroup eg = ((EpochGroup)iew.getEntity());
        Experiment exp = eg.getExperiment();
        
        Map<String, Object> equipmentSetup = (Map<String, Object>) wd.getProperty("equipmentSetup");
        EquipmentSetup es = exp.getEquipmentSetup();
        
        if (es == null)
        {
            exp.setEquipmentSetup(equipmentSetup);
        }else{
            for (String key : equipmentSetup.keySet())
            {
                es.addDeviceDetail(key, equipmentSetup.get(key));
            }
        }
        
        List<Map<String, Object>> epochs = (List<Map<String, Object>>) wd.getProperty("epochs");
        
        for (Map<String, Object> epoch : epochs)
        {
            Protocol protocol = null;
            if (epoch.containsKey("protocol"))
            {
                protocol = (Protocol)epoch.get("protocol");
            }else if (epoch.get("protocolName") != null && epoch.get("protocolDocument") != null){
                protocol = eg.getDataContext().insertProtocol((String)epoch.get("protocolName"), (String)epoch.get("protocolDocument"));
            }
            
            Map<String, Source> input = (Map<String, Source>)wd.getProperty("sources");
            DateTime start = (DateTime)epoch.get("start");
            DateTime end = (DateTime)epoch.get("end");
            Map<String, Object> protocolParameters = (Map<String, Object>)epoch.get("protocolParameters");
            Map<String, Object> deviceParameters = (Map<String, Object>)epoch.get("deviceParameters");
            Map<String, Object> epochProperties = (Map<String, Object>)epoch.get("properties");
            Epoch e = eg.insertEpoch(input, null, start, end, protocol, protocolParameters, deviceParameters);
            for (String key : epochProperties.keySet())
            {
                Object val = epochProperties.get(key);
                if (val != null)
                    e.addProperty(key, val);
            }
            
            List<Map<String, Object>> measurements = (List<Map<String, Object>>)epoch.get("measurements");
            for (Map<String, Object> m : measurements)
            {
                Measurement measurement;
                try {
                    measurement = e.insertMeasurement((String) m.get("name"),
                            (Set<String>) m.get("sourceNames"),//set by sourceSelector? 
                            (Set<String>) m.get("deviceNames"),
                            new URL((String) m.get("url")),
                            (String) m.get("mimeType"));
                } catch (MalformedURLException ex) {
                    throw new OvationException(ex);
                }

                Map<String, Object> measurementProperties = (Map<String, Object>)m.get("properties");
                for (String key : measurementProperties.keySet()) {
                    Object val = measurementProperties.get(key);
                    if (val != null) {
                        measurement.addProperty(key, val);
                    }
                }
            }
        }

        List<Map<String, Object>> parentEpochGroups = (List<Map<String, Object>>) wd.getProperty("parentEpochGroups");
        if (parentEpochGroups != null) {
            for (Map<String, Object> parentEpochGroup : parentEpochGroups) {
                Protocol protocol = null;
                if (parentEpochGroup.containsKey("protocol")) {
                    protocol = (Protocol) parentEpochGroup.get("protocol");
                } else if (parentEpochGroup.get("protocolName") != null && parentEpochGroup.get("protocolDocument") != null) {
                    protocol = eg.getDataContext().insertProtocol((String) parentEpochGroup.get("protocolName"), (String) parentEpochGroup.get("protocolDocument"));
                }
                DateTime start = (DateTime) parentEpochGroup.get("start");
                DateTime end = (DateTime) parentEpochGroup.get("end");
                String label = (String) parentEpochGroup.get("label");

                Map<String, Object> protocolParameters = (Map<String, Object>) parentEpochGroup.get("protocolParameters");//set by wherever
                Map<String, Object> deviceParameters = (Map<String, Object>) parentEpochGroup.get("deviceParameters");//set by which panel
                Map<String, Object> parentEpochGroupProperties = (Map<String, Object>) parentEpochGroup.get("properties");
                EpochGroup parent = eg.insertEpochGroup(label, start, protocol, protocolParameters, deviceParameters);
                for (String key : parentEpochGroupProperties.keySet()) {
                    Object val = parentEpochGroupProperties.get(key);
                    if (val != null) {
                        parent.addProperty(key, val);
                    }
                }
                for (Map<String, Object> epochGroup : (List<Map<String, Object>>) parentEpochGroup.get("egs")) {
                }
                //TODO: finish prairie import
            /*
                 * List<Map<String, Object>> measurements = (List<Map<String,
                 * Object>>)epoch.get("measurements"); for (Map<String, Object>
                 * m : measurements) {
                 * e.insertMeasurement((String)m.get("name"),
                 * (Set<String>)m.get("sourceNames"),
                 * (Set<String>)m.get("deviceNames"), (URL)m.get("url"),
                 * (String)m.get("mimeType"));
            }
                 */
            }
        }
    }
}
