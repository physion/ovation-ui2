/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import us.physion.ovation.domain.OvationEntity;

/**
 *
 * @author huecotanks
 */
public interface IEntityWrapper {

    String getDisplayName();

    OvationEntity getEntity();
    
    <T extends OvationEntity> T getEntity(Class<T> clazz);

    Class getType();

    String getURI();
}
