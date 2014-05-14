/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.Node.Property;
import us.physion.ovation.domain.AnnotatableEntity;
import us.physion.ovation.ui.interfaces.IEntityWrapper;


/**
 *
 * @author huecotanks
 */
//DEPRICATED
public class EntityProperty extends Property{

    boolean writable;
    Object value;
    String key;
    
    IEntityWrapper entity;
    
    EntityProperty(IEntityWrapper e, String key, Object value, boolean canWrite)
    {
        super(value.getClass());
        entity = e;
        this.key = key;
        this.value = value;
        writable = canWrite;
    }
    
    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public boolean canWrite() {
        return writable;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        entity.getEntity(AnnotatableEntity.class).addProperty(key, t);
        value = t;
    }
    
}
