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

import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author barry
 */
public class Installer extends ModuleInstall {
    @Override
    public synchronized void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            public void run() {
                Lookup.getDefault().lookup(ConnectionProvider.class).resetConnection();
            }
        });
    }
}
