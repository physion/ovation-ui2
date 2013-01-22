/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import java.util.ArrayList;

/**
 *
 * @author huecotanks
 */
public class MultiUserParameter {

    ArrayList<Object> values;

    MultiUserParameter(Object value) {
        values = new ArrayList();
        add(value);
    }

    public void add(Object value) {
        if (value instanceof MultiUserParameter) {
            values.addAll(((MultiUserParameter) value).values);
        } else {
            values.add(value);
        }
    }

    @Override
    public String toString() {
        String s = "{";
        for (Object value : values) {
            s += value + ", ";
        }
        if (s.length() == 1) {
            return "";
        }
        return s.substring(0, s.length() - 2) + "}";
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof MultiUserParameter)
        {
            if (values.size() != ((MultiUserParameter)o).values.size())
                return false;
            for (Object v : ((MultiUserParameter)o).values)
            {
                if (!values.contains(v))
                    return false;
            }
            return true;
        }
        return false;
    }
}
