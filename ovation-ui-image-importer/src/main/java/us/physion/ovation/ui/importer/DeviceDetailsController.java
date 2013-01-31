/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.awt.Component;
import java.util.Map;
import org.openide.WizardDescriptor;
import us.physion.ovation.ui.interfaces.BasicWizardPanel;

/**
 *
 * @author jackie
 */
public class DeviceDetailsController extends BasicWizardPanel {

    String responseName;
    int responseCount;

    public DeviceDetailsController(int epochCount, int responseCount) {
        super();
        if (epochCount <0)
            responseName = "response";
        else
            responseName = "epoch" + epochCount + ".response" + responseCount;
        this.responseCount = responseCount;
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new DeviceDetailsPanel(changeSupport, responseCount);
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        String initialDeviceName = (String) data.getProperty(responseName + ".device.name");
        String initialDeviceManufacturer = (String) data.getProperty(responseName + ".device.manufacturer");
        Map<String, Object> initialDeviceParameters = (Map<String, Object>) data.getProperty(responseName + ".device.parameters");
        DeviceDetailsPanel c = (DeviceDetailsPanel) getComponent();
        if (initialDeviceName != null) {
            c.setDeviceName(initialDeviceName);
        }
        if (initialDeviceManufacturer != null) {
            c.setDeviceManufacturer(initialDeviceManufacturer);
        }
        if (initialDeviceParameters != null) {
            c.setDeviceParams(initialDeviceParameters);
        }
    }

    @Override
    public void storeSettings(WizardDescriptor data) {
        DeviceDetailsPanel c = (DeviceDetailsPanel) getComponent();
        String deviceID = (String) data.getProperty(responseName + ".device.name");
        String deviceName = c.getDeviceName();
        String deviceManufacturer = c.getDeviceManufacturer();

        //find the device in devices, and change its name, manufacturer
        Map<String, Map<String, Object>> devices = (Map<String, Map<String, Object>>) data.getProperty("devices");
        Map<String, Object> deviceInfo = devices.get(deviceID);
        if (deviceInfo != null) {
            deviceInfo.put("name", deviceName);
            deviceInfo.put("manufacturer", deviceManufacturer);
        }
        data.putProperty(responseName + ".device.name", deviceName);
        data.putProperty(responseName + ".device.manufacturer", deviceManufacturer);
        data.putProperty(responseName + ".device.parameters", c.getDeviceParameters());
    }

    @Override
    public boolean isValid() {
        DeviceDetailsPanel c = (DeviceDetailsPanel) getComponent();
        return (c.getDeviceName() != null && !c.getDeviceName().isEmpty()
                && c.getDeviceManufacturer() != null && !c.getDeviceManufacturer().isEmpty());
    }
}
