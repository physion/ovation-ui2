/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.DataContext;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = NotesTopComponent.class)
@ConvertAsProperties(dtd = "-//us.physion.ovation.ui.detailviews//Notes//EN",
autostore = false)
@TopComponent.Description(preferredID = "NotesTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "us.physion.ovation.ui.detailviews.NotesTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_NotesAction",
preferredID = "NotesTopComponent")
@Messages({
    "CTL_NotesAction=Notes",
    "CTL_NotesTopComponent=My Notes",
    "HINT_NotesTopComponent=My Notes View"
})
public final class NotesTopComponent extends TopComponent {

    private LookupListener listener = new LookupListener() {

        @Override
        public void resultChanged(LookupEvent le) {

            //TODO: we should have some other Interface for things that can update the tags view
            //then we could get rid of the Library dependancy on the Explorer API
            if (TopComponent.getRegistry().getActivated() instanceof ExplorerManager.Provider){
                update();
            }
        }

    };
    protected Lookup.Result<IEntityWrapper> global;
    protected Collection<? extends IEntityWrapper> entities;
    NotesTableModel notesModel;
    NotesTableRenderer renderer;
    public NotesTopComponent() {
        notesModel = new NotesTableModel();
        initComponents();
        setName(Bundle.CTL_NotesTopComponent());
        setToolTipText(Bundle.HINT_NotesTopComponent());
        renderer = new NotesTableRenderer(notesModel, jTable1.getSelectionBackground());
        jTable1.setDefaultRenderer(Object.class, renderer);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable1.setRowSelectionAllowed(true);
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent lse) {
                int row = jTable1.getSelectedRow();
                if (row >=0 )
                {
                    NoteValue v = (NoteValue)notesModel.getValueAt(row, 0);
                    jTextArea1.setText(v.text);
                }
            }

        });
        jTable1.setGridColor(Color.LIGHT_GRAY);
        global = Utilities.actionsGlobalContext().lookupResult(IEntityWrapper.class);
        global.addLookupListener(listener);
    }

    /*public void addNote(IAnnotation ann)
    {
        notesModel.addNote(ann);
    }*/
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new JTable();

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NotesTopComponent.class, "NotesTopComponent.jLabel1.text")); // NOI18N
        jLabel1.setAlignmentX(0.5F);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextArea1FocusLost(evt);
            }
        });
        jTextArea1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextArea1KeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(jTextArea1);

        jSplitPane1.setRightComponent(jScrollPane3);

        jTable1.setModel(notesModel);
        jScrollPane1.setViewportView(jTable1);

        jScrollPane2.setViewportView(jScrollPane1);

        jSplitPane1.setLeftComponent(jScrollPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextArea1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea1KeyReleased
        
    }//GEN-LAST:event_jTextArea1KeyReleased

    private void jTextArea1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea1FocusLost
        int row = jTable1.getSelectedRow();
        if (row >= 0 )
        {
            notesModel.setValueAt(jTextArea1.getText(), row, 0);
        }
    }//GEN-LAST:event_jTextArea1FocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
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
    
    public void update()
    {
        Collection<? extends IEntityWrapper> entities = global.allInstances();
        update(Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext(), entities);
    }
    
    public void update(DataContext c, Collection<? extends IEntityWrapper> entities)
    {
        this.entities = entities;
        notesModel.setContext(c);
        setEntities(entities);
    }
    
    public void setEntities(final Collection<? extends IEntityWrapper> entities)
    {
        EventQueueUtilities.runOffEDT(new Runnable(){

            @Override
            public void run() {
                renderer.editor = null;
                notesModel.setEntities(entities);
            }
        });
        
    }

    Collection<? extends IEntityWrapper> getEntities() {
        return entities;
    }
}
