/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import us.physion.ovation.domain.Protocol;

/**
 *
 * @author jackie
 */
public class ProtocolDisplayPanel extends JScrollPane{
    
    JComboBox protocolBox;
    JTextField uriField;
    JTextField functionField;
    JTextField scmUrlField;
    JTextField scmRevisionField;
    JTextArea protocolDocArea;
    JPanel panel;
    
    public JComboBox getComboBox()
    {
        return protocolBox;
    }
    ProtocolDisplayPanel()
    {
        panel = new JPanel();
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setLayout(new GridBagLayout());
        protocolBox = new JComboBox();
        protocolBox.setSelectedItem("");
        uriField = new JTextField();
        uriField.setEditable(false);
        functionField = new JTextField();
        functionField.setEditable(false);
        scmUrlField = new JTextField();
        scmUrlField.setEditable(false);
        scmRevisionField = new JTextField();
        scmRevisionField.setEditable(false);
        protocolDocArea = new JTextArea();
        protocolDocArea.setEditable(false);
        
        protocolDocArea.setPreferredSize(new Dimension(getWidth(), 200));
        
        addLabeledComponent("Protocol Name:", protocolBox, 0, false);
        addLabeledComponent("Protocol URI:", uriField, 1, false);
        addLabeledComponent("Function:", functionField, 2, false);
        addLabeledComponent("Scm URL:", scmUrlField, 3, false);
        addLabeledComponent("Scm Revision:", scmRevisionField, 4, false);
        addLabeledComponent("Protocol Document:", protocolDocArea, 5, true);
        
        this.setViewportView(panel);
    }
    
    final void addLabeledComponent(String label, Component component, int row, boolean bottom)
    {
        JLabel l = new JLabel(label);
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 10, 0);
        c.weightx = 0.0;
        if(bottom)
            c.weighty = 1.0;
        else
            c.weighty = 0.0;
        panel.add(l, c);
        
        c.weightx = 1.0;
        c.gridx = 1;
        c.anchor = GridBagConstraints.NORTHEAST;
        panel.add(component, c);
    }
    
    void setSelectedProtocol(Protocol p)
    {
        String uri = p.getURI().toString();
        if (uri == null || uri.isEmpty()) {
            uriField.setText("N/A");
            uriField.setForeground(Color.GRAY);
        } else {
            uriField.setText(uri);
        }
        String functionName = p.getFunctionName();
        if (functionName == null || functionName.isEmpty()) {
            functionField.setText("N/A");
            functionField.setForeground(Color.GRAY);
        } else {
            functionField.setText(functionName);
        }
        String scmUrl = p.getScmUrl();
        if (scmUrl == null || scmUrl.isEmpty()) {
            scmUrlField.setText("N/A");
            scmUrlField.setForeground(Color.GRAY);
        } else {
            scmUrlField.setText(scmUrl);
        }
        String scmRevision = p.getScmRevision();
        if (scmRevision == null || scmRevision.isEmpty()) {
            scmRevisionField.setText("N/A");
            scmRevisionField.setForeground(Color.GRAY);
        } else {
            scmRevisionField.setText(scmRevision);
        }
        protocolDocArea.setText(p.getProtocolDocument());
    }
    
}
