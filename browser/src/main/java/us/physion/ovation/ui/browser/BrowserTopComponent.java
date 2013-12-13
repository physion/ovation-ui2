package us.physion.ovation.ui.browser;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//us.physion.ovation.ui.browser//Browser//EN",
autostore = false)
@TopComponent.Description(preferredID = "BrowserTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "us.physion.ovation.ui.browser.BrowserTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_BrowserAction",
preferredID = "BrowserTopComponent")
@Messages({
    "CTL_BrowserAction=Project Navigator",
    "CTL_BrowserTopComponent=Project Navigator",
    "HINT_BrowserTopComponent=Browse your Ovation Database"
})
public final class BrowserTopComponent extends TopComponent implements ExplorerManager.Provider{

    private Lookup lookup;
    private final ExplorerManager explorerManager = new ExplorerManager();

    
    public BrowserTopComponent() {
        final TreeFilter filter = new TreeFilter();
        filter.setProjectView(true);
        
        setLayout(new BorderLayout());
        add(new FilteredTreeViewPanel(filter), BorderLayout.CENTER);
        
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
        
    }

    @Override
    public Lookup getLookup()
    {
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
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
}
