/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import us.physion.ovation.domain.Resource;
/**
 *
 * @author jackie
 */
public interface IResourceWrapper {

    Resource getEntity();

    String getName();

    String getURI();

    String toString();

}
