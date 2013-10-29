/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.util.Comparator;

/**
 *
 * @author huecotanks
 */
public class EntityComparator implements Comparator<EntityWrapper>{

    @Override
    public int compare(EntityWrapper o1, EntityWrapper o2) {
        return o1.getDisplayName().compareTo(o2.getDisplayName());
    }
    
}
