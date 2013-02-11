package us.physion.ovation.ui;

import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

public class ApplicationStuff extends NbTestCase {

    public static Test suite() {
        /*return NbModuleSuite.createConfiguration(ApplicationTest.class).suite();
	.
                gui(false).
	    failOnMessage(Level.WARNING). // works at least in RELEASE71
	                    failOnException(Level.INFO).
                suite(); // RELEASE71+, else use NbModuleSuite.create(NbModuleSuite.createConfiguration(...))
	*/

    return NbModuleSuite.allModules(ApplicationStuff.class);

    }

    public ApplicationStuff(String n) {
        super(n);
    }

    public void testApplication() {
        // pass if there are merely no warnings/exceptions
        /* Example of using Jelly Tools with gui(true):
        new ActionNoBlock("Help|About", null).performMenu();
        new NbDialogOperator("About").closeByButton();
         */
    }

}
