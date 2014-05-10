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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.swing.JComponent;
import us.physion.ovation.domain.OvationEntity;

/**
 *
 * @author barry
 */
public abstract class AbstractDataVisualization implements DataVisualization {

    private final Set<OvationEntity> entities;

    public AbstractDataVisualization() {
        this(ImmutableSet.<OvationEntity>of());
    }

    public AbstractDataVisualization(Iterable<? extends OvationEntity> entities) {
        this.entities = ImmutableSet.copyOf(entities);
    }

    @Override
    public Iterable<? extends OvationEntity> getEntities() {
        return entities;
    }


    @Override
    public JComponent generateInfoPanel() {
        return new DataElementInfoPanel(getEntities());
    }

}
