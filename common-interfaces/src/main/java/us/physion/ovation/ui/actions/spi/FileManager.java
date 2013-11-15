package us.physion.ovation.ui.actions.spi;

import java.io.File;

public interface FileManager {

    boolean revealInFinder(File f);

    public String getRevealText();
}
