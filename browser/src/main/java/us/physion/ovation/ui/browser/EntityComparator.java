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
public class EntityComparator<T extends EntityWrapper> implements Comparator<T>{

    @Override
    public int compare(T o1, T o2) {
        return o1.getDisplayName().toLowerCase().compareTo(o2.getDisplayName().toLowerCase());
    }
    
}
