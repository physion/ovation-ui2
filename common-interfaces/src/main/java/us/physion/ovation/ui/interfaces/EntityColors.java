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

package us.physion.ovation.ui.interfaces;

import java.awt.Color;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochGroup;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.Resource;

/**
 *
 * @author barry
 */
public class EntityColors {

    public static Color getEntityColor(Class<? extends OvationEntity> cls) {

        if (Source.class.isAssignableFrom(cls)) {
            return new Color(110, 43, 98);
        } else if (Project.class.isAssignableFrom(cls)) {
            return new Color(0, 89, 153);
        } else if (Experiment.class.isAssignableFrom(cls)) {
            return Color.darkGray;
        } else if (EpochGroup.class.isAssignableFrom(cls)) {
            return Color.black;
        } else if (Epoch.class.isAssignableFrom(cls)) {
            return Color.black;
        } else if (Resource.class.isAssignableFrom(cls)) {
            return new Color(0, 126, 189);
        } else if (AnalysisRecord.class.isAssignableFrom(cls)) {
            return new Color(51, 153, 0);
        } else if (Protocol.class.isAssignableFrom(cls)) {
            return new Color(255, 158, 27);
        }

        return Color.BLACK;
    }

    public static String getEntityColorHex(Class<? extends OvationEntity> cls) {
        return colorToHex(getEntityColor(cls));
    }

    public static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x",
                color.getRed(),
                color.getGreen(),
                color.getBlue());
    }
}
