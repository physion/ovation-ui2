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
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//us.physion.ovation.ui.browser//SourceBrowser//EN",
autostore = false)
@TopComponent.Description(preferredID = "SourceBrowserTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "us.physion.ovation.ui.browser.SourceBrowserTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_SourceBrowserAction",
preferredID = "SourceBrowserTopComponent")
@Messages({
    "CTL_SourceBrowserAction=Source Navigator",
    "CTL_SourceBrowserTopComponent=Source Navigator",
    "HINT_SourceBrowserTopComponent=Browse your Ovation Dataset starting from the Source hierarchy"
})
public final class SourceBrowserTopComponent extends TopComponent implements ExplorerManager.Provider {

    private final ExplorerManager em = new ExplorerManager();

    public SourceBrowserTopComponent() {
        final TreeFilter filter = new TreeFilter();
        filter.setProjectView(false);
        
        setLayout(new BorderLayout());
        add(new FilteredTreeViewPanel(filter), BorderLayout.CENTER);
        
        setName(Bundle.CTL_SourceBrowserTopComponent());
        setToolTipText(Bundle.HINT_SourceBrowserTopComponent());
        
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));

        BrowserUtilities.initBrowser(em, filter);
        filter.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                BrowserUtilities.resetView(em, filter);
            }
        });
        
        ActionMap actionMap = this.getActionMap();
        actionMap.put("copy-to-clipboard", (Action) new BrowserCopyAction());
    }

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

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
}
