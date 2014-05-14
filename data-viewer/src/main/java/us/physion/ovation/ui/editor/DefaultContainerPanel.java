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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.physion.ovation.domain.OvationEntity;

public class DefaultContainerPanel extends JPanel {

    OvationEntity entity;
    JLabel elementName;
    JLabel messageLabel;

    public DefaultContainerPanel(OvationEntity entity) {
        this.entity = entity;
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        elementName = new JLabel(entity.getClass().getSimpleName());

        c.insets = new Insets(10, 10, 10, 10);
        c.anchor = GridBagConstraints.NORTH;
        c.weighty = 0.0;
        c.gridy = 0;
        add(elementName, c);
        c.gridy = 1;
        //add(messageLabel, c);
        c.gridy = 2;
        c.weighty = 1.0;
    }
}
