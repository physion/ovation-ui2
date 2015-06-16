/*
 * Copyright (C) 2015 Physion LLC
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

import java.io.File;
import javax.swing.JComponent;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.ui.dnd.FileWellService;

@ServiceProvider(service = FileWellService.class)
public class FileWellServiceImpl extends FileWellService {

    @Override
    public JComponent createFileWell(final FileWellHandler handler) {
        FileWell well = new FileWell();

        well.setDelegate(new FileWell.AbstractDelegate(handler.getPrompt(), handler.getTooltip()) {

            @Override
            public void filesDropped(File[] files) {
                handler.filesDropped(files);
            }
            
        });

        return well;
    }

}
