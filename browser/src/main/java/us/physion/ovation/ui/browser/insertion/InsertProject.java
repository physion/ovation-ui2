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

package us.physion.ovation.ui.browser.insertion;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.openide.WizardDescriptor;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.DataContext;
import us.physion.ovation.ui.browser.BrowserUtilities;
import us.physion.ovation.ui.interfaces.RootInsertable;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

@ServiceProvider(service=RootInsertable.class)
public class InsertProject extends InsertEntity implements RootInsertable {

    public InsertProject() {
        putValue(NAME, "Insert Project...");
    }

    @Override
    public List<WizardDescriptor.Panel<WizardDescriptor>> getPanels(IEntityWrapper parent)
    {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new InsertProjectWizardPanel1());
        return panels;
    }
    @Override
    public void wizardFinished(WizardDescriptor wiz, DataContext c, IEntityWrapper parent)
    {
            c.insertProject((String)wiz.getProperty("project.name"),
                    (String)wiz.getProperty("project.purpose"),
                    (DateTime)wiz.getProperty("project.start"));

            BrowserUtilities.resetView();
    }
}
