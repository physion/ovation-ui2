package us.physion.ovation.ui.detailviews;

import java.awt.BorderLayout;
import java.io.File;
import java.util.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.mixin.PropertyAnnotatable;
import us.physion.ovation.loader.TabularService;
import us.physion.ovation.ui.*;
import us.physion.ovation.ui.dnd.FileWellService;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.interfaces.IEntityWrapper;


@ConvertAsProperties(dtd = "-//us.physion.ovation.detailviews//PropertiesView//EN",
autostore = false)
@TopComponent.Description(preferredID = "PropertiesViewTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "properties", openAtStartup = true)
@ActionID(category = "Window", id = "us.physion.ovation.detailviews.PropertiesViewTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_PropertiesViewAction",
preferredID = "PropertiesViewTopComponent")
@Messages({
    "CTL_PropertiesViewAction=Properties",
    "CTL_PropertiesViewTopComponent=Properties",
    "HINT_PropertiesViewTopComponent=Displays the properties of the selected entites",
    "Importing_Properties=Importing properties",
    "Drop_File=Add Properties",
    "Drop_File_Tooltip=Drop CSV or Excel file with 2 columns to add as properties"
})
public final class PropertiesViewTopComponent extends TopComponent {

    Lookup.Result global;
    private Collection<? extends IEntityWrapper> entities;
    private final LookupListener listener = (LookupEvent le) -> {
        //TODO: we should have some other Interface for things that can update the tags view
        //then we could get rid of the Library dependancy on the Explorer API
        if (TopComponent.getRegistry().getActivated() instanceof ExplorerManager.Provider)
        {
            update();
        }
    };

    public void update()
    {
        EventQueueUtilities.runOffEDT(() -> {
            update(global.allInstances());
        });
    }

    public us.physion.ovation.ui.ScrollableTableTree getTableTree()
    {
        return (us.physion.ovation.ui.ScrollableTableTree)tableTree;
    }

    protected void setTableTree(us.physion.ovation.ui.ScrollableTableTree t)
    {
        tableTree = t;
    }

    public void update(final Collection<? extends IEntityWrapper> entities)
    {
        DataContext c = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        setEntities(entities, c);

        if (entities.size() > 1) {
            EventQueueUtilities.runOnEDT(() -> {
                setName(Bundle.CTL_PropertiesViewTopComponent() + " - " + entities.size() + " entities");
            });
        } else {
            EventQueueUtilities.runOnEDT(() -> {
                setName(Bundle.CTL_PropertiesViewTopComponent());
            });
        }
    }

    protected List<TableTreeKey> setEntities(Collection<? extends IEntityWrapper> entities, DataContext c)
    {
        List<TableTreeKey> properties = PerUserAnnotationSets.createPropertySets(entities, c);
        ((ScrollableTableTree) tableTree).setKeys(properties);

        this.entities = entities;
        return properties;
    }

    public PropertiesViewTopComponent() {
        initComponents();
        setName(Bundle.CTL_PropertiesViewTopComponent());
        setToolTipText(Bundle.HINT_PropertiesViewTopComponent());

        global = Utilities.actionsGlobalContext().lookupResult(IEntityWrapper.class);
        global.addLookupListener(listener);

        add(FileWellService.getDefault().createFileWell(new FileWellService.FileWellHandler() {

            @Override
            public void filesDropped(final File[] files) {
                final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Importing_Properties());
                ph.start();
                RequestProcessor.getDefault().post(() -> {
                    Collection<? extends IEntityWrapper> entities = getEntities();
                    if (entities == null || entities.isEmpty()) {
                        ph.finish();
                        return;
                    }

                    try {
                        for (File f : files) {
                            String[][] tabularData = TabularService.load(f);
                            if (tabularData != null && tabularData.length > 0 && tabularData[0].length == 2) {
                                Map<String, Object> keyValue = toMap(tabularData);
                                
                                for (IEntityWrapper wrapper : entities) {
                                    OvationEntity entity = wrapper.getEntity();
                                    if (entity instanceof PropertyAnnotatable) {
                                        ((PropertyAnnotatable) entity).addAllProperties(keyValue);
                                    }
                                }
                            }
                        }
                        
                        //invalidate?
                        update(entities);
                    } finally {
                        ph.finish();
                    }
                });
            }

            @Override
            public String getPrompt() {
                return Bundle.Drop_File();
            }

            @Override
            public String getTooltip() {
                return Bundle.Drop_File_Tooltip();
            }

            private Map<String, Object> toMap(String[][] tabularData) {
                Map<String, Object> keyValue = new HashMap<>();
                for (String[] prop : tabularData) {
                    if (prop.length == 2) {
                        keyValue.put(prop[0], PropertyTableModelListener.parse(prop[1]));
                    }
                }
                return keyValue;
            }

        }), BorderLayout.SOUTH);
    }

    public Collection<? extends IEntityWrapper> getEntities()
    {
        return entities;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableTree = new us.physion.ovation.ui.ScrollableTableTree();

        setLayout(new java.awt.BorderLayout());
        add(tableTree, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane tableTree;
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
