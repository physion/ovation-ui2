/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.joda.time.DateTime;
import org.openide.WizardDescriptor;
import org.openide.util.lookup.ServiceProvider;
import ovation.*;
import us.physion.ovation.ui.interfaces.EpochGroupInsertable;
import us.physion.ovation.ui.interfaces.IEntityWrapper;


@ServiceProvider(service = EpochGroupInsertable.class)
/**
 *
 * @author huecotanks
 */
public class ImportPrairie extends ImportImage{
 
    public ImportPrairie()
    {
        putValue(NAME, "Import Prairie Data...");
    }
    
    @Override
    public int getPosition() {
        return 105;
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
                files.add(new FileMetadata(file, true));
            } else {
                for (File f : chooser.getSelectedFiles()) {
                    files.add(new FileMetadata(f, true));
                }
            }
        }
    }
    
    @Override
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper iew) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();

        GetImageFilesController c = new GetImageFilesController(files);
        int epochCount = files.size();
        
        panels.add(c);
        for (int i = 0; i < epochCount; i++) {
            panels.add(new EpochDetailsController(i));

            int responseCount = files.get(i).getResponses().size();
            for (int j = 0; j < responseCount; j++) {
                panels.add(new DeviceDetailsController(i, j));
                panels.add(new ResponseDetailsController(i, j));
            }
        }
        return panels;
    }
    
    @Override
    public void wizardFinished(WizardDescriptor wd, ovation.IAuthenticatedDataStoreCoordinator dsc, IEntityWrapper iew) {
    
        EpochGroup eg = ((EpochGroup)iew.getEntity());
        Experiment exp = eg.getExperiment();
        
        Map<String, Map<String, Object>> devices = (Map<String, Map<String, Object>>) wd.getProperty("devices");
        for (String deviceName : devices.keySet()) {
            Map<String, Object> device = devices.get(deviceName);
            //name, manufacturer, properties
            ExternalDevice dev = exp.externalDevice((String) device.get("name"),
                    (String) device.get("manufacturer"));
            Map<String, Object> properties = (Map<String, Object>) device.get("properties");
            for (String key : properties.keySet()) {
                Object val = properties.get(key);
                if (val != null)
                    dev.addProperty(key, val);
            }
        }

        int i =0;
        for (;;)
        {
            String epochName = "epoch" + String.valueOf(i++);
            
            String protocolID = (String)wd.getProperty(epochName + ".protocolID");
            if (protocolID == null)
            {
                break;//no more epochs
            }
            Map<String, Object> protocolParameters = (Map<String, Object>) wd.getProperty(epochName + ".protocolParameters");            
            DateTime start = (DateTime) wd.getProperty(epochName + ".start");
            DateTime end = (DateTime) wd.getProperty(epochName + ".end");
            Map<String, Object> epochProperties = (Map<String, Object>) wd.getProperty(epochName + ".properties");

            Epoch e = eg.insertEpoch(start, end, protocolID, protocolParameters);
            for (String key : epochProperties.keySet())
            {
                Object val = epochProperties.get(key);
                if (val != null)
                    e.addProperty(key, val);
            }

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
                NumericDataType type = (NumericDataType) wd.getProperty(responseName + ".dataType");
                String units = (String) wd.getProperty(responseName + ".units");
                String[] dimensionLabels = (String[]) wd.getProperty(responseName + ".dimensionLabels");
                double[] samplingRates = (double[]) wd.getProperty(responseName + ".samplingRates");
                String[] samplingRateUnits = (String[]) wd.getProperty(responseName + ".samplingRateUnits");
                String uti = (String) wd.getProperty(responseName + ".uti");

                Response r = e.insertURLResponse(exp.externalDevice(deviceName, deviceManufacturer),
                        deviceParameters,
                        url,
                        shape,
                        type,
                        units,
                        dimensionLabels,
                        samplingRates,
                        samplingRateUnits,
                        uti);
            }
        }
    }
}