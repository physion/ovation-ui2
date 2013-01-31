/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

//import net.imglib2.img.ImagePlusAdapter;
//import net.imglib2.img.Img;
//import net.imglib2.img.ImgPlus;
//import net.imglib2.img.display.imagej.ImageJFunctions;
//import net.imglib2.io.ImgIOException;
//import net.imglib2.io.ImgOpener;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import ovation.OvationException;
import ovation.Response;
import ovation.URLResponse;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author huecotanks
 */
    public class ImageJVisualizationFactory implements VisualizationFactory{

    @Override
	public Visualization createVisualization(Response r) {
        if (r instanceof URLResponse)
	    {
		String url = ((URLResponse)r).getURLString();
                try {
		return new ImageJVisualization(url, r.getExternalDevice().getName());
                } catch (Throwable e) {
                    System.out.println(e);
                    throw new OvationException("Unable to create ImageJVisualization", e);
                }
	    }
        
        throw new OvationException("Embedded responses not supported. Use URLResponse.");
    }

    @Override
	public int getPreferenceForDataContainer(Response r) {
        if (r.getUTI().toLowerCase().contains("tif"))
	    {
		return 110;
	    }
        return -1;
    }
    
    }