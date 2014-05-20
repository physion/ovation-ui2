package us.physion.ovation.ui.interfaces;

import com.google.common.util.concurrent.ListenableFuture;

public interface RefreshableNode
{
    public ListenableFuture<Void> refresh();
}
