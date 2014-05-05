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

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.OvationEntity;
/**
 * Acts as a visualization factor for all containers that have a
 * {@code [Class]VisualizationPanel} defined.
 *
 * @author barry
 */
@ServiceProvider(service = ContainerVisualizationFactory.class)
public class ReflectionContainerVisualizationFactory implements ContainerVisualizationFactory {

    @Override
    public ContainerVisualization createVisualization(OvationEntity e) {
        return new ReflectionContainerVisualization(e);
    }

    @Override
    public int getPreferenceForContainer(OvationEntity e) {
        try {
            Class cls = Class.forName("us.physion.ovation.ui.editor."
                    + e.getClass().getSimpleName()
                    + "VisualizationPanel");

            if (Component.class.isAssignableFrom(cls)) {
                return 100;
            }

            return -1;
        } catch (ClassNotFoundException ex) {
            return -1;
        }
    }

    class ReflectionContainerVisualization implements ContainerVisualization {

        final OvationEntity entity;

        public ReflectionContainerVisualization(OvationEntity e) {
            this.entity = e;
        }

        @Override
        public Component generatePanel() {
            try {
                Class cls = Class.forName("us.physion.ovation.ui.editor."
                        + entity.getClass().getSimpleName()
                        + "VisualizationPanel");

                Class entityCls = Class.forName("us.physion.ovation.domain."
                        + entity.getClass().getSimpleName());

                return (Component) cls.getConstructor(entityCls).newInstance(entity);
            } catch (ClassNotFoundException ex) {
                return new DefaultContainerVisualizationFactory()
                        .createVisualization(entity)
                        .generatePanel();
            } catch (NoSuchMethodException ex) {
                return new DefaultContainerVisualizationFactory()
                        .createVisualization(entity)
                        .generatePanel();
            } catch (SecurityException ex) {
                return new DefaultContainerVisualizationFactory()
                        .createVisualization(entity)
                        .generatePanel();
            } catch (InstantiationException ex) {
                return new DefaultContainerVisualizationFactory()
                        .createVisualization(entity)
                        .generatePanel();
            } catch (IllegalAccessException ex) {
                return new DefaultContainerVisualizationFactory()
                        .createVisualization(entity)
                        .generatePanel();
            } catch (IllegalArgumentException ex) {
                return new DefaultContainerVisualizationFactory()
                        .createVisualization(entity)
                        .generatePanel();
            } catch (InvocationTargetException ex) {
                return new DefaultContainerVisualizationFactory()
                        .createVisualization(entity)
                        .generatePanel();
            }
        }
    }

}
