package us.physion.ovation.ui.browser;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import us.physion.ovation.ui.browser.TreeFilter.NavigatorType;
import us.physion.ovation.ui.interfaces.TreeViewProvider;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//us.physion.ovation.ui.browser//ProjectBrowser//EN",
        autostore = false)
@TopComponent.Description(preferredID = "ProjectBrowserTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "us.physion.ovation.ui.browser.ProjectBrowserTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_BrowserAction",
        preferredID = "ProjectBrowserTopComponent")
@Messages({
    "CTL_BrowserAction=Projects Navigator",
    "CTL_BrowserTopComponent=Projects",
    "HINT_BrowserTopComponent=Browse your Ovation Database"
})
public final class ProjectBrowserTopComponent extends TopComponent implements ExplorerManager.Provider, TreeViewProvider {

    private Lookup lookup;
    private final ExplorerManager explorerManager = new ExplorerManager();
    private final BeanTreeView view;
    private final TreeFilter filter;
    
    private static final String SHOW_FIRST_RUN_TIP = "show_first_run_tip";

    public ProjectBrowserTopComponent() {

        filter = new TreeFilter(NavigatorType.PROJECT);

        final Preferences prefs = NbPreferences.forModule(ProjectBrowserTopComponent.class);

        filter.setExperimentsVisible(prefs.getBoolean("experiments-visible", true));
        filter.setEpochGroupsVisible(prefs.getBoolean("epoch-groups-visible", false));
        filter.setEpochsVisible(prefs.getBoolean("epochs-visible", false));
        
        filter.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean newValue = (Boolean) evt.getNewValue();

                if (evt.getPropertyName().equals("experimentsVisible")) {
                    prefs.putBoolean("experiments-visible", newValue);
                }

                if (evt.getPropertyName().equals("epochGroupsVisible")) {
                    prefs.putBoolean("epoch-groups-visible", newValue);
                }

                if(evt.getPropertyName().equals("epochsVisible")) {
                    prefs.putBoolean("epochs-visible", newValue);
                }
            }
        });

        setLayout(new BorderLayout());
        FilteredTreeViewPanel panel = new FilteredTreeViewPanel(filter, "us.physion.ovation.ui.browser.insertion.NewProjectAction");
        view = panel.getTreeView();
        add(panel, BorderLayout.CENTER);

        setName(Bundle.CTL_BrowserTopComponent());
        setToolTipText(Bundle.HINT_BrowserTopComponent());

        lookup = ExplorerUtils.createLookup(explorerManager, getActionMap());
        associateLookup(lookup);
        BrowserUtilities.initBrowser(explorerManager, filter);
        filter.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                BrowserUtilities.resetView(explorerManager, filter);
            }
        });

        ActionMap actionMap = this.getActionMap();
        actionMap.put("copy-to-clipboard", (Action) new BrowserCopyAction());
        
        if(prefs.getBoolean(SHOW_FIRST_RUN_TIP, true)) {
            //prefs.putBoolean(SHOW_FIRST_RUN_TIP, false);
        }

    }

    @Override
    public Action[] getActions() {
        return ActionUtils.appendToArray(new Action[]{new ResettableAction(this), null}, super.getActions());
    }

    @Override
    public Object getTreeView() {
        return view;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public void componentOpened() {
        //BrowserUtilities.createTreeComponent(em, true);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");

    }

    void readProperties(java.util.Properties p) {
        //String version = p.getProperty("version");

    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
}
