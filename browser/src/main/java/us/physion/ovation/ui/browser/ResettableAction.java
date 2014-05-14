/*
 * Copyright (C) 2014 Physion Consulting LLC
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
import javax.swing.AbstractAction;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.ui.interfaces.RefreshableNode;

@Messages({
    "CTL_Reload=Reload"
})
public class ResettableAction extends AbstractAction {

    private final RefreshableNode node;
    private final Provider explorer;

    public ResettableAction(RefreshableNode node) {
        this(node, null);
    }

    public ResettableAction(ExplorerManager.Provider explorer) {
        this(null, explorer);
    }

    private ResettableAction(RefreshableNode node, ExplorerManager.Provider explorer) {
        super(Bundle.CTL_Reload());
        this.node = node;
        this.explorer = explorer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RefreshableNode target = node;
        if (target == null && explorer != null) {
            Node root = explorer.getExplorerManager().getRootContext();
            if (root instanceof RefreshableNode) {
                target = (RefreshableNode) root;
            }
        }
        if (target != null) {
            target.refresh();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
