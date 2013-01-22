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
import java.io.IOException;
import org.openide.util.Exceptions;
import ovation.Response;
import ovation.URLResponse;

/**
 *
 * @author huecotanks
 */
public class DicomWrapper implements Visualization {

    String name;
    SourceImage src;

    DicomWrapper(Response r) {
        DicomInputStream in = null;
        try {
            if (r instanceof URLResponse)
                in = new DicomInputStream(r.getDataStream());
            else{
                in = new DicomInputStream(new ByteArrayInputStream(r.getDataBytes()));
            }
            src = new SourceImage(in);
            this.name = r.getExternalDevice().getName();
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
    public boolean shouldAdd(Response r) {
        return false;
    }

    @Override
    public void add(Response r) {
        throw new UnsupportedOperationException("Dicoms are not displayed in groups");
    }
    
}
