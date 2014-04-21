/*
 * Copyright (C) 2014 Physion LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package us.physion.ovation.ui.browser;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;
import us.physion.ovation.domain.mixin.Identity;
import us.physion.ovation.ui.actions.SelectInProjectNavigatorActionFactory;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.URITreePathProvider;

public abstract class AbstractInputsAction<Record, Input extends Identity> extends NodeAction {

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
        Record record = getRecord(wrapper);

        if (record == null) {
            return null;
        }

        URITreePathProvider pathProvider = nodes[0].getLookup().lookup(URITreePathProvider.class);
        final List<URI> analysisURI = pathProvider != null ? pathProvider.getTreePath() : null;

        Map<String, Input> inputs = getInputs(record);

        if (inputs.isEmpty()) {
            return null;
        }

        JMenuItem presenters = new JMenu(getMenuDisplayName());

        for (Map.Entry<String, Input> e : inputs.entrySet()) {
            final Input data = e.getValue();
            //XXX: input key == data.getName()?
            presenters.add(new JMenuItem(new AbstractAction(e.getKey()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SelectInProjectNavigatorActionFactory f = Lookup.getDefault().lookup(SelectInProjectNavigatorActionFactory.class);
                    if (f != null) {
                        f.select(data, getDisplayName(data), analysisURI).actionPerformed(null);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }));
        }

        return presenters;
    }
    
    protected abstract Record getRecord(IEntityWrapper wrapper);
    protected abstract Map<String, Input> getInputs(Record record);
    protected abstract String getDisplayName(Input i);
    protected abstract String getMenuDisplayName();

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
        Record record = getRecord(wrapper);
        return record != null && !getInputs(record).isEmpty();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
