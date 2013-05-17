/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
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
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import ovation.*;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.domain.*;

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
        
        panels.add(new GetImageFilesController(files));
        for (int i = 0; i < epochCount; i++) {
            panels.add(new EpochDetailsController(i));

            int responseCount = files.get(i).getMeasurements().size();
            for (int j = 0; j < responseCount; j++) {
                panels.add(new DeviceDetailsController(i, j));
                panels.add(new ResponseDetailsController(i, j));
            }
        }
        return panels;
    }

    @Override
    public void wizardFinished(WizardDescriptor wd, DataStoreCoordinator dsc, IEntityWrapper iew) {
        EpochGroup eg = ((EpochGroup)iew.getEntity());
        Experiment exp = eg.getExperiment();
        
        Map<String, Map<String, Object>> devices = (Map<String, Map<String, Object>>) wd.getProperty("devices");
        EquipmentSetup es = exp.getEquipmentSetup();
        for (String deviceName : devices.keySet()) {
            Map<String, Object> device = devices.get(deviceName);
            //name, manufacturer, properties
            Map<String, Object> properties = (Map<String, Object>) device.get("properties");
            String prefix = device.get("name") + "." + device.get("manufacturer") + ".";
            for (String key : properties.keySet()) {
                es.addDeviceDetail(prefix + key, properties.get(key));
            }
        }

        List<Map<String, Object>> epochs = (List<Map<String, Object>>) wd.getProperty("epochs");
        
        int i =0;
        for (Map<String, Object> epoch : epochs)
        {
            Protocol protocol;
            if (epoch.containsKey("protocol"))
            {
                protocol = (Protocol)epoch.get("protocol");
            }else{
                protocol = eg.getDataContext().insertProtocol((String)epoch.get("protocolName"), (String)epoch.get("protocolDocument"));
            }
            DateTime start = (DateTime)epoch.get("start");
            DateTime end = (DateTime)epoch.get("end");
            Map<String, Object> protocolParameters = (Map<String, Object>)epoch.get("protocolParameters");
            Map<String, Object> epochProperties = (Map<String, Object>)epoch.get("properties");
            Epoch e = eg.insertEpoch(start, end, protocol, protocolParameters, null);//no device parameters
            for (String key : epochProperties.keySet())
            {
                Object val = epochProperties.get(key);
                if (val != null)
                    e.addProperty(key, val);
            }
            
            List<Map<String, Object>> measurements = (List<Map<String, Object>>)epoch.get("measurements");
            for (Map<String, Object> m : measurements)
            {
                e.insertMeasurement((String)m.get("name"), 
                        (Set<String>)m.get("sourceNames"), 
                        (Set<String>)m.get("deviceNames"), 
                        (URL)m.get("url"),
                        (String)m.get("mimeType"));
            }
        }
        for(;;)
        {
            String epochName = "epoch" + String.valueOf(i++);
            
            //create a protocol, if it is a new one
            Protocol protocol = null;
            String protocolID = (String)wd.getProperty(epochName + ".protocolID");//Is that where we put the protocol id
            if (protocolID == null)
            {
                break;//no more epochs
            }
            Map<String, Object> protocolParameters = (Map<String, Object>) wd.getProperty(epochName + ".protocolParameters");            
            DateTime start = (DateTime) wd.getProperty(epochName + ".start");
            DateTime end = (DateTime) wd.getProperty(epochName + ".end");
            Map<String, Object> epochProperties = (Map<String, Object>) wd.getProperty(epochName + ".properties");

            Epoch e = eg.insertEpoch(start, end, protocol, protocolParameters, null);
            for (String key : epochProperties.keySet())
            {
                Object val = epochProperties.get(key);
                if (val != null)
                    e.addProperty(key, val);
            }

            //TODO: multiple devices
            //TODO: measurement associated with multiple sources?
            int j = 0;
            for (;;) {
                String responseName = epochName + ".response" + j;
                j++;
                String deviceName = (String) wd.getProperty(responseName + ".device.name");
                if (deviceName == null) {
                    break;
                }
                String deviceManufacturer = (String)wd.getProperty(responseName + ".device.manufacturer");    
                Map<String, Object> deviceParameters = (Map<String, Object>) wd.getProperty(responseName + ".device.parameters");
                String url = (String) wd.getProperty(responseName + ".url");
                long[] shape = (long[]) wd.getProperty(responseName + ".shape");
                String type = (String) wd.getProperty(responseName + ".dataType");
                String units = (String) wd.getProperty(responseName + ".units");
                String[] dimensionLabels = (String[]) wd.getProperty(responseName + ".dimensionLabels");
                double[] samplingRates = (double[]) wd.getProperty(responseName + ".samplingRates");
                String[] samplingRateUnits = (String[]) wd.getProperty(responseName + ".samplingRateUnits");
                String uti = (String) wd.getProperty(responseName + ".uti");

                //TODO: deviceParameters go on the Epoch
                //dimensionLabels, 
                //units
                //samplingRates
                //samplingRateUnits should all go somewhere, right?
                //Measurement r = e.insertMeasurement(name, Set<String> sourceNames, Set<String> devices, url, uti);
            }
        }
    }
}
