/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import org.openide.WizardDescriptor;
import us.physion.ovation.ui.interfaces.BasicWizardPanel;

import java.awt.*;
import java.util.Map;

/**
 * @author jackie
 */
public class EpochDetailsController extends BasicWizardPanel
{

    String previousEpochName;
    String epochName;
    int epochNum;

    EpochDetailsController(int num)
    {
        super();
        epochNum = num;
        if (num == -1) {
            epochName = "epoch";
        } else {
            epochName = "epoch" + num;
        }

        if (num == 0) {
            previousEpochName = "epoch";
        } else {
            previousEpochName = "epoch" + (num - 1);
        }
    }

    @Override
    public Component getComponent()
    {
        if (component == null) {
            component = new EpochDetailsPanel(changeSupport, epochNum);
            System.out.println("creating component");
        }
        return component;
    }

    @Override
    public void readSettings(WizardDescriptor data)
    {
        //read protocolID and protocolParameters
        EpochDetailsPanel c = (EpochDetailsPanel) getComponent();
        setProtocolID(data);
        setProtocolParameters(data);
    }

    @Override
    public void storeSettings(WizardDescriptor data)
    {
        EpochDetailsPanel c = (EpochDetailsPanel) getComponent();
        data.putProperty(epochName + ".protocolID", c.getProtocolID());
        data.putProperty(epochName + ".protocolParameters", c.getProtocolParameters());
    }

    @Override
    public boolean isValid()
    {
        EpochDetailsPanel c = (EpochDetailsPanel) component;
        return !(c.getProtocolID() == null || c.getProtocolID().isEmpty());
    }

    private void setProtocolID(WizardDescriptor data)
    {
        String initialProtocolID = (String) data.getProperty(epochName + ".protocolID");
        if (initialProtocolID == null) {
            initialProtocolID = (String) data.getProperty(previousEpochName + ".protocolID");
        }
        if (initialProtocolID != null) {
            EpochDetailsPanel c = (EpochDetailsPanel) getComponent();
            c.setProtocolID(initialProtocolID);
        }
    }

    private void setProtocolParameters(WizardDescriptor data)
    {
        Map<String, Object> initialProtocolParameters = (Map<String, Object>) data.getProperty(
                epochName + ".protocolParameters");
        if (initialProtocolParameters == null) {
            initialProtocolParameters = (Map<String, Object>) data.getProperty(
                    previousEpochName + ".protocolParameters");
        }

        if (initialProtocolParameters != null) {
            EpochDetailsPanel c = (EpochDetailsPanel) getComponent();
            c.setProtocolParameters(initialProtocolParameters);
        }
    }

}
