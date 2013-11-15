package us.physion.ovation.ui.osx;

import java.io.File;
import java.lang.reflect.Method;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.actions.spi.FileManager;

@ServiceProvider(service = FileManager.class, position = 10)
@Messages({
    "Open_in_Finder=Open in Finder..."
})
public class OSXFileManager implements FileManager {

    @Override
    public boolean revealInFinder(File f) {
        try {
            Class fm = Class.forName("com.apple.eio.FileManager"); //NOI18N

            Method m = fm.getDeclaredMethod("revealInFinder", File.class); //NOI18N
            Object r = m.invoke(null, f);

            return (r instanceof Boolean) ? (Boolean) r : true;
        } catch (Exception ex) {
            throw new OvationException(ex);
        }
    }

    @Override
    public String getRevealText() {
        return Bundle.Open_in_Finder();
    }
}
