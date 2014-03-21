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

import javax.swing.Action;

public final class ActionUtils {

    private ActionUtils() {
        //nothing
    }

    public static Action[] appendToArray(Action[] list, Action... e) {
        int listLen = list == null ? 0 : list.length;
        Action[] expanded = new Action[listLen + e.length];
        if (list != null) {
            System.arraycopy(list, 0, expanded, 0, list.length);
        }
        System.arraycopy(e, 0, expanded, listLen, e.length);

        return expanded;
    }
}
