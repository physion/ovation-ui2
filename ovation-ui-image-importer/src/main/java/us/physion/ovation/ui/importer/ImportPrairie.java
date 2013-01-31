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
        panels.add(c);
        panels.add(new EpochDetailsController(-1));
        panels.add(new DeviceDetailsController(-1, -1));
        //panels.add(new ResponseDetailsController(-1, -1));

        return panels;
    }
    
    @Override
    public void wizardFinished(WizardDescriptor wd, ovation.IAuthenticatedDataStoreCoordinator dsc, IEntityWrapper iew) {

        EpochGroup eg = ((EpochGroup)iew.getEntity());
        Experiment exp = eg.getExperiment();
        ExternalDevice dev = exp.externalDevice((String)wd.getProperty("response.device.name"), (String)wd.getProperty("response.device.manufacturer"));
        Map<String, Object> properties = (Map<String, Object>)wd.getProperty("response.device.properties");
        if (properties != null) {
            for (String key : properties.keySet()) {
                dev.addProperty(key, properties.get(key));
            }
        }
        /*
         * Map<String, Map<String, Object>> devices = (Map<String, Map<String,
         * Object>>) wd.getProperty("devices"); for (String deviceName : devices.keySet()) {
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
        }*/
        

        Map<String, Object> parent = (Map<String, Object>) wd.getProperty("parentEpochGroup");
        Source s = eg.getSource();
        DateTime start = (DateTime) parent.get("start");
        EpochGroup parentEpochGroup = eg.insertEpochGroup(s, (String) parent.get("label"), start, (DateTime) parent.get("end"));
        List<Map<String, Object>> egs = (List<Map<String, Object>>) parent.get("egs");

        List<Map<String, Object>> responses = (List<Map<String, Object>>) wd.getProperty("responses");
        if (!egs.isEmpty()) {
            for (Map<String, Object> epochGroup : egs) {
                double secs = (Double)epochGroup.get("deltaT");
                DateTime end = start.plusSeconds((int)secs);
                EpochGroup child = parentEpochGroup.insertEpochGroup(s, (String) epochGroup.get("label"), start, end);
                insertEpochsAndResponses(dev,
                        epochGroup,
                        child,
                        responses,
                        (String) wd.getProperty("epoch.protocolID"),
                        (Map<String, Object>) wd.getProperty("epoch.protocolParameters"),
                        start);

                start = end;
            }
        } else {
            insertEpochsAndResponses(dev,
                    parent,
                    parentEpochGroup,
                    responses,
                    (String) wd.getProperty("epoch.protocolID"),
                    (Map<String, Object>) wd.getProperty("epoch.protocolParameters"),
                    start);
        }
    }

    private void insertEpochsAndResponses(ExternalDevice dev, 
            Map<String, Object> epochGroup, 
            EpochGroup child, 
            List<Map<String, Object>> responses, 
            String protocolID, 
            Map<String, Object>protocolParameters,
            DateTime start) {
        
        Epoch prev = null;
        for (int j = (Integer) epochGroup.get("responseStart"); j < (Integer) epochGroup.get("responseEnd"); j++) {
            Map<String, Object> response = responses.get(j);
            double deltaT = (Double) response.get("epoch.deltaT");
            DateTime epochEnd = start.plusSeconds((int) deltaT);
            Epoch e = child.insertEpoch(start, epochEnd, protocolID, protocolParameters);
            if (prev != null)
            {
                prev.setNextEpoch(e);
                e.setPreviousEpoch(prev);
            }
            prev = e;
            
            start = epochEnd;
            
            Map<String, Object> deviceParameters = (Map<String, Object>) response.get("device.parameters");
            String url = (String) response.get("url");
            long[] shape = (long[]) response.get("shape");
            NumericDataType type = (NumericDataType) response.get("dataType");
            String units = (String) response.get("units");
            String[] dimensionLabels = (String[]) response.get("dimensionLabels");
            double[] samplingRates = (double[]) response.get("samplingRates");
            String[] samplingRateUnits = (String[]) response.get("samplingRateUnits");
            String uti = (String) response.get("uti");

            Response r = e.insertURLResponse(dev,
                    deviceParameters,
                    url,
                    shape,
                    type,
                    units,
                    dimensionLabels,
                    samplingRates,
                    samplingRateUnits,
                    uti);
            Map<String, Object> properties = (Map<String, Object>) response.get("properties");
            for (String key : properties.keySet()) {
                r.addProperty(key, properties.get(key));
            }
        }
    }
}
