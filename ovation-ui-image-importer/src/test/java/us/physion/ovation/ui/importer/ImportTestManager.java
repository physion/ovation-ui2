/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import us.physion.ovation.ui.test.TestManager;

/**
 *
 * @author huecotanks
 */
public class ImportTestManager extends TestManager{
    
    private static final String LICENSE_TEXT = "crS9RjS6wJgmZkJZ1WRbdEtIIwynAVmqFwrooGgsM7ytyR+wCD3xpjJEENey+b0GVVEgib++HAKh94LuvLQXQ2lL2UCUo75xJwVLL3wmd21WbumQqKzZk9p6fkHCVoiSxgon+2RaGA75ckKNmUVTeIBn+QkalKCg9p1P7FbWqH3diXlAOKND2mwjI8V4unq7aaKEUuCgdU9V/BjFBkoytG8FzyBCNn+cBUNTByYy7RxYxH37xECZJ6/hG/vP4QjKpks9cu3yQL9QjXBQIizrzini0eQj62j+QzCSf0oQg8KdIeZHuU+ZSZZ1pUHLYiOiQWaOL9cVPxqMzh5Q/Zvu6Q==";
    private static final String LICENSE_INSTITUTION = "Institution";
    private static final String LICENSE_GROUP = "Lab";
    private static final String FIRST_USER = "TestUser";
    private static final String FIRST_PASSWORD = "password";
    private static final String CONNECTION_FILE = "data" + File.separator + "import-test.connection";


    public ImportTestManager() {
        super(CONNECTION_FILE, LICENSE_INSTITUTION, LICENSE_GROUP, LICENSE_TEXT, FIRST_USER, FIRST_PASSWORD);
    }

    @Override
    public String getLicenseText() {
        return LICENSE_TEXT;
    }

    @Override
    public String getLicenseInstitution() {
        return LICENSE_INSTITUTION;
    }

    @Override
    public String getLicenseGroup() {
        return LICENSE_GROUP;
    }

    @Override
    public String getConnectionFile() {
       return CONNECTION_FILE;
    }

    @Override
    public String getFirstUserName() {
        return FIRST_USER;
    }

    @Override
    public String getFirstUserPassword() {
        return FIRST_PASSWORD;
    }
}
