/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Protocol;
import static us.physion.ovation.ui.browser.BrowserUtilities.cn;
import us.physion.ovation.ui.browser.insertion.ProtocolSelector;
import us.physion.ovation.ui.interfaces.ConnectionListener;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//us.physion.ovation.ui.browser//ProtocolView//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "ProtocolViewTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "leftSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "us.physion.ovation.ui.browser.ProtocolViewTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ProtocolViewAction",
        preferredID = "ProtocolViewTopComponent")
@Messages({
    "CTL_ProtocolViewAction=Protocol View",
    "CTL_ProtocolViewTopComponent=Protocol View",
    "HINT_ProtocolViewTopComponent=This window displays all the existing Protocols in the database"
})
public final class ProtocolViewTopComponent extends TopComponent {

    private DataContext ctx;
    DefaultComboBoxModel model;
    
    protected ConnectionListener cn = new ConnectionListener(new Runnable(){

            @Override
            public void run() {
                resetProtocols(Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext());
            }
            
        });
    
    public ProtocolViewTopComponent()
    {
        this(Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext());
    }
    
    public ProtocolViewTopComponent(DataContext context) {
        super();
        this.ctx = context;
        ConnectionProvider cp = Lookup.getDefault().lookup(ConnectionProvider.class);
        cp.addConnectionListener(cn);
        
        initComponents();
        
        setName(Bundle.CTL_ProtocolViewTopComponent());
        setToolTipText(Bundle.HINT_ProtocolViewTopComponent());
        
        resetProtocols(context);
        JComboBox protocolBox = ((ProtocolDisplayPanel)jScrollPane1).getComboBox();
        protocolBox.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null)
                {
                    return new JLabel();
                }
                return super.getListCellRendererComponent(list, ((Protocol)value).getName(), index, isSelected, cellHasFocus);
            }
        });
        protocolBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                Protocol selected = ((Protocol) ((JComboBox) ae.getSource()).getSelectedItem());
                ((ProtocolDisplayPanel)jScrollPane1).setSelectedProtocol(selected);
            }
        }
);
    }
    
    List<Protocol> protocols;
    
    public 

void resetProtocols()
    {
        resetProtocols(Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext());
    }
    public void resetProtocols(DataContext context)
    {
        if (context == null)
        {
            //protocols = Lists.newArrayList();
            return;
        }
        
         protocols = Lists.newArrayList(context.getProtocols());
         Collections.sort(protocols, new Comparator<Protocol>() {

             @Override
        public int compare(Protocol o1, Protocol o2) {
                 return o1.getName().compareTo(o2.getName());
             }
         });
         
         model = new DefaultComboBoxModel(protocols.toArray());
         ((ProtocolDisplayPanel)jScrollPane1).getComboBox().setModel(model);
         
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new ProtocolDisplayPanel();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    @Override
        public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
        public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
