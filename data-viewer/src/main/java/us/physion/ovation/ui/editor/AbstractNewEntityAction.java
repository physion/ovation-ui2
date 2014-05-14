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

package us.physion.ovation.ui.editor;

import com.google.common.collect.Lists;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import us.physion.ovation.domain.OvationEntity;

/**
 *
 * @author barry
 */
public abstract class AbstractNewEntityAction<T extends OvationEntity> extends AbstractAction implements ActionListener{
    
    protected void selectNode(final T entity, 
            final String topComponentId, 
            final ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OpenNodeInBrowserAction(Lists.newArrayList(entity.getURI()),
                        null,
                        false,
                        Lists.<URI>newArrayList(),
                        topComponentId).actionPerformed(e);
            }
        });
    }
}
