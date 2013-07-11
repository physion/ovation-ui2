/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import us.physion.ovation.ui.interfaces.UpdateStep;

/**
 *
 * @author huecotanks
 */
public class UpdateSchemaStep implements UpdateStep{

    String schemafile;
    
    UpdateSchemaStep(String schema)
    {
        schemafile = schema;
    }
    @Override
    public String getStepDescriptor() {
        return schemafile;
    }
    
     @Override 
     public int hashCode()
    {
        return getStepDescriptor().hashCode();
    }
    
    @Override 
    public boolean equals(Object o)
    {
        if (o instanceof UpdateSchemaStep)
            return getStepDescriptor().equals(((UpdateSchemaStep)o).getStepDescriptor());
        return false;
    }
    
    
}
