/*
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */

package us.physion.ovation.ui.interfaces;

/**
 * Created by IntelliJ IDEA.
 * User: kiwiberry
 * Date: Sep 21, 2011
 * Time: 4:18:00 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IUpdateUI{
    public void update(int progress, String text);
    public void cancel();
}
