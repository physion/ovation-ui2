/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import us.physion.ovation.ui.ResizableTree;
import us.physion.ovation.ui.TableNode;

/**
 *
 * @author jackie
 */
public class MockResizableTree implements ResizableTree {

    boolean wasResized = false;

    @Override
    public void resizeNode(TableNode n) {
        wasResized = true;
    }

    boolean wasResized() {
        return wasResized;
    }
}