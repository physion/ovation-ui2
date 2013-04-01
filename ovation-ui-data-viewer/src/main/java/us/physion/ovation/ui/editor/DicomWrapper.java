/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.display.SingleImagePanel;
import com.pixelmed.display.SourceImage;
import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.openide.util.Exceptions;
import us.physion.ovation.domain.Measurement;

/**
 *
 * @author huecotanks
 */
public class DicomWrapper implements Visualization {

    String name;
    SourceImage src;

    DicomWrapper(Measurement r) {
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(new FileInputStream(r.getData().get()));
            src = new SourceImage(in);
            this.name = r.getName();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex.getLocalizedMessage());
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex.getLocalizedMessage());
        } catch (DicomException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex.getLocalizedMessage());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex.getLocalizedMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    throw new RuntimeException(ex.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public Component generatePanel() {
        ImagePanel p = new ImagePanel(name, new SingleImagePanel(src));
        return p;
    }

    @Override
    public boolean shouldAdd(Measurement r) {
        return false;
    }

    @Override
    public void add(Measurement r) {
        throw new UnsupportedOperationException("Dicoms are not displayed in groups");
    }
    
}
