/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MetadataStore;
import loci.formats.in.PrairieReader;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.services.OMEXMLService;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.Timestamp;
import org.joda.time.DateTime;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import us.physion.ovation.exceptions.OvationException;

/**
 *
 * @author huecotanks
 */
public class FileMetadata {

    File file;
    MetadataRetrieve retrieve;
    DateTime start;
    DateTime end;
    Map<String, Object> epochProperties;
    List<Map<String, Object>> instruments;
    List<Map<String, Object>> responses;
    Map<String, Object> parentEpochGroup;
    
    boolean isPrairie;

    FileMetadata(File f)
    {
        this(f, false);
    }
    FileMetadata(File f, boolean isPrairie) {
        file = f;
        this.isPrairie = isPrairie;
        ServiceFactory factory = null;
        OMEXMLService service = null;
        IMetadata meta = null;
        try {
            //LoggerFactory l;
            //LoggerFactory.getLogger(FileMetadata.class);
            //ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
            factory = new ServiceFactory();

            service = factory.getInstance(OMEXMLService.class);
            try {
                meta = service.createOMEXMLMetadata();
            } catch (ServiceException ex) {
                Logger.getLogger(ImportImage.class.getName()).log(Level.SEVERE, null, ex);
                throw new OvationException("Unable to create metadata. " + ex.getMessage());
            }
        } catch (DependencyException ex) {
            Logger.getLogger(ImportImage.class.getName()).log(Level.SEVERE, null, ex);
            throw new OvationException("Unable to create metadata. " + ex.getMessage());
        }
        
        IFormatReader r;
        if (isPrairie) {
            r = new PrairieReader();
        } else {
            r = new ImageReader(); 
            if (r instanceof PrairieReader)
                isPrairie = true;
        }
        r.setMetadataStore((MetadataStore)meta);
        try {
            r.setId(file.getAbsolutePath());
        } catch (FormatException ex) {
            Exceptions.printStackTrace(ex);
            throw new OvationException("Unable to parse file. " + ex.getMessage());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            throw new OvationException("Unable to read file. " + ex.getMessage());
        } catch (OutOfMemoryError ex)
        {
            Exceptions.printStackTrace(ex);
            throw new OvationException("Unable to read file. Out of java heap memory");
        }
        Hashtable original;
        try {
            original = service.getOriginalMetadata(service.createOMEXMLMetadata());
        } catch (ServiceException ex) {
            throw new OvationException("Unable to read original metadata. " + ex.getMessage());
        }
        retrieve = service.asRetrieve((loci.formats.meta.MetadataStore)r.getMetadataStore());

        parseRetrieve(retrieve, original);
    }

    public File getFile() {
        return file;
    }

    public DateTime getStart() {
        if (start != null) {
            return start;
        }

        Date min = null;
        for (int i = 0; i < retrieve.getImageCount(); i++) {
            Date newDate = retrieve.getImageAcquisitionDate(i).asDate();
            if (newDate != null) {
                if (min == null) {
                    min = newDate;
                }
                if (min.after(newDate)) {
                    min = newDate;
                }
            }
        }
        return new DateTime(min);
    }

    public DateTime getEnd(boolean recomputeFromStart) {
        if (end != null && !recomputeFromStart) {
            return end;
        }

        Date max = null;
        int count = -1;
        for (int i = 0; i < retrieve.getImageCount(); i++) {
            Date newDate = retrieve.getImageAcquisitionDate(i).asDate();
            if (newDate != null) {
                if (max == null) {
                    max = newDate;
                    count = i;
                }
                if (!newDate.before(max)) {//if they're equal, use the later image
                    max = newDate;
                    count = i;
                }
            }
        }
        double seconds = 0;
        try{
            for (int i =0; i< retrieve.getPlaneCount(count); i++)
            {
                seconds += retrieve.getPlaneDeltaT(count, i);
            }
        }catch (NullPointerException e){}
        return new DateTime(max).plusSeconds(((int)(seconds)));
    }

    public void setStart(DateTime s) {
        start = s;
    }

    public void setEnd(DateTime s) {
        end = s;
    }

    public Map<String, Object> getEpochProperties() {
        return epochProperties;
    }

    public List<Map<String, Object>> getDevices() {
        return instruments;
    }

    public List<Map<String, Object>> getResponses() {
        return responses;
    }

    private void parseRetrieve(MetadataRetrieve retrieve, Hashtable original) {
        instruments = getInstrumentData();

        epochProperties = new HashMap<String, Object>();
        if (original != null) {
            for (Object key : original.keySet()) {
                epochProperties.put("original." + key, original.get(key));
            }
        }
        addMetadataProperties(epochProperties, retrieve);

        responses = new ArrayList<Map<String, Object>>();

        
        
        int imageNumber = -1;
        try{
            imageNumber = retrieve.getImageCount() -1;
        } catch (NullPointerException e)
        {
            throw new OvationException("No Images located");//?
        }
        
        if (imageNumber > 0)
        {
            throw new OvationException("Multi image import not supported yet");
        }
        if (imageNumber == 0)
        {
            int planeCount = (Integer) catchNullPointer(retrieve, "getPlaneCount", new Class[]{Integer.TYPE}, new Object[]{imageNumber});
           
           if (!isPrairie)// if (planeCount <= 1)
            {
                responses.add(createResponse(imageNumber));
            }else
            {
                parentEpochGroup = new HashMap<String, Object>();
                String name = (String) catchNullPointer(retrieve, "getImageName", new Class[]{Integer.TYPE}, new Object[]{imageNumber});
                if (name == null){
                    name = getFile().getName().split("\\.")[0];
                }
                if (isPrairie){
                    name = name.split("Config")[0].split("\\.")[0];
                }
                put("label", name, parentEpochGroup, true);
               
                int responseCount = 0;
                int tCount = ((PositiveInteger) catchNullPointer(retrieve, "getPixelsSizeT", new Class[]{Integer.TYPE}, new Object[]{imageNumber})).getValue();
                List<Map<String, Object>> egs = new ArrayList();
                for (int i=0; i< tCount; i++)
                {
                    Map<String, Object> eg = new HashMap<String, Object>();
                    put("responseStart", responseCount, eg, true);
                    int zCount = ((PositiveInteger) catchNullPointer(retrieve, "getPixelsSizeZ", new Class[]{Integer.TYPE}, new Object[]{imageNumber})).getValue();
           
                    double deltaTForEpochGroup = 0;
                    for (int j=0; j<zCount; j++)
                    {
                        Map<String, Object> responseStruct = createResponse(imageNumber);
                        if (isPrairie)
                        {
                            String url = generateURL(i, retrieve.getChannelName(imageNumber, retrieve.getPlaneTheC(imageNumber, j).getValue()), j);
                            put("url", url, responseStruct);        
                        }
                        put("epoch.deltaT", retrieve.getPlaneDeltaT(imageNumber, j), responseStruct);

                        Map<String, Object> properties = new HashMap();
                        put("exposureTime", catchNullPointer(retrieve, "getPlaneExposureTime", new Class[]{Integer.TYPE, Integer.TYPE}, new Object[]{imageNumber, j}), properties);
                        put("positionX", retrieve.getPlanePositionX(imageNumber, j), properties);
                        put("positionY", retrieve.getPlanePositionY(imageNumber, j), properties);
                        put("positionZ", retrieve.getPlanePositionZ(imageNumber, j), properties);
                        put("channel", retrieve.getPlaneTheC(imageNumber, j), properties);
                        put("t-group", retrieve.getPlaneTheT(imageNumber, j), properties);
                        put("z-group", retrieve.getPlaneTheZ(imageNumber, j), properties);

                        responseStruct.put("properties", properties);

                        deltaTForEpochGroup += retrieve.getPlaneDeltaT(imageNumber, j);
                        responses.add(responseStruct);
                    }
                    put("deltaT", deltaTForEpochGroup, eg, true);
                    put("label", "Cycle_"+ i, eg, true);
                    responseCount += zCount;
                    put("responseEnd", responseCount, eg, true);

                    if (tCount == 1)
                    {
                        eg.remove("label");
                        parentEpochGroup.putAll(eg);
                    }else{
                        egs.add(eg);
                    }
                }
                put("egs", egs, parentEpochGroup, true);

            }
             
            //TODO: plates -- start and end time information?
            
        }
    }
    
    private Map<String, Object> createResponse(int imageNumber)
    {
        Map<String, Object> responseStruct = new HashMap<String, Object>();
        put("name", "response" + imageNumber, responseStruct, true);

        String ref = (String) catchNullPointer(retrieve, "getImageInstrumentRef", new Class[]{Integer.TYPE}, new Object[]{imageNumber});
        boolean found = false;
        if (ref != null) {
            for (Map<String, Object> device : instruments) {
                if (device.get("ID").equals(ref)) {
                    put("device.name", device.get("ID"), responseStruct, true);
                    put("device.manufacturer", device.get("manufacturer"), responseStruct, true);
                    found = true;
                }
            }
        }
        if (!found && !instruments.isEmpty()) {
            String name = "";
            String manufacturer = "";
            for (Map<String, Object> device : instruments) {
                name += "-" + device.get("ID");
                manufacturer += "-" + device.get("manufacturer");
            }
            put("device.name", name.substring(1), responseStruct, true);
            put("device.manufacturer", manufacturer.substring(1), responseStruct, true);
        }
        put("device.parameters", getDeviceParameters(imageNumber), responseStruct, true);

        if (!isPrairie) {
            try {
                put("url", getFile().toURI().toURL().toExternalForm(), responseStruct, true);
            } catch (MalformedURLException ex) {
                throw new OvationException("Unable to get url for image file. " + ex.getMessage());
            }
        }
        addMultidimensionalFields(retrieve, responseStruct, imageNumber);

        //data type doesn't actually matter, since this is not a NumericData object
        ByteOrder b;
        if (retrieve.getPixelsBinDataBigEndian(imageNumber, 0)) {
            b = ByteOrder.BIG_ENDIAN;
        } else {
            b = ByteOrder.LITTLE_ENDIAN;
        }
        put("dataType", "tiff", responseStruct, true);
        put("units", "pixels", responseStruct, true);
        put("uti", "public.tiff", responseStruct, true);//TODO: fix - get file type?
        return responseStruct;
    }

    private Map<String, Object> getDeviceParameters(int imageNum) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        put("imageName", retrieve.getImageName(imageNum), parameters);
        put("imageDescription", retrieve.getImageDescription(imageNum), parameters);
        put("imageID", retrieve.getImageID(imageNum), parameters);

        try {
            put("imagingEnvironment.airPressure", retrieve.getImagingEnvironmentAirPressure(imageNum), parameters);
        } catch (NullPointerException e) {
        }
        try {
            put("imagingEnvironment.CO2Percent", retrieve.getImagingEnvironmentCO2Percent(imageNum), parameters);
        } catch (NullPointerException e) {
        }
        try {
            put("imagingEnvironment.humidity", retrieve.getImagingEnvironmentHumidity(imageNum), parameters);
        } catch (NullPointerException e) {
        }
        try {
            put("imagingEnvironment.temperature", retrieve.getImagingEnvironmentTemperature(imageNum), parameters);
        } catch (NullPointerException e) {
        }
        try {
            put("stage." + retrieve.getStageLabelName(imageNum) + ".x", retrieve.getStageLabelX(imageNum), parameters);
            put("stage." + retrieve.getStageLabelName(imageNum) + ".y", retrieve.getStageLabelY(imageNum), parameters);
            put("stage." + retrieve.getStageLabelName(imageNum) + ".z", retrieve.getStageLabelZ(imageNum), parameters);
        } catch (NullPointerException e) {
        }

        try {
            String obj = "objective";
            put(obj + ".settingsID", retrieve.getObjectiveSettingsID(imageNum), parameters);
            put(obj + ".settingsMedium", retrieve.getObjectiveSettingsMedium(imageNum), parameters);
            put(obj + ".settingsRefractiveIndex", retrieve.getObjectiveSettingsRefractiveIndex(imageNum), parameters);
            put(obj + ".settingsCorrectionCollar", retrieve.getObjectiveSettingsCorrectionCollar(imageNum), parameters);
        } catch (NullPointerException e) {
        }

        int channelCount = 0;
        try {
            channelCount = retrieve.getChannelCount(imageNum);
        } catch (IndexOutOfBoundsException e) {
        }

        for (int i = 0; i < channelCount; i++) {
            String channelID = "channel_" + retrieve.getChannelID(imageNum, i);
            put(channelID + ".acqisitionMode", retrieve.getChannelAcquisitionMode(imageNum, i), parameters);
            put(channelID + ".color", retrieve.getChannelColor(imageNum, i), parameters);
            put(channelID + ".emissionWavelength", retrieve.getChannelEmissionWavelength(imageNum, i), parameters);
            put(channelID + ".excitationWavelength", retrieve.getChannelExcitationWavelength(imageNum, i), parameters);
            put(channelID + ".illuminationType", retrieve.getChannelIlluminationType(imageNum, i), parameters);
            put(channelID + ".fluor", retrieve.getChannelFluor(imageNum, i), parameters);
            put(channelID + ".ID", retrieve.getChannelID(imageNum, i), parameters);

            try {
                put(channelID + ".lightSourceSettingsID", retrieve.getChannelLightSourceSettingsID(imageNum, i), parameters);
                put(channelID + ".lightSourceSettingsAttenuation", retrieve.getChannelLightSourceSettingsAttenuation(imageNum, i), parameters);
                put(channelID + ".lightSourceSettingsWavelength", retrieve.getChannelLightSourceSettingsWavelength(imageNum, i), parameters);
            } catch (NullPointerException e) {
            }

            put(channelID + ".name", retrieve.getChannelName(imageNum, i), parameters);
            put(channelID + ".NDFilter", retrieve.getChannelNDFilter(imageNum, i), parameters);
            put(channelID + ".pinholeSize", retrieve.getChannelPinholeSize(imageNum, i), parameters);
            put(channelID + ".pockelCellSetting", retrieve.getChannelPockelCellSetting(imageNum, i), parameters);
            put(channelID + ".samplesPerPixel", retrieve.getChannelSamplesPerPixel(imageNum, i), parameters);

            try {
                String channelDetector = channelID + ".detector";
                put(channelDetector + ".settingsBinning", retrieve.getDetectorSettingsBinning(imageNum, i), parameters);//TODO: do they really mean channel number?
                put(channelDetector + ".settingsGain", retrieve.getDetectorSettingsGain(imageNum, i), parameters);//TODO: do they really mean channel number?
                put(channelDetector + ".settingsID", retrieve.getDetectorSettingsID(imageNum, i), parameters);//TODO: do they really mean channel number?
                put(channelDetector + ".settingsOffset", retrieve.getDetectorSettingsOffset(imageNum, i), parameters);//TODO: do they really mean channel number?
                put(channelDetector + ".settingsReadOutRate", retrieve.getDetectorSettingsReadOutRate(imageNum, i), parameters);//TODO: do they really mean channel number?
                put(channelDetector + ".settingsVoltage", retrieve.getDetectorSettingsVoltage(imageNum, i), parameters);//TODO: do they really mean channel number?
            } catch (NullPointerException e) {
            }
        }

        int mmCount = 0;
        try {
            mmCount = retrieve.getMicrobeamManipulationCount(imageNum);
        } catch (IndexOutOfBoundsException e) {
        }
        for (int i = 0; i < mmCount; i++) {
            put("microbeamManipulation" + i + ".ID", retrieve.getMicrobeamManipulationID(imageNum, i), parameters);
            put("microbeamManipulation" + i + ".description", retrieve.getMicrobeamManipulationDescription(imageNum, i), parameters);
            put("microbeamManipulation" + i + ".type", retrieve.getMicrobeamManipulationType(imageNum, i), parameters);

            //for each light source ?
            //put("microbeamManipulation" + i+"."+ "lightSourceSettingsAttenuation", retrieve.getMicrobeamManipulationLightSourceSettingsAttenuation(imageNumber, i), parameters);
            //put("microbeamManipulation" + i+"."+ "lightSourceSettingsID", retrieve.getMicrobeamManipulationLightSourceSettingsID(imageNumber, i), parameters);
            //put("microbeamManipulation" + i+"."+ "lightSourceSettingnWavelength", retrieve.getMicrobeamManipulationLightSourceSettingsWavelength(imageNumber, i), parameters);

        }

        int lsCount = 0;
        try {
            lsCount = retrieve.getLightSourceCount(imageNum);
        } catch (IndexOutOfBoundsException e) {
        }
        for (int k = 0; k < lsCount; k++) {
            String type = retrieve.getLightSourceType(imageNum, k).toLowerCase();

            if (type.equals("arc")) {
                String arcName = "arc_" + retrieve.getArcID(imageNum, k);
                put(arcName + ".power", retrieve.getArcPower(imageNum, k), parameters);
            }
            if (type.equals("filament")) {
                String filamentName = "filament_" + retrieve.getFilamentID(imageNum, k);
                put(filamentName + ".power", retrieve.getFilamentPower(imageNum, k), parameters);
            }

            if (type.equals("laser")) {
                String laserName = "laser_" + retrieve.getLaserID(imageNum, k);
                put(laserName + ".frequencyMultiplication", retrieve.getLaserFrequencyMultiplication(imageNum, k), parameters);
                put(laserName + ".medium", retrieve.getLaserLaserMedium(imageNum, k), parameters);
                try {
                    put(laserName + ".pockelCell", retrieve.getLaserPockelCell(imageNum, k), parameters);
                } catch (NullPointerException e) {
                }
                put(laserName + ".power", retrieve.getLaserPower(imageNum, k), parameters);
                try {
                    put(laserName + ".pump", retrieve.getLaserPump(imageNum, k), parameters);
                } catch (NullPointerException e) {
                }
                put(laserName + ".repetitionRate", retrieve.getLaserRepetitionRate(imageNum, k), parameters);
                put(laserName + ".tuneable", retrieve.getLaserTuneable(imageNum, k), parameters);
                put(laserName + ".wavelength", retrieve.getLaserWavelength(imageNum, k), parameters);
            }

            if (type.equals("lightEmittingDiodeName")) {
                String lightEmittingDiodeName = "lightEmittingDiodeName_" + retrieve.getLightEmittingDiodeID(imageNum, k);
                put(lightEmittingDiodeName + ".power", retrieve.getLightEmittingDiodePower(imageNum, k), parameters);
            }
        }

        int objCount = 0;
        try {
            objCount = retrieve.getObjectiveCount(imageNum);
        } catch (IndexOutOfBoundsException e) {
        }
        for (int k = 0; k < objCount; k++) {
            String objName = "objective_" + retrieve.getObjectiveID(imageNum, k);
            put(objName + ".calibratedMagnification", retrieve.getObjectiveCalibratedMagnification(imageNum, k), parameters);
            put(objName + ".correction", retrieve.getObjectiveCorrection(imageNum, k), parameters);
            put(objName + ".immersion", retrieve.getObjectiveImmersion(imageNum, k), parameters);
            put(objName + ".iris", retrieve.getObjectiveIris(imageNum, k), parameters);
            put(objName + ".lensNA", retrieve.getObjectiveLensNA(imageNum, k), parameters);
            put(objName + ".nominalMagnification", retrieve.getObjectiveNominalMagnification(imageNum, k), parameters);
            put(objName + ".workingDistance", retrieve.getObjectiveWorkingDistance(imageNum, k), parameters);
        }

        int filterCount = 0;
        try {
            filterCount = retrieve.getFilterCount(imageNum);
        } catch (IndexOutOfBoundsException e) {
        }
        for (int k = 0; k < filterCount; k++) {
            String filterName = "filter_" + retrieve.getFilterID(imageNum, k);
            put(filterName + ".transmittanceRangeCutIn", retrieve.getTransmittanceRangeCutIn(imageNum, k), parameters);
            put(filterName + ".transmittanceRangeCutInTolerance", retrieve.getTransmittanceRangeCutInTolerance(imageNum, k), parameters);
            put(filterName + ".transmittanceRangeCutOut", retrieve.getTransmittanceRangeCutOut(imageNum, k), parameters);
            put(filterName + ".transmittanceRangeCutOutTolerance", retrieve.getTransmittanceRangeCutOutTolerance(imageNum, k), parameters);
            put(filterName + ".transmittanceRangeTransmittance", retrieve.getTransmittanceRangeTransmittance(imageNum, k), parameters);

            put(filterName + ".wheel", retrieve.getFilterFilterWheel(imageNum, k), parameters);
        }

        int detectorCount = 0;
        try {
            detectorCount = retrieve.getDetectorCount(imageNum);
        } catch (IndexOutOfBoundsException e) {
        }
        for (int k = 0; k < detectorCount; k++) {
            String filterName = "detector_" + retrieve.getDetectorID(imageNum, k);
            put(filterName + ".amplificationGain", retrieve.getDetectorAmplificationGain(imageNum, k), parameters);
            put(filterName + ".gain", retrieve.getDetectorGain(imageNum, k), parameters);
            put(filterName + ".offset", retrieve.getDetectorOffset(imageNum, k), parameters);
            put(filterName + ".voltage", retrieve.getDetectorVoltage(imageNum, k), parameters);
            put(filterName + ".zoom", retrieve.getDetectorZoom(imageNum, k), parameters);
        }

        return parameters;
    }

    protected void addMetadataProperties(Map<String, Object> properties, MetadataRetrieve retrieve) {
        int dsCount = 0;
        try {
            dsCount = retrieve.getDatasetCount();
        } catch (IndexOutOfBoundsException e) {
        }
        for (int i = 0; i < dsCount; i++) {
            put("dataset" + i + ".description", retrieve.getDatasetDescription(i), properties);
            put("dataset" + i + ".ID", retrieve.getDatasetID(i), properties);
            put("dataset" + i + ".name", retrieve.getDatasetName(i), properties);
            put("dataset" + i + ".imageRef", retrieve.getDatasetImageRefCount(i), properties);
        }

        int experimenterCount = 0;
        try {
            experimenterCount = retrieve.getExperimenterCount();
        } catch (IndexOutOfBoundsException e) {
        }
        for (int i = 0; i < experimenterCount; i++) {
            String name = retrieve.getExperimenterFirstName(i) + retrieve.getExperimenterLastName(i);
            put("experimenter" + i + ".username", retrieve.getExperimenterUserName(i), properties);
            put("experimenter" + i + ".ID", retrieve.getExperimenterID(i), properties);
            put("experimenter" + i + ".name", name, properties);
            put("experimenter" + i + ".email", retrieve.getExperimenterEmail(i), properties);
            put("experimenter" + i + ".institution", retrieve.getExperimenterInstitution(i), properties);
        }
        
        try {
            this.getImageProperties(0);
        } catch (IndexOutOfBoundsException e) {
        }
    }

    private List<Map<String, Object>> getInstrumentData() {
        List<Map<String, Object>> instrumentStructs = new ArrayList<Map<String, Object>>();

        int instrumentCount = 0;
        try {
            instrumentCount = retrieve.getInstrumentCount();
        } catch (IndexOutOfBoundsException e) {
        }
        for (int j = 0; j < instrumentCount; j++) {
            Map<String, Object> instrumentProperties = new HashMap<String, Object>();
            Map<String, Object> instrumentStruct = new HashMap<String, Object>();
            put("ID", retrieve.getInstrumentID(j), instrumentStruct);

            if (isMicroscope(retrieve, j)) {
                put("microscopeLotNumber", retrieve.getMicroscopeLotNumber(j), instrumentProperties);
                put("microscopeManufacturer", retrieve.getMicroscopeManufacturer(j), instrumentProperties);
                setManufacturer(retrieve.getMicroscopeManufacturer(j), instrumentStruct);
                put("microscopeModel", retrieve.getMicroscopeModel(j), instrumentProperties);
                put("microscopeSerialNumber", retrieve.getMicroscopeSerialNumber(j), instrumentProperties);
                put("microscopeType", retrieve.getMicroscopeType(j), instrumentProperties);
            }

            int lsCount = 0;
            try {
                lsCount = retrieve.getLightSourceCount(j);
            } catch (IndexOutOfBoundsException e) {
            }
            for (int k = 0; k < lsCount; k++) {

                String type = retrieve.getLightSourceType(j, k).toLowerCase();
                put("lightSource" + k + ".type", type, instrumentProperties);

                if (type.equals("arc")) {
                    String arcName = "arc_" + retrieve.getArcID(j, k);
                    put(arcName + ".ID", retrieve.getArcID(j, k), instrumentProperties);
                    put(arcName + ".lotNumber", retrieve.getArcLotNumber(j, k), instrumentProperties);
                    put(arcName + ".manufacturer", retrieve.getArcManufacturer(j, k), instrumentProperties);
                    setManufacturer(retrieve.getArcManufacturer(j, k), instrumentStruct);

                    put(arcName + ".model", retrieve.getArcModel(j, k), instrumentProperties);
                    put(arcName + ".serialNumber", retrieve.getArcSerialNumber(j, k), instrumentProperties);
                    put(arcName + ".type", retrieve.getArcType(j, k), instrumentProperties);
                } else if (type.equals("filament")) {
                    String filamentName = "filament_" + retrieve.getFilamentID(j, k);
                    put(filamentName + ".ID", retrieve.getFilamentID(j, k), instrumentProperties);
                    put(filamentName + ".lotNumber", retrieve.getFilamentLotNumber(j, k), instrumentProperties);
                    put(filamentName + ".manufacturer", retrieve.getFilamentManufacturer(j, k), instrumentProperties);
                    setManufacturer(retrieve.getFilamentManufacturer(j, k), instrumentStruct);
                    put(filamentName + ".model", retrieve.getFilamentModel(j, k), instrumentProperties);
                    put(filamentName + ".serialNumber", retrieve.getFilamentSerialNumber(j, k), instrumentProperties);
                    put(filamentName + ".type", retrieve.getFilamentType(j, k), instrumentProperties);
                } else if (type.equals("laser")) {
                    String laserName = "laser_" + retrieve.getLaserID(j, k);
                    put(laserName + ".ID", retrieve.getLaserID(j, k), instrumentProperties);
                    put(laserName + ".medium", retrieve.getLaserLaserMedium(j, k), instrumentProperties);
                    put(laserName + ".lotNumber", retrieve.getLaserLotNumber(j, k), instrumentProperties);
                    put(laserName + ".manufacturer", retrieve.getLaserManufacturer(j, k), instrumentProperties);
                    setManufacturer(retrieve.getLaserManufacturer(j, k), instrumentStruct);
                    put(laserName + ".model", retrieve.getLaserModel(j, k), instrumentProperties);
                    put(laserName + ".serialNumber", retrieve.getLaserSerialNumber(j, k), instrumentProperties);
                    put(laserName + ".tuneable", retrieve.getLaserTuneable(j, k), instrumentProperties);
                    put(laserName + ".type", retrieve.getLaserType(j, k), instrumentProperties);
                } else if (type.equals("lightEmittingDiode")) {
                    String lightEmittingDiodeName = "lightEmittingDiodeName_" + retrieve.getLightEmittingDiodeID(j, k);
                    put(lightEmittingDiodeName + ".ID", retrieve.getLightEmittingDiodeID(j, k), instrumentProperties);
                    put(lightEmittingDiodeName + ".lotNumber", retrieve.getLightEmittingDiodeLotNumber(j, k), instrumentProperties);
                    put(lightEmittingDiodeName + ".manufacturer", retrieve.getLightEmittingDiodeManufacturer(j, k), instrumentProperties);
                    setManufacturer(retrieve.getLightEmittingDiodeManufacturer(j, k), instrumentStruct);
                    put(lightEmittingDiodeName + ".model", retrieve.getLightEmittingDiodeModel(j, k), instrumentProperties);
                    put(lightEmittingDiodeName + ".serialNumber", retrieve.getLightEmittingDiodeSerialNumber(j, k), instrumentProperties);
                }
            }

            int count = 0;
            try {
                count = retrieve.getDichroicCount(j);
            } catch (IndexOutOfBoundsException e) {
            }
            for (int k = 0; k < count; k++) {
                String filterName = "dichroic_" + retrieve.getDichroicID(j, k);
                put(filterName + ".ID", retrieve.getDichroicID(j, k), instrumentProperties);
                put(filterName + ".lotNumber", retrieve.getDichroicLotNumber(j, k), instrumentProperties);
                put(filterName + ".manufacturer", retrieve.getDichroicManufacturer(j, k), instrumentProperties);
                setManufacturer(retrieve.getDichroicManufacturer(j, k), instrumentStruct);
                put(filterName + ".model", retrieve.getDichroicModel(j, k), instrumentProperties);
                put(filterName + ".serialNumber", retrieve.getDichroicSerialNumber(j, k), instrumentProperties);
            }

            count = 0;
            try {
                count = retrieve.getObjectiveCount(j);
            } catch (IndexOutOfBoundsException e) {
            }
            for (int k = 0; k < count; k++) {
                String objName = "objective_" + retrieve.getObjectiveID(j, k);
                put(objName + ".ID", retrieve.getObjectiveID(j, k), instrumentProperties);
                put(objName + ".lotNumber", retrieve.getObjectiveLotNumber(j, k), instrumentProperties);
                put(objName + ".manufacturer", retrieve.getObjectiveManufacturer(j, k), instrumentProperties);
                setManufacturer(retrieve.getObjectiveManufacturer(j, k), instrumentStruct);
                put(objName + ".model", retrieve.getObjectiveModel(j, k), instrumentProperties);
                put(objName + ".serialNumber", retrieve.getObjectiveSerialNumber(j, k), instrumentProperties);
            }

            count = 0;
            try {
                count = retrieve.getFilterCount(j);
            } catch (IndexOutOfBoundsException e) {
            }
            for (int k = 0; k < count; k++) {
                String filterName = "filter_" + retrieve.getFilterID(j, k);
                put(filterName + ".wheel", retrieve.getFilterFilterWheel(j, k), instrumentProperties);
                put(filterName + ".ID", retrieve.getFilterID(j, k), instrumentProperties);
                put(filterName + ".lotNumber", retrieve.getFilterLotNumber(j, k), instrumentProperties);
                put(filterName + ".model", retrieve.getFilterModel(j, k), instrumentProperties);
                put(filterName + ".serialNumber", retrieve.getFilterSerialNumber(j, k), instrumentProperties);
                put(filterName + ".manufacturer", retrieve.getFilterManufacturer(j, k), instrumentProperties);
                setManufacturer(retrieve.getFilterManufacturer(j, k), instrumentStruct);
                put(filterName + ".type", retrieve.getFilterType(j, k), instrumentProperties);
            }

            count = 0;
            try {
                count = retrieve.getDetectorCount(j);
            } catch (IndexOutOfBoundsException e) {
            }
            for (int k = 0; k < count; k++) {
                String filterName = "detector_" + retrieve.getDetectorID(j, k);
                put(filterName + ".ID", retrieve.getDetectorID(j, k), instrumentProperties);
                put(filterName + ".lotNumber", retrieve.getDetectorLotNumber(j, k), instrumentProperties);
                put(filterName + ".model", retrieve.getDetectorModel(j, k), instrumentProperties);
                put(filterName + ".manufacturer", retrieve.getDetectorManufacturer(j, k), instrumentProperties);
                setManufacturer(retrieve.getDetectorManufacturer(j, k), instrumentStruct);
                put(filterName + ".serialNumber", retrieve.getDetectorSerialNumber(j, k), instrumentProperties);
                put(filterName + ".type", retrieve.getDetectorType(j, k), instrumentProperties);
            }

            put("properties", instrumentProperties, instrumentStruct, true);
            instrumentStructs.add(instrumentStruct);
        }
        return instrumentStructs;
    }

    public Object catchNullPointer(MetadataRetrieve retrieve, String methodName, Class[] argTypes, Object[] args) {
        try {
            Method m = retrieve.getClass().getMethod(methodName, argTypes);
            return m.invoke(retrieve, args);
        } catch (IllegalAccessException e) {
        } catch (IllegalArgumentException e) {
        } catch (InvocationTargetException e) {
        } catch (NullPointerException e) {
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        }
        return null;
    }

    private void put(String name, Object value, Map<String, Object> map) {
        put(name, value, map, false);
    }

    private void put(String name, Object value, Map<String, Object> map, boolean putDirectly) {
        if (value != null) {
            if (putDirectly || value instanceof String) {
                map.put(name, value);
                return;
            }

            //cast to string then to int
            //TODO: handle each OME enum, PositiveInteger, etc separately
            String val = value.toString();
            try {
                int v = Integer.valueOf(val);
                map.put(name, v);
                return;
            } catch (NumberFormatException e) {
            }

            try {
                long v = Long.valueOf(val);
                map.put(name, v);
                return;
            } catch (NumberFormatException e) {
            }

            try {
                double v = Double.valueOf(val);
                map.put(name, v);
                return;
            } catch (NumberFormatException e) {
            }

            map.put(name, val);
        }
    }

    protected Map<String, Object> getImageProperties(int imageNumber) {
        Map<String, Object> properties = new HashMap<String, Object>();
        put("imageName", retrieve.getImageName(imageNumber), properties);
        put("imageDescription", retrieve.getImageDescription(imageNumber), properties);
        put("imageID", retrieve.getImageID(imageNumber), properties);

        return properties;
    }

    private boolean isMicroscope(MetadataRetrieve retrieve, int j) {
        try {
            retrieve.getMicroscopeLotNumber(j);
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    private void setManufacturer(String manufacturer, Map<String, Object> instrumentStruct) {
        if (instrumentStruct.get("manufacturer") == null) {
            put("manufacturer", manufacturer, instrumentStruct);
        }
    }

    private void addMultidimensionalFields(MetadataRetrieve retrieve, Map<String, Object> responseStruct, int j) {
        int shapeCount = 0;
        long shapeX = -1, shapeY = -1, shapeZ = -1, shapeC = -1, shapeT = -1;
        double rateX = -1, rateY = -1, rateZ = -1, rateC = -1, rateT = -1;
        try {
            shapeX = retrieve.getPixelsSizeX(j).getValue().longValue();
            rateX = retrieve.getPixelsPhysicalSizeX(j).getValue() / shapeX;
        } catch (NullPointerException e) {
        } finally {
            if (shapeX > 0) {
                shapeCount++;
            }
        }
        try {
            shapeY = retrieve.getPixelsSizeY(j).getValue().longValue();
            rateY = retrieve.getPixelsPhysicalSizeY(j).getValue() / shapeY;
        } catch (NullPointerException e) {
        } finally {
            if (shapeY > 0) {
                shapeCount++;
            }
        }
        try {
            shapeZ = retrieve.getPixelsSizeZ(j).getValue().longValue();
            rateZ = retrieve.getPixelsPhysicalSizeZ(j).getValue() / shapeZ;
        } catch (NullPointerException e) {
        } finally {
            if (shapeZ > 0) {
                shapeCount++;
            }
        }
        try {
            shapeC = retrieve.getPixelsSizeC(j).getValue().longValue();
            rateC = 1;
        } catch (NullPointerException e) {
        } finally {
            if (shapeC > 0) {
                shapeCount++;
            }
        }
        try {
            shapeT = retrieve.getPixelsSizeT(j).getValue().longValue();
            try {
                double timeIncrement = retrieve.getPixelsTimeIncrement(j).doubleValue();
                rateT = timeIncrement == 0 ? 0 : (1 / timeIncrement);
            } catch (NullPointerException e) {
            }
        } catch (NullPointerException e) {
        } finally {
            if (shapeT > 0) {
                shapeCount++;
            }
        }

        long[] shape = new long[shapeCount];
        double[] samplingRates = new double[shapeCount];
        String[] samplingRateUnits = new String[shapeCount];
        String[] dimensionLabels = new String[shapeCount];
        shapeCount = 0;
        if (shapeX > 0) {
            shape[shapeCount] = shapeX;
            samplingRates[shapeCount] = rateX;
            samplingRateUnits[shapeCount] = "microns";
            dimensionLabels[shapeCount++] = "X";
        }
        if (shapeY > 0) {
            shape[shapeCount] = shapeY;
            samplingRates[shapeCount] = rateY;
            samplingRateUnits[shapeCount] = "microns";
            dimensionLabels[shapeCount++] = "Y";
        }
        if (shapeZ > 0) {
            shape[shapeCount] = shapeZ;
            samplingRates[shapeCount] = rateZ;
            samplingRateUnits[shapeCount] = "frames";
            dimensionLabels[shapeCount++] = "Z";
        }
        if (shapeC > 0) {
            shape[shapeCount] = shapeC;
            samplingRates[shapeCount] = rateC;
            samplingRateUnits[shapeCount] = "channels";
            dimensionLabels[shapeCount++] = "Channels";
        }
        if (shapeT > 0) {
            shape[shapeCount] = shapeT;
            samplingRates[shapeCount] = rateT;
            samplingRateUnits[shapeCount] = "Hz";//I'm not sure about Hz
            dimensionLabels[shapeCount++] = "Time";
        }

        put("shape", shape, responseStruct, true);
        put("samplingRates", samplingRates, responseStruct, true);
        put("samplingRateUnits", samplingRateUnits, responseStruct, true);//TODO make sure the UI handles dimension errors gracefully
        put("dimensionLabels", dimensionLabels, responseStruct, true);

    }

    private String generateURL(int cycleNumber, String channelName, int zNumber) {
        String filename = getFile().getAbsolutePath().split("Config.")[0].split("\\.")[0];
        filename += "_Cycle" + convertTo5Digit(cycleNumber) + "_CurrentSettings_" + channelName + "_" + convertTo6Digit(zNumber) + ".tif";
        try {
            return new File(filename).toURI().toURL().toExternalForm();
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            throw new OvationException("Unable to create file. Bad path");
        }
    }

    private String convertTo5Digit(int number) {
        number++;
        int digits = number/10;
        String s = "";
        for (int i=0; i < (4 - digits); i++)
        {
            s +="0";
        }
        s += String.valueOf(number);
        return s;
    }
    private String convertTo6Digit(int number) {
        number++;
        int digits = number/10;
        String s = "";
        for (int i=0; i < (5 - digits); i++)
        {
            s +="0";
        }
        s += String.valueOf(number);
        return s;
    }

    Map<String, Object> getParentEpochGroup() {
        return parentEpochGroup;
    }

    boolean containsSingleEpoch() {
        return parentEpochGroup == null;
    }
}
