package us.physion.ovation.ui.actions.impl;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.actions.spi.FileManager;

/**
 * Fallback FileManager implementation using Java Desktop
 **/
@ServiceProvider(service = FileManager.class, position = 100)
@Messages({
    "Reveal_Text=Open in File Explorer...",
    "Java_Desktop_not_supported=Java Desktop not supported on this machine"
})
public class JavaDesktopFileManager implements FileManager {

    /**
     * <b>Note</b>: Doesn't really reveal the file, just opens the parent folder.
     */
    @Override
    public boolean revealInFinder(File f) {
        File parent = f.getParentFile();
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(parent);

                return true;
            } else {
                throw new OvationException(Bundle.Java_Desktop_not_supported());
            }
        } catch (IOException ex) {
            throw new OvationException(ex);
        }
    }

    @Override
    public String getRevealText() {
        return Bundle.Reveal_Text();
    }
}
