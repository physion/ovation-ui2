/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.io.Opener;
import imagej.ImageJ;
import imagej.display.DisplayService;
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
		return new ImageJVisualization(url);
		/*try {
		  /*ImgPlus ip = ImgOpener.open(((URLResponse)r).getURLString());
// display the dataset
DisplayService displayService = new ImageJ().getService(DisplayService.class);
displayService.getActiveDisplay().display(ip);
return new ImageJVisualization(((URLResponse)r).getURLString());*/
                
                // define the file to open

       
 
		// display it via ImageJ
		//imp.show();//null pointer
 
		// wrap it into an ImgLib image (no copying)
		//final Img image = ImagePlusAdapter.wrap( imp );
 
		// display it via ImgLib using ImageJ
		//ImageJFunctions.show( image );
		/*} catch (Exception ex) {
System.out.println(ex.getMessage());
/*try{
ImgPlus ip = ImgOpener.open(((URLResponse)r).getURLString());
// display the dataset
DisplayService displayService = new ImageJ().getService(DisplayService.class);
displayService.getActiveDisplay().display(ip);
return new ImageJVisualization(((URLResponse)r).getURLString());
/*System.out.println("First error " + e.getMessage());
try{
// load the dataset
final IOService ioService = context.getService(IOService.class);
final Dataset dataset = ioService.loadDataset(url);

// display the dataset
final DisplayService displayService =
context.getService(DisplayService.class);
displayService.createDisplay(file.getName(), dataset);*/
                /*}catch (Exception e)
{
System.out.println(e.getMessage());
}*/
	    /* }*/
	    }
        
        return new ImageJVisualization(null);
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