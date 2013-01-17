package us.physion.ovation.ui.interfaces;
import java.util.List;

public interface UpdateInfo
{
    public int getSchemaVersion();
    public String getSpecificationVersion();
    public List<UpdateStep> getUpdateSteps(); 
}