/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
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
import ovation.*;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//us.physion.ovation.detailviews//ResourceView//EN",
autostore = false)
@TopComponent.Description(preferredID = "ResourceViewTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "properties", openAtStartup = true)
@ActionID(category = "Window", id = "us.physion.ovation.detailviews.ResourceViewTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ResourceViewAction",
preferredID = "ResourceViewTopComponent")
@Messages({
    "CTL_ResourceViewAction=Resources",
    "CTL_ResourceViewTopComponent=Resources",
    "HINT_ResourceViewTopComponent=This window displays the Resource objects associated with the selected Ovation entity"
})
public final class ResourceViewTopComponent extends TopComponent {

    private LookupListener listener = new LookupListener() {

        @Override
        public void resultChanged(LookupEvent le) {

            //TODO: we should have some other Interface for things that can update the tags view
            //then we could get rid of the Library dependancy on the Explorer API
            if (TopComponent.getRegistry().getActivated() instanceof ExplorerManager.Provider)
            {
                //closeEditedResourceFiles();
                updateResources();
            }
        }

    };
    protected Lookup.Result<IEntityWrapper> global;
    protected Collection<? extends IEntityWrapper> entities;
    protected ResourceListModel listModel;
    protected Set<IResourceWrapper> editedSet = new HashSet();
    boolean saveButtonEnabled;

    public ResourceViewTopComponent() {
        initComponents();
        setSavedButtonEnabled(false);
        resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setName(Bundle.CTL_ResourceViewTopComponent());
        setToolTipText(Bundle.HINT_ResourceViewTopComponent());

        global = Utilities.actionsGlobalContext().lookupResult(IEntityWrapper.class);
        global.addLookupListener(listener);
        
        resourceList.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                int index = -1;
                if (evt.getClickCount() == 2 || evt.getClickCount() == 3) {
                    index = list.locationToIndex(evt.getPoint());
                    final IResourceWrapper rw = (IResourceWrapper) listModel.getElementAt(index);
                    EventQueueUtilities.runOffEDT(new Runnable(){

                        @Override
                        public void run() {
                            editResource(rw);
                        }
                    });
                }
            }
        });
        
        resourceList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent lse) {
                
                for (Object value: resourceList.getSelectedValues())
                {
                    if (editedSet.contains(value))
                    {
                       setSavedButtonEnabled(true);
                       return;
                    }
                }
                setSavedButtonEnabled(false);
            }
        });
    }
    
    protected void editResource(IResourceWrapper rw)
    {
        if (!editedSet.contains(rw)) {
            Resource r = rw.getEntity();
            try{
                r.edit();
            } catch(OvationException e)
            {
                //pass, for now - this can be deleted with ovation version 1.4
            } catch(UnsupportedOperationException e)
            {
                //pass, for now - this can be deleted with ovation version 1.4
            }
            editedSet.add(rw);
            
            setSavedButtonEnabled(true);
        }
    }
    
    protected void setSavedButtonEnabled(final boolean enable)
    {
        saveButtonEnabled = enable;
        EventQueueUtilities.runOnEDT(new Runnable(){

                @Override
                public void run() {
                    saveButton.setEnabled(enable);
                }
            });
    }
    
    protected void closeEditedResourceFiles()
    {
        for (IResourceWrapper rw : editedSet)
        {
            rw.getEntity().releaseLocalFile();
        }
        editedSet = new HashSet();
        setSavedButtonEnabled(false);
    }

    protected void updateResources()
    {
        entities = global.allInstances();
        EventQueueUtilities.runOffEDT(new Runnable(){

            @Override
            public void run() {
                updateResources(entities);
            }
        });
    }
    
    protected void updateResources(Collection<? extends IEntityWrapper> entities)
    {
        List<IResourceWrapper> resources = new LinkedList();
        for (IEntityWrapper e: entities)
        {
            for (Resource r : e.getEntity().getResourcesIterable())
            {
                resources.add(new ResourceWrapper(r));
            }
        }
        
        LinkedList<IResourceWrapper> toRemove = new LinkedList();
        for (IResourceWrapper rw : editedSet)
        {
            if (!resources.contains(rw))
            {
                toRemove.add(rw);
            }
        }
        
        //TODO: wrap in a transaction? run on another thread?
        for (IResourceWrapper rw : toRemove)
        {
            editedSet.remove(rw);
            rw.getEntity().releaseLocalFile();
        }
        
        listModel.setResources(resources);

        if (editedSet.isEmpty()) {
            setSavedButtonEnabled(false);
        }
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        resourceList = new javax.swing.JList();
        insertResourceButton = new javax.swing.JButton();
        removeResourceButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        listModel = new ResourceListModel();
        resourceList.setModel(listModel);
        jScrollPane1.setViewportView(resourceList);

        org.openide.awt.Mnemonics.setLocalizedText(insertResourceButton, org.openide.util.NbBundle.getMessage(ResourceViewTopComponent.class, "ResourceViewTopComponent.insertResourceButton.text")); // NOI18N
        insertResourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertResourceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeResourceButton, org.openide.util.NbBundle.getMessage(ResourceViewTopComponent.class, "ResourceViewTopComponent.removeResourceButton.text")); // NOI18N
        removeResourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeResourceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(ResourceViewTopComponent.class, "ResourceViewTopComponent.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(insertResourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeResourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(insertResourceButton)
                    .addComponent(removeResourceButton)
                    .addComponent(saveButton))
                .addGap(28, 28, 28))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void insertResourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertResourceButtonActionPerformed
        //addButton
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(new JPanel());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            //add to preferences
            //TODO add a dialog that asks for uti type
            String path = chooser.getSelectedFile().getAbsolutePath();
            addResource(entities, path);
        }
    }//GEN-LAST:event_insertResourceButtonActionPerformed

    private void removeResourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeResourceButtonActionPerformed
        //Delete selected resources
        EventQueueUtilities.runOffEDT(new Runnable(){
            public void run()
            {
                removeResources(resourceList.getSelectedValues(), entities);
            }
        });
    }//GEN-LAST:event_removeResourceButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        final Object[] rws = resourceList.getSelectedValues();
        EventQueueUtilities.runOffEDT(new Runnable() {

            @Override
            public void run() {
                for (Object rw : rws) {
                    Resource r = ((IResourceWrapper) rw).getEntity();
                    if (r.canWrite()) {
                        r.sync();
                    }
                }
            }
        });
    }//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton insertResourceButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeResourceButton;
    private javax.swing.JList resourceList;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        this.setSavedButtonEnabled(false);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
        closeEditedResourceFiles();
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

    protected void removeResources(Object[] selectedValues, Collection<? extends IEntityWrapper> entities) {
        for (Object o :selectedValues)
        {
            if (o instanceof IResourceWrapper) {
                String rName = ((IResourceWrapper) o).getName();
                for (IEntityWrapper e : entities) {
                    IEntityBase eb = e.getEntity();
                    for (String name : eb.getResourceNames()) {
                        if (name.equals(rName)) {
                            Resource r = eb.getResource(name);
                            if (r.canWrite()) {
                                eb.removeResource(r);
                            }
                        }
                    }
                }
            }
        }
        updateResources(entities);// don't regrab entities from the current TopComponent
    }
    
    protected boolean saveButtonIsEnabled()
    {
        return saveButtonEnabled;
    }
    
    protected List<IResourceWrapper> getResources()
    {
        return listModel.getResources();
    }
    
    protected void addResource(Collection<? extends IEntityWrapper> entities, String path)
    {
        Resource r = null;

        for (IEntityWrapper e : entities) {
            r = e.getEntity().addResource(Response.NUMERIC_DATA_UTI, path);
        }
        if (r != null) {
            listModel.addResource(new ResourceWrapper(r));
        }
    }

    private class ResourceListModel extends AbstractListModel
    {
        List<IResourceWrapper> resources = new LinkedList<IResourceWrapper>();
        public List<IResourceWrapper> getResources()
        {
            return resources;
        }
        
        @Override
        public int getSize() {
            return resources.size();
        }

        @Override
        public Object getElementAt(int i) {
            if (i < resources.size())
                return resources.get(i);
            return null;
        }

        protected void setResources(List<IResourceWrapper> newResources)
        {
            int length = Math.max(resources.size(), newResources.size());
            resources = newResources;
            this.fireContentsChanged(this, 0, length);
        }

        protected void addResource(IResourceWrapper resource)
        {
            resources.add(resource);
            this.fireContentsChanged(this, resources.size(), resources.size());
        }
    };

}
