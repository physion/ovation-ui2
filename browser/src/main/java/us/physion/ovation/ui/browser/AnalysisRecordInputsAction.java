package us.physion.ovation.ui.browser;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.NodeAction;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.domain.Revision;
import us.physion.ovation.ui.actions.SelectInProjectNavigatorActionFactory;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.URITreePathProvider;

@ActionID(
    category = "Navigate",
id = "us.physion.ovation.ui.browser.AnalysisRecordInputsAction")
@ActionRegistration(
    displayName = "#CTL_AnalysisRecordInputsAction")
@Messages("CTL_AnalysisRecordInputsAction=Inputs")
public final class AnalysisRecordInputsAction extends NodeAction {

    @Override
    public JMenuItem getPopupPresenter() {
        return getMenuPresenter();
    }

    @Override
    public JMenuItem getMenuPresenter() {
        Node[] nodes = getActivatedNodes();
        if (nodes == null || nodes.length != 1) {
            return null;
        }

        IEntityWrapper wrapper = nodes[0].getLookup().lookup(IEntityWrapper.class);
        AnalysisRecord record = wrapper.getEntity(AnalysisRecord.class);

        if (record == null) {
            return null;
        }

        URITreePathProvider pathProvider = nodes[0].getLookup().lookup(URITreePathProvider.class);
        final List<URI> analysisURI = pathProvider != null ? pathProvider.getTreePath() : null;
        
        Map<String, Revision> inputs = record.getInputs();
        
        if (inputs.isEmpty()) {
            return null;
        }

        JMenuItem presenters = new JMenu(Bundle.CTL_AnalysisRecordInputsAction());

        for (Map.Entry<String, Revision> e : inputs.entrySet()) {
            final Revision data = e.getValue();
            //XXX: input key == data.getName()?
            presenters.add(new JMenuItem(new AbstractAction(e.getKey()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SelectInProjectNavigatorActionFactory f = Lookup.getDefault().lookup(SelectInProjectNavigatorActionFactory.class);
                    if (f != null) {
                        f.select(data, data.getResource().getLabel(), analysisURI).actionPerformed(null);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }));
        }

        return presenters;
    }


    @Override
    protected void performAction(Node[] nodes) {
        //impossible
    }

    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return false;
        }
        IEntityWrapper wrapper = nodes[0].getLookup().lookup(IEntityWrapper.class);
        if (wrapper == null) {
            return false;
        }
        AnalysisRecord record = wrapper.getEntity(AnalysisRecord.class);
        return record != null && !record.getInputs().isEmpty();
    }

    @Override
    public String getName() {
        return Bundle.CTL_AnalysisRecordInputsAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
