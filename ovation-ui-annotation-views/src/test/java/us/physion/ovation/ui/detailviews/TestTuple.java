/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

/**
 *
 * @author huecotanks
 */
class TestTuple {
    String key;
    Object value;
    TestTuple(String key, Object value)
    {
        this.key = key;
        this.value = value;
    }
    
    public String getKey()
    {
        return key;
    }
    
    public Object getValue()
    {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) 
    {
        if (obj instanceof TestTuple)
        {
            return (((TestTuple)obj).getKey().equals(key) && ((TestTuple)obj).getValue().equals(value));
        }
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return key.hashCode();
    }
}
